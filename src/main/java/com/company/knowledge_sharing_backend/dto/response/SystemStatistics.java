package com.company.knowledge_sharing_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemStatistics {
    // User statistics
    private Integer totalUsers;
    private Integer activeUsers;
    private Integer newUsersThisMonth;

    // Document statistics
    private Integer totalDocuments;
    private Integer documentsThisMonth;
    private Map<String, Integer> documentsByType; // PDF, DOC, IMAGE
    private Map<String, Integer> documentsBySharingLevel; // PRIVATE, GROUP, PUBLIC

    // Engagement statistics
    private Integer totalRatings;
    private Double averageRating;
    private Integer totalFavorites;
    private Integer totalNotifications;

    // Tag and Group statistics
    private Integer totalTags;
    private Integer totalGroups;

    // Top performers
    private Map<String, Long> topTags; // tag name -> document count
    private Map<String, Long> topContributors; // username -> document count
    private Map<String, Double> topRatedDocuments; // document title -> rating
}

