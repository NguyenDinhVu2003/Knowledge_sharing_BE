package com.company.knowledge_sharing_backend.service;

import com.company.knowledge_sharing_backend.dto.response.NotificationResponse;
import com.company.knowledge_sharing_backend.entity.Document;

import java.util.List;

public interface NotificationService {

    // Get notifications
    List<NotificationResponse> getUserNotifications(Long userId);

    List<NotificationResponse> getUnreadNotifications(Long userId);

    Integer getUnreadCount(Long userId);

    // Mark as read
    void markAsRead(Long notificationId, Long userId);

    void markAllAsRead(Long userId);

    // Delete notifications
    void deleteNotification(Long notificationId, Long userId);

    void clearAllNotifications(Long userId);

    // Create notifications (internal use)
    void notifyNewDocument(Document document);

    void notifyDocumentUpdate(Document document);

    void notifyDocumentRated(Document document, Integer rating);

    void notifyNewComment(Long documentId, Long commentAuthorId, String commentContent);

    void notifyCommentReply(Long parentCommentAuthorId, Long replyAuthorId, String replyContent, Long documentId);
}

