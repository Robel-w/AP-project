package com.civicpulse.backend.service;

import com.civicpulse.backend.dto.FeedbackRequest;
import com.civicpulse.backend.dto.FeedbackResponse;
import com.civicpulse.backend.dto.MessageResponse;
import com.civicpulse.backend.model.Feedback;
import com.civicpulse.backend.model.User;
import com.civicpulse.backend.repository.FeedbackRepository;
import com.civicpulse.backend.repository.UserRepository;
import com.civicpulse.backend.model.Attachment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedbackService {


    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    //Create new feedback

    public FeedbackResponse createFeedback(FeedbackRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found!"));

        if (request.getLatitude() == null || request.getLongitude() == null) {
            throw new RuntimeException("Latitude/Longitude are required");
        }

        Feedback feedback = new Feedback();
        feedback.setTitle(request.getTitle());
        feedback.setCategory(request.getCategory());
        feedback.setDescription(request.getDescription());
        feedback.setLatitude(request.getLatitude());
        feedback.setLongitude(request.getLongitude());
        feedback.setUser(user);

        Feedback savedFeedback = feedbackRepository.save(feedback);

        // Notify admins near the issue location (async)
        notificationService.notifyAdminsNearIssue(
                savedFeedback.getLatitude(),
                savedFeedback.getLongitude(),
                10.0,
                "New issue reported: " + savedFeedback.getTitle(),
                savedFeedback.getId()
        );

        return convertToResponse(savedFeedback);
    }

    //Get all feedback with their messages (chat history)
    public List<FeedbackResponse> getAllFeedback() {
        return feedbackRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    //Get single feedback with full chat history

    public FeedbackResponse getFeedbackById(Long id) {
        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Feedback not found"));

        return convertToResponse(feedback);
    }

    public List<FeedbackResponse> getNearby(Double lat, Double lng, Double radiusKm, String category) {
        if (lat == null || lng == null) {
            throw new RuntimeException("lat/lng are required");
        }
        if (radiusKm == null || radiusKm <= 0) {
            radiusKm = 5.0;
        }

        // Fast + minimal: bounding box query in DB, precise distance filter in memory.
        double latDelta = radiusKm / 111.32; // ~km per degree latitude
        double lngDelta = radiusKm / (111.32 * Math.cos(Math.toRadians(lat)));
        double minLat = lat - latDelta;
        double maxLat = lat + latDelta;
        double minLng = lng - lngDelta;
        double maxLng = lng + lngDelta;

        List<Feedback> candidates = (category == null || category.isBlank())
                ? feedbackRepository.findByLatitudeBetweenAndLongitudeBetween(minLat, maxLat, minLng, maxLng)
                : feedbackRepository.findByCategoryAndLatitudeBetweenAndLongitudeBetween(category, minLat, maxLat, minLng, maxLng);

        final double finalRadiusKm = radiusKm;
        return candidates.stream()
                .filter(f -> f.getLatitude() != null && f.getLongitude() != null)
                .filter(f -> haversineKm(lat, lng, f.getLatitude(), f.getLongitude()) <= finalRadiusKm)
                .map(this::convertToResponse)
                .collect(Collectors.toList());
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

    //Helper method to convert Entity → DTO (including messages)
    private FeedbackResponse convertToResponse(Feedback feedback) {
        // Convert messages from entity to MessageResponse DTO
        List<MessageResponse> messageResponses = feedback.getMessages().stream()
                .map(message -> {
                    MessageResponse resp = new MessageResponse();
                    resp.setId(message.getId());
                    resp.setSender(message.getUser().getUsername());
                    resp.setContent(message.getContent());
                    resp.setTimestamp(message.getTimestamp());
                    if (message.getAttachment() != null) {
                        Attachment att = message.getAttachment();
                        resp.setAttachmentId(att.getId());
                        resp.setAttachmentName(att.getFileName());
                        resp.setAttachmentType(att.getFileType());
                        resp.setAttachmentSize(att.getFileSize());
                        resp.setAttachmentUrl("/api/files/download/" + att.getId());
                    }
                    return resp;
                })
                .collect(Collectors.toList());

        return new FeedbackResponse(
                feedback.getId(),
                feedback.getTitle(),
                feedback.getCategory(),
                feedback.getDescription(),
                feedback.getLatitude(),
                feedback.getLongitude(),
                feedback.getCreatedAt(),
                feedback.getUser().getUsername(),
                messageResponses                     // ← Chat history
        );
    }
}