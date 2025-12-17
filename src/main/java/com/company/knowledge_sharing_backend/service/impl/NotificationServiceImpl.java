package com.company.knowledge_sharing_backend.service.impl;

import com.company.knowledge_sharing_backend.dto.response.NotificationResponse;
import com.company.knowledge_sharing_backend.exception.ResourceNotFoundException;
import com.company.knowledge_sharing_backend.exception.UnauthorizedException;
import com.company.knowledge_sharing_backend.entity.*;
import com.company.knowledge_sharing_backend.repository.NotificationRepository;
import com.company.knowledge_sharing_backend.repository.UserInterestRepository;
import com.company.knowledge_sharing_backend.repository.FavoriteRepository;
import com.company.knowledge_sharing_backend.repository.UserRepository;
import com.company.knowledge_sharing_backend.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserInterestRepository userInterestRepository;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private UserRepository userRepository;

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

            notificationRepository.save(notification);
        }
    }

    @Override
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

            notificationRepository.save(notification);
        }
    }

    @Override
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

        notificationRepository.save(notification);
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

