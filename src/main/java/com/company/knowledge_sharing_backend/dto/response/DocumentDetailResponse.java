package com.company.knowledge_sharing_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentDetailResponse {
    private Long id;
    private String title;
    private String summary;
    private String content;
    private String filePath;
    private String fileType;
    private Long fileSize;
    private String sharingLevel;
    private Integer versionNumber;
    private Boolean isArchived;

    // Owner information
    private Long ownerId;
    private String ownerUsername;
    private String ownerEmail;

    // Statistics
    private Double averageRating;
    private Integer ratingCount;
    private Integer favoriteCount;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Related data
    private List<String> tags;
    private List<Long> groupIds;
    private List<DocumentVersionResponse> versions;

    // User-specific data
    private Integer userRating; // Current user's rating
    private Boolean isFavorited; // Is favorited by current user
}

