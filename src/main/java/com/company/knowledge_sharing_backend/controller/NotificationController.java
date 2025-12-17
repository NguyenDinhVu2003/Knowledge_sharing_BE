package com.company.knowledge_sharing_backend.controller;

import com.company.knowledge_sharing_backend.dto.response.MessageResponse;
import com.company.knowledge_sharing_backend.dto.response.NotificationResponse;
import com.company.knowledge_sharing_backend.entity.User;
import com.company.knowledge_sharing_backend.service.AuthService;
import com.company.knowledge_sharing_backend.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AuthService authService;

    /**
     * Get all notifications for current user
     * GET /api/notifications
     */
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications() {
        User currentUser = authService.getCurrentUser();
        List<NotificationResponse> notifications = notificationService.getUserNotifications(currentUser.getId());

        return ResponseEntity.ok(notifications);
    }

    /**
     * Get unread notifications
     * GET /api/notifications/unread
     */
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotifications() {
        User currentUser = authService.getCurrentUser();
        List<NotificationResponse> notifications = notificationService.getUnreadNotifications(currentUser.getId());

        return ResponseEntity.ok(notifications);
    }

    /**
     * Get unread count
     * GET /api/notifications/unread/count
     */
    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Integer>> getUnreadCount() {
        User currentUser = authService.getCurrentUser();
        Integer count = notificationService.getUnreadCount(currentUser.getId());

        Map<String, Integer> response = new HashMap<>();
        response.put("count", count);

        return ResponseEntity.ok(response);
    }

    /**
     * Mark notification as read
     * PUT /api/notifications/{id}/read
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<MessageResponse> markAsRead(@PathVariable Long id) {
        User currentUser = authService.getCurrentUser();
        notificationService.markAsRead(id, currentUser.getId());

        return ResponseEntity.ok(new MessageResponse("Notification marked as read"));
    }

    /**
     * Mark all notifications as read
     * PUT /api/notifications/read-all
     */
    @PutMapping("/read-all")
    public ResponseEntity<MessageResponse> markAllAsRead() {
        User currentUser = authService.getCurrentUser();
        notificationService.markAllAsRead(currentUser.getId());

        return ResponseEntity.ok(new MessageResponse("All notifications marked as read"));
    }

    /**
     * Delete notification
     * DELETE /api/notifications/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteNotification(@PathVariable Long id) {
        User currentUser = authService.getCurrentUser();
        notificationService.deleteNotification(id, currentUser.getId());

        return ResponseEntity.ok(new MessageResponse("Notification deleted"));
    }

    /**
     * Clear all notifications
     * DELETE /api/notifications/all
     */
    @DeleteMapping("/all")
    public ResponseEntity<MessageResponse> clearAllNotifications() {
        User currentUser = authService.getCurrentUser();
        notificationService.clearAllNotifications(currentUser.getId());

        return ResponseEntity.ok(new MessageResponse("All notifications cleared"));
    }
}

