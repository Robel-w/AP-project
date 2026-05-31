package com.civicpulse.backend.websocket;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private Long feedbackId;
    private String sender;
    private String content;
    private LocalDateTime timestamp;
    
    private Long attachmentId;
    private String attachmentName;
    private String attachmentType;
    private Long attachmentSize;
    private String attachmentUrl;

    public ChatMessage(Long feedbackId, String sender, String content) {
        this.feedbackId = feedbackId;
        this.sender = sender;
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }

    public ChatMessage(Long feedbackId, String sender, String content, Long attachmentId, String attachmentName, String attachmentType, Long attachmentSize, String attachmentUrl) {
        this.feedbackId = feedbackId;
        this.sender = sender;
        this.content = content;
        this.timestamp = LocalDateTime.now();
        this.attachmentId = attachmentId;
        this.attachmentName = attachmentName;
        this.attachmentType = attachmentType;
        this.attachmentSize = attachmentSize;
        this.attachmentUrl = attachmentUrl;
    }
}
