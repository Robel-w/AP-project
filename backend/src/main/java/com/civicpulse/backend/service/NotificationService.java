package com.civicpulse.backend.service;

import com.civicpulse.backend.model.Notification;
import com.civicpulse.backend.model.User;
import com.civicpulse.backend.model.UserRole;
import com.civicpulse.backend.repository.NotificationRepository;
import com.civicpulse.backend.repository.UserRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository, SimpMessagingTemplate messagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Async
    public void sendNotification(Long userId, String message, String type, Long relatedEntityId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return;

        Notification notification = Notification.builder()
                .user(user)
                .message(message)
                .type(type)
                .relatedEntityId(relatedEntityId)
                .isRead(false)
                .build();

        Notification saved = notificationRepository.save(notification);

        // Broadcast to WebSocket topic
        messagingTemplate.convertAndSend("/topic/notifications/" + userId, saved);
    }

    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    @Async
    public void notifyAdminsNearIssue(Double issueLat, Double issueLng, Double radiusKm, String message, Long feedbackId) {
        if (issueLat == null || issueLng == null) return;
        if (radiusKm == null || radiusKm <= 0) radiusKm = 10.0;

        List<User> admins = userRepository.findByRole(UserRole.ADMIN);
        for (User admin : admins) {
            if (admin.getLatitude() == null || admin.getLongitude() == null) continue;
            double dist = haversineKm(issueLat, issueLng, admin.getLatitude(), admin.getLongitude());
            if (dist > radiusKm) continue;

            sendNotification(admin.getId(), message, "NEW_FEEDBACK_NEARBY", feedbackId);
        }
    }

    private static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
