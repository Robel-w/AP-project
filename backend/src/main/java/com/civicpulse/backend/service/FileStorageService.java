package com.civicpulse.backend.service;

import com.civicpulse.backend.model.Attachment;
import com.civicpulse.backend.model.Feedback;
import com.civicpulse.backend.repository.AttachmentRepository;
import com.civicpulse.backend.repository.FeedbackRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class FileStorageService {

    private final AttachmentRepository attachmentRepository;
    private final FeedbackRepository feedbackRepository;

    public FileStorageService(AttachmentRepository attachmentRepository, FeedbackRepository feedbackRepository) {
        this.attachmentRepository = attachmentRepository;
        this.feedbackRepository = feedbackRepository;
    }

    public Attachment storeFile(MultipartFile file, Long feedbackId) throws IOException {
        validateFile(file);

        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        if (fileName.contains("..")) {
            throw new RuntimeException("Sorry! Filename contains invalid path sequence " + fileName);
        }

        Feedback feedback = null;
        if (feedbackId != null) {
            feedback = feedbackRepository.findById(feedbackId)
                    .orElseThrow(() -> new RuntimeException("Feedback not found"));
        }

        Attachment attachment = Attachment.builder()
                .fileName(fileName)
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .data(file.getBytes())
                .feedback(feedback)
                .build();

        return attachmentRepository.save(attachment);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Uploaded file is empty.");
        }

        // 1. Validate file size (5MB limit)
        long maxSizeBytes = 5 * 1024 * 1024;
        if (file.getSize() > maxSizeBytes) {
            throw new RuntimeException("File size exceeds the allowed limit of 5MB.");
        }

        // 2. Validate file type (extension and MIME type correlation)
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new RuntimeException("Uploaded filename is missing.");
        }

        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex == -1) {
            throw new RuntimeException("Uploaded file must have an extension.");
        }
        String extension = originalFilename.substring(dotIndex + 1).toLowerCase();

        boolean isValidType = false;
        if (contentType != null) {
            String mime = contentType.toLowerCase();
            switch (mime) {
                case "image/jpeg":
                case "image/jpg":
                    isValidType = extension.equals("jpg") || extension.equals("jpeg");
                    break;
                case "image/png":
                    isValidType = extension.equals("png");
                    break;
                case "image/gif":
                    isValidType = extension.equals("gif");
                    break;
                case "image/webp":
                    isValidType = extension.equals("webp");
                    break;
                case "application/pdf":
                    isValidType = extension.equals("pdf");
                    break;
                case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
                    isValidType = extension.equals("docx");
                    break;
                case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
                    isValidType = extension.equals("xlsx");
                    break;
                case "text/plain":
                    isValidType = extension.equals("txt");
                    break;
                default:
                    isValidType = false;
            }
        }

        if (!isValidType) {
            throw new RuntimeException("Security violation: Unsupported or mismatched file type/extension.");
        }
    }

    public Attachment getFile(Long fileId) {
        return attachmentRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found with id " + fileId));
    }

    public List<Attachment> getFilesByFeedback(Long feedbackId) {
        return attachmentRepository.findByFeedbackId(feedbackId);
    }
}
