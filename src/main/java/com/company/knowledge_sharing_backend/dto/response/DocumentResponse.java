package com.company.knowledge_sharing_backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Document response with metadata and statistics")
public class DocumentResponse {

    @Schema(description = "Document ID", example = "1")
    private Long id;

    @Schema(description = "Document title", example = "Spring Boot Best Practices")
    private String title;

    @Schema(description = "Brief summary", example = "Comprehensive guide to Spring Boot development")
    private String summary;

    @Schema(description = "Full content (optional)")
    private String content;

    @Schema(description = "File path on server", example = "abc123.pdf")
    private String filePath;

    @Schema(description = "File type", example = "PDF", allowableValues = {"PDF", "DOC", "IMAGE"})
    private String fileType;

    @Schema(description = "File size in bytes", example = "1048576")
    private Long fileSize;

    @Schema(description = "Sharing level", example = "PUBLIC", allowableValues = {"PRIVATE", "GROUP", "PUBLIC"})
    private String sharingLevel;

    @Schema(description = "Current version number", example = "1")
    private Integer versionNumber;

    @Schema(description = "Is document archived", example = "false")
    private Boolean isArchived;

    // Owner information
    @Schema(description = "Owner user ID", example = "1")
    private Long ownerId;

    @Schema(description = "Owner username", example = "john.doe")
    private String ownerUsername;

    // Computed fields
    @Schema(description = "Average rating (1-5)", example = "4.5")
    private Double averageRating;

    @Schema(description = "Number of ratings", example = "10")
    private Integer ratingCount;

    // Timestamps
    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    // Related data
    @Schema(description = "Associated tags", example = "[\"Spring Boot\", \"Java\", \"Backend\"]")
    private List<String> tags;

    @Schema(description = "Associated group IDs", example = "[1, 2]")
    private List<Long> groupIds;
}

