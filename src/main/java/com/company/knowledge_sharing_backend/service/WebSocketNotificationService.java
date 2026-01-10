package com.company.knowledge_sharing_backend.service;

import com.company.knowledge_sharing_backend.dto.response.NotificationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Service for sending real-time notifications via WebSocket
 */
@Service
@Slf4j
public class WebSocketNotificationService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Send notification to specific user via WebSocket
     *
     * @param userId - Target user ID
     * @param notification - Notification to send
     */
    public void sendNotificationToUser(Long userId, NotificationResponse notification) {
        String destination = "/topic/notifications/" + userId;

        try {
            messagingTemplate.convertAndSend(destination, notification);
        } catch (Exception e) {
            log.error("Failed to send notification to user {}: {}", userId, e.getMessage());
        }
    }

    /**
     * Send notification count update to user
     *
     * @param userId - Target user ID
     * @param unreadCount - New unread count
     */
    public void sendUnreadCountToUser(Long userId, Integer unreadCount) {
        String destination = "/topic/notifications/" + userId + "/count";

        try {
            messagingTemplate.convertAndSend(destination, unreadCount);
        } catch (Exception e) {
            log.error("Failed to send unread count to user {}: {}", userId, e.getMessage());
        }
    }
}

