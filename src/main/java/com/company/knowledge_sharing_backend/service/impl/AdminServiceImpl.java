package com.company.knowledge_sharing_backend.service.impl;

import com.company.knowledge_sharing_backend.dto.response.SystemStatistics;
import com.company.knowledge_sharing_backend.dto.response.UserManagementResponse;
import com.company.knowledge_sharing_backend.exception.BadRequestException;
import com.company.knowledge_sharing_backend.exception.ResourceNotFoundException;
import com.company.knowledge_sharing_backend.entity.*;
import com.company.knowledge_sharing_backend.repository.*;
import com.company.knowledge_sharing_backend.service.AdminService;
import com.company.knowledge_sharing_backend.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminServiceImpl implements AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Override
    @Transactional(readOnly = true)
    public List<UserManagementResponse> getAllUsers() {
        List<User> users = userRepository.findAll();

        return users.stream()
                .map(this::mapToUserManagementResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserManagementResponse getUserDetails(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return mapToUserManagementResponse(user);
    }

    @Override
    @CacheEvict(value = "statistics", allEntries = true)
    public void updateUserRole(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        try {
            Role role = Role.valueOf(roleName.toUpperCase());
            user.setRole(role);
            userRepository.save(user);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid role. Must be EMPLOYEE or ADMIN");
        }
    }

    @Override
    @CacheEvict(value = "statistics", allEntries = true)
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Delete user's documents and associated files
        Page<Document> documentsPage = documentRepository.findByOwnerId(userId, Pageable.unpaged());
        List<Document> documents = documentsPage.getContent();

        for (Document document : documents) {
            try {
                fileStorageService.deleteFile(document.getFilePath());
            } catch (Exception e) {
                // Log error but continue
                System.err.println("Failed to delete file: " + document.getFilePath());
            }
        }

        // Delete user (cascade will handle related entities)
        userRepository.delete(user);
    }

    @Override
    @Transactional(readOnly = true)
    public SystemStatistics getSystemStatistics() {
        // User statistics
        Integer totalUsers = (int) userRepository.count();

        LocalDateTime monthAgo = LocalDateTime.now().minusMonths(1);
        List<User> allUsers = userRepository.findAll();
        Integer newUsersThisMonth = (int) allUsers.stream()
                .filter(u -> u.getCreatedAt().isAfter(monthAgo))
                .count();

        Integer activeUsers = totalUsers; // Simplified - could track last login

        // Document statistics
        Integer totalDocuments = (int) documentRepository.count();

        List<Document> allDocuments = documentRepository.findAll();
        Integer documentsThisMonth = (int) allDocuments.stream()
                .filter(d -> d.getCreatedAt().isAfter(monthAgo))
                .count();

        // Documents by type
        Map<String, Integer> documentsByType = allDocuments.stream()
                .collect(Collectors.groupingBy(
                        d -> d.getFileType().name(),
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));

        // Documents by sharing level
        Map<String, Integer> documentsBySharingLevel = allDocuments.stream()
                .collect(Collectors.groupingBy(
                        d -> d.getSharingLevel().name(),
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));

        // Engagement statistics
        Integer totalRatings = (int) ratingRepository.count();

        List<Rating> allRatings = ratingRepository.findAll();
        Double averageRating = allRatings.isEmpty() ? 0.0 :
                allRatings.stream()
                        .mapToInt(Rating::getRatingValue)
                        .average()
                        .orElse(0.0);

        Integer totalFavorites = (int) favoriteRepository.count();
        Integer totalNotifications = (int) notificationRepository.count();

        // Tag and Group statistics
        Integer totalTags = (int) tagRepository.count();
        Integer totalGroups = (int) groupRepository.count();

        // Top tags
        List<Object[]> tagStats = tagRepository.findAllWithDocumentCount();
        Map<String, Long> topTags = tagStats.stream()
                .limit(10)
                .collect(Collectors.toMap(
                        arr -> ((Tag) arr[0]).getName(),
                        arr -> (Long) arr[1],
                        (v1, v2) -> v1,
                        LinkedHashMap::new
                ));

        // Top contributors
        Map<String, Long> topContributors = allDocuments.stream()
                .collect(Collectors.groupingBy(
                        d -> d.getOwner().getUsername(),
                        Collectors.counting()
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (v1, v2) -> v1,
                        LinkedHashMap::new
                ));

        // Top rated documents
        Map<String, Double> topRatedDocuments = allDocuments.stream()
                .filter(d -> d.getAverageRating() != null && d.getAverageRating() > 0)
                .sorted(Comparator.comparing(Document::getAverageRating).reversed())
                .limit(10)
                .collect(Collectors.toMap(
                        Document::getTitle,
                        Document::getAverageRating,
                        (v1, v2) -> v1,
                        LinkedHashMap::new
                ));

        return SystemStatistics.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .newUsersThisMonth(newUsersThisMonth)
                .totalDocuments(totalDocuments)
                .documentsThisMonth(documentsThisMonth)
                .documentsByType(documentsByType)
                .documentsBySharingLevel(documentsBySharingLevel)
                .totalRatings(totalRatings)
                .averageRating(Math.round(averageRating * 100.0) / 100.0)
                .totalFavorites(totalFavorites)
                .totalNotifications(totalNotifications)
                .totalTags(totalTags)
                .totalGroups(totalGroups)
                .topTags(topTags)
                .topContributors(topContributors)
                .topRatedDocuments(topRatedDocuments)
                .build();
    }

    @Override
    public void deleteDocument(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

        // Delete file
        try {
            fileStorageService.deleteFile(document.getFilePath());
        } catch (Exception e) {
            System.err.println("Failed to delete file: " + document.getFilePath());
        }

        // Delete document (cascade will handle related entities)
        documentRepository.delete(document);
    }

    @Override
    public void deleteRating(Long ratingId) {
        Rating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new ResourceNotFoundException("Rating not found with id: " + ratingId));

        ratingRepository.delete(rating);
    }

    @Override
    public void deleteNotification(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));

        notificationRepository.delete(notification);
    }

    // ==================== HELPER METHODS ====================

    private UserManagementResponse mapToUserManagementResponse(User user) {
        Integer documentCount = documentRepository.countByOwnerId(user.getId()).intValue();
        Integer ratingCount = ratingRepository.countByUserId(user.getId()).intValue();
        Integer favoriteCount = favoriteRepository.countByUserId(user.getId()).intValue();

        return UserManagementResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .documentCount(documentCount)
                .ratingCount(ratingCount)
                .favoriteCount(favoriteCount)
                .createdAt(user.getCreatedAt())
                .lastActivity(user.getUpdatedAt()) // Simplified - could track actual last login
                .build();
    }
}

