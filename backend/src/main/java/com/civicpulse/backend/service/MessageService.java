package com.civicpulse.backend.service;

import com.civicpulse.backend.model.Feedback;
import com.civicpulse.backend.model.Message;
import com.civicpulse.backend.model.User;
import com.civicpulse.backend.repository.FeedbackRepository;
import com.civicpulse.backend.repository.MessageRepository;
import com.civicpulse.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.civicpulse.backend.model.Attachment;
import com.civicpulse.backend.repository.AttachmentRepository;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final FeedbackRepository feedbackRepository;
    private final AttachmentRepository attachmentRepository;

    public Message saveMessage(Long feedbackId, String username, String content, Long attachmentId){
        User user = userRepository.findByUsername(username).orElseThrow(()->new RuntimeException("User not Found!"));
        Feedback feedback = feedbackRepository.findById(feedbackId).orElseThrow(()-> new RuntimeException("feedback not found!"));

        Message message = new Message();
        message.setContent(content);
        message.setUser(user);
        message.setFeedback(feedback);

        if (attachmentId != null) {
            Attachment attachment = attachmentRepository.findById(attachmentId)
                    .orElseThrow(() -> new RuntimeException("Attachment not found"));
            message.setAttachment(attachment);
        }

        return messageRepository.save(message);
    }
}
