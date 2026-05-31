package com.civicpulse.backend.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CleanupTask {

    // Runs every day at midnight to clean up old resolved data or system temp files (demonstration purposes)
    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanupOldData() {
        System.out.println("Running scheduled cleanup task at " + LocalDateTime.now());
        // In a real app, you might delete old unread notifications or unused attachments
    }
}
