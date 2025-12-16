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
public class DocumentResponse {
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

    // Computed fields
    private Double averageRating;
    private Integer ratingCount;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Related data
    private List<String> tags;
    private List<Long> groupIds;
}

