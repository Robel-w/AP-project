package com.civicpulse.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private Long id;
    private String sender;
    private String content;
    private LocalDateTime timestamp;

    private Long attachmentId;
    private String attachmentName;
    private String attachmentType;
    private Long attachmentSize;
    private String attachmentUrl;
}
