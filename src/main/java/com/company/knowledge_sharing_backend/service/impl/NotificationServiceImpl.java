package com.company.knowledge_sharing_backend.service.impl;

import com.company.knowledge_sharing_backend.dto.response.NotificationResponse;
import com.company.knowledge_sharing_backend.exception.ResourceNotFoundException;
import com.company.knowledge_sharing_backend.exception.UnauthorizedException;
import com.company.knowledge_sharing_backend.entity.*;
import com.company.knowledge_sharing_backend.repository.NotificationRepository;
import com.company.knowledge_sharing_backend.repository.UserInterestRepository;
import com.company.knowledge_sharing_backend.repository.FavoriteRepository;
import com.company.knowledge_sharing_backend.repository.UserRepository;
import com.company.knowledge_sharing_backend.repository.DocumentRepository;
import com.company.knowledge_sharing_backend.service.NotificationService;
import com.company.knowledge_sharing_backend.service.WebSocketNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserInterestRepository userInterestRepository;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private WebSocketNotificationService webSocketNotificationService;

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUserNotifications(Long userId) {
        // Verify user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);

        return notifications.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotifications(Long userId) {
        // Verify user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        List<Notification> notifications = notificationRepository
                .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);

        return notifications.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId).intValue();
    }

    @Override
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));

        // Verify ownership
        if (!notification.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You don't have permission to modify this notification");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Override
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }

    @Override
    public void deleteNotification(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));

        // Verify ownership
        if (!notification.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You don't have permission to delete this notification");
        }

        notificationRepository.delete(notification);
    }

    @Override
    public void clearAllNotifications(Long userId) {
        notificationRepository.deleteByUserId(userId);
    }

    @Override
    @Async("taskExecutor")
    public void notifyNewDocument(Document document) {
        // Get document tags
        Set<Tag> documentTags = document.getTags();

        if (documentTags.isEmpty()) {
            return; // No tags, no notifications
        }

        // Find users interested in these tags
        List<Long> interestedUserIds = userInterestRepository.findUserIdsByTags(new ArrayList<>(documentTags));

        // Remove document owner from notification list
        interestedUserIds.remove(document.getOwner().getId());

        // Create notifications for interested users
        for (Long userId : interestedUserIds) {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) continue;

            String tagNames = documentTags.stream()
                    .map(Tag::getName)
                    .limit(3)
                    .collect(Collectors.joining(", "));

            String message = String.format(
                    "New document \"%s\" matches your interests in %s",
                    document.getTitle(),
                    tagNames
            );

            Notification notification = Notification.builder()
                    .user(user)
                    .document(document)
                    .message(message)
                    .isRead(false)
                    .build();

            Notification saved = notificationRepository.save(notification);

            // Send real-time notification via WebSocket
            NotificationResponse response = mapToResponse(saved);
            webSocketNotificationService.sendNotificationToUser(user.getId(), response);

            // Send updated unread count
            Integer unreadCount = getUnreadCount(user.getId());
            webSocketNotificationService.sendUnreadCountToUser(user.getId(), unreadCount);
        }
    }

    @Override
    @Async("taskExecutor")
    public void notifyDocumentUpdate(Document document) {
        // Find users who favorited this document
        List<Favorite> favorites = favoriteRepository.findByDocumentId(document.getId());

        // Create notifications for users who favorited
        for (Favorite favorite : favorites) {
            User user = favorite.getUser();

            // Don't notify the owner
            if (user.getId().equals(document.getOwner().getId())) {
                continue;
            }

            String message = String.format(
                    "Document \"%s\" you favorited was updated to version %d",
                    document.getTitle(),
                    document.getVersionNumber()
            );

            Notification notification = Notification.builder()
                    .user(user)
                    .document(document)
                    .message(message)
                    .isRead(false)
                    .build();

            Notification saved = notificationRepository.save(notification);

            // Send real-time notification via WebSocket
            NotificationResponse response = mapToResponse(saved);
            webSocketNotificationService.sendNotificationToUser(user.getId(), response);

            // Send updated unread count
            Integer unreadCount = getUnreadCount(user.getId());
            webSocketNotificationService.sendUnreadCountToUser(user.getId(), unreadCount);
        }
    }

    @Override
    @Async("taskExecutor")
    public void notifyDocumentRated(Document document, Integer rating) {
        // Notify document owner
        User owner = document.getOwner();

        String message = String.format(
                "Your document \"%s\" received a %d-star rating",
                document.getTitle(),
                rating
        );

        Notification notification = Notification.builder()
                .user(owner)
                .document(document)
                .message(message)
                .isRead(false)
                .build();

        Notification saved = notificationRepository.save(notification);

        // Send real-time notification via WebSocket
        NotificationResponse response = mapToResponse(saved);
        webSocketNotificationService.sendNotificationToUser(owner.getId(), response);

        // Send updated unread count
        Integer unreadCount = getUnreadCount(owner.getId());
        webSocketNotificationService.sendUnreadCountToUser(owner.getId(), unreadCount);
    }

    @Override
    @Async("taskExecutor")
    public void notifyNewComment(Long documentId, Long commentAuthorId, String commentContent) {
        log.info("notifyNewComment called: documentId={}, commentAuthorId={}", documentId, commentAuthorId);

        // Get document
        Document document = documentRepository.findById(documentId).orElse(null);

        if (document == null) {
            log.warn("Document not found with id: {}", documentId);
            return;
        }

        // Notify document owner (if not the commenter)
        User owner = document.getOwner();
        if (!owner.getId().equals(commentAuthorId)) {
            User commentAuthor = userRepository.findById(commentAuthorId).orElse(null);
            if (commentAuthor == null) {
                log.warn("Comment author not found with id: {}", commentAuthorId);
                return;
            }

            String preview = commentContent.length() > 50
                    ? commentContent.substring(0, 50) + "..."
                    : commentContent;

            String message = String.format(
                    "%s commented on your document \"%s\": %s",
                    commentAuthor.getUsername(),
                    document.getTitle(),
                    preview
            );

            Notification notification = Notification.builder()
                    .user(owner)
                    .document(document)
                    .message(message)
                    .isRead(false)
                    .build();

            Notification saved = notificationRepository.save(notification);
            log.info("Comment notification created successfully: id={}, userId={}, message={}",
                    saved.getId(), owner.getId(), message);

            // Send real-time notification via WebSocket
            NotificationResponse response = mapToResponse(saved);
            webSocketNotificationService.sendNotificationToUser(owner.getId(), response);

            // Send updated unread count
            Integer unreadCount = getUnreadCount(owner.getId());
            webSocketNotificationService.sendUnreadCountToUser(owner.getId(), unreadCount);
        } else {
            log.info("Skipping notification - user commenting on own document");
        }
    }

    @Override
    @Async("taskExecutor")
    public void notifyCommentReply(Long parentCommentAuthorId, Long replyAuthorId, String replyContent, Long documentId) {
        log.info("notifyCommentReply called: parentCommentAuthorId={}, replyAuthorId={}, documentId={}",
                parentCommentAuthorId, replyAuthorId, documentId);

        // Don't notify if replying to own comment
        if (parentCommentAuthorId.equals(replyAuthorId)) {
            log.info("Skipping notification - user replying to own comment");
            return;
        }

        User parentCommentAuthor = userRepository.findById(parentCommentAuthorId).orElse(null);
        User replyAuthor = userRepository.findById(replyAuthorId).orElse(null);

        if (parentCommentAuthor == null) {
            log.warn("Parent comment author not found with id: {}", parentCommentAuthorId);
            return;
        }

        if (replyAuthor == null) {
            log.warn("Reply author not found with id: {}", replyAuthorId);
            return;
        }

        // Get document for reference
        Document document = documentRepository.findById(documentId).orElse(null);

        String preview = replyContent.length() > 50
                ? replyContent.substring(0, 50) + "..."
                : replyContent;

        String message = String.format(
                "%s replied to your comment%s: %s",
                replyAuthor.getUsername(),
                document != null ? " on \"" + document.getTitle() + "\"" : "",
                preview
        );

        Notification notification = Notification.builder()
                .user(parentCommentAuthor)
                .document(document)
                .message(message)
                .isRead(false)
                .build();

        Notification saved = notificationRepository.save(notification);
        log.info("Reply notification created successfully: id={}, userId={}, message={}",
                saved.getId(), parentCommentAuthor.getId(), message);

        // Send real-time notification via WebSocket
        NotificationResponse response = mapToResponse(saved);
        webSocketNotificationService.sendNotificationToUser(parentCommentAuthor.getId(), response);

        // Send updated unread count
        Integer unreadCount = getUnreadCount(parentCommentAuthor.getId());
        webSocketNotificationService.sendUnreadCountToUser(parentCommentAuthor.getId(), unreadCount);
    }

    // ==================== HELPER METHODS ====================

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .message(notification.getMessage())
                .isRead(notification.getIsRead())
                .documentId(notification.getDocument() != null ? notification.getDocument().getId() : null)
                .documentTitle(notification.getDocument() != null ? notification.getDocument().getTitle() : null)
                .createdAt(notification.getCreatedAt())
                .build();
    }
}

