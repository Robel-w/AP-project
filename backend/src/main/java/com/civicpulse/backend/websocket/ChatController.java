package com.civicpulse.backend.websocket;

import com.civicpulse.backend.model.Message;
import com.civicpulse.backend.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import com.civicpulse.backend.model.User;
import com.civicpulse.backend.model.Attachment;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ChatController {
    private final MessageService messageService;
    private final org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;
    private final com.civicpulse.backend.service.NotificationService notificationService;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessage chatMessage, SimpMessageHeaderAccessor headerAccessor){
        // Retrieve and authenticate HTTP Session user copied during handshake
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
        if (sessionAttributes == null) {
            throw new RuntimeException("Unauthorized: No session active.");
        }
        User user = (User) sessionAttributes.get("user");
        if (user == null) {
            throw new RuntimeException("Unauthorized: Please log in first.");
        }

        // Force the sender to be the authenticated session user (prevents spoofing)
        String authenticatedUsername = user.getUsername();

        // Save to database
        Message savedMessage = messageService.saveMessage(
                chatMessage.getFeedbackId(),
                authenticatedUsername,
                chatMessage.getContent(),
                chatMessage.getAttachmentId()
        );

        ChatMessage responseMessage = new ChatMessage(
                savedMessage.getFeedback().getId(),
                authenticatedUsername,
                savedMessage.getContent()
        );

        if (savedMessage.getAttachment() != null) {
            Attachment att = savedMessage.getAttachment();
            responseMessage.setAttachmentId(att.getId());
            responseMessage.setAttachmentName(att.getFileName());
            responseMessage.setAttachmentType(att.getFileType());
            responseMessage.setAttachmentSize(att.getFileSize());
            responseMessage.setAttachmentUrl("/api/files/download/" + att.getId());
        }

        // Broadcast to specific feedback room
        messagingTemplate.convertAndSend("/topic/feedback/" + chatMessage.getFeedbackId(), responseMessage);

        // Trigger a notification (Async) for the feedback owner
        notificationService.sendNotification(
            savedMessage.getFeedback().getUser().getId(),
            "New message in your feedback: " + savedMessage.getFeedback().getTitle(),
            "NEW_MESSAGE",
            savedMessage.getFeedback().getId()
        );
    }
}
