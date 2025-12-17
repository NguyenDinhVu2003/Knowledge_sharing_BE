package com.company.knowledge_sharing_backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Advanced search request with multiple filter criteria")
public class DocumentSearchRequest {

    // Basic search
    @Schema(description = "Search keyword (searches in title, summary, content)", example = "spring boot")
    private String query; // Search keyword in title, summary, content

    // Tag filters
    @Schema(description = "Filter by tag names", example = "[\"Java\", \"Spring Boot\"]")
    private List<String> tags; // Filter by tag names

    @Schema(description = "Match all tags (true) or any tag (false)", example = "false")
    private Boolean matchAllTags = false; // true = AND, false = OR (default: false)

    // Metadata filters
    @Schema(description = "Filter by sharing level", example = "PUBLIC", allowableValues = {"PRIVATE", "GROUP", "PUBLIC"})
    private String sharingLevel; // PRIVATE, GROUP, PUBLIC

    @Schema(description = "Filter by file type", example = "PDF", allowableValues = {"PDF", "DOC", "IMAGE"})
    private String fileType; // PDF, DOC, IMAGE

    @Schema(description = "Filter by owner ID", example = "1")
    private Long ownerId; // Filter by specific owner

    @Schema(description = "Filter by owner username", example = "john.doe")
    private String ownerUsername; // Filter by owner username

    // Group filters
    @Schema(description = "Filter by group IDs", example = "[1, 2]")
    private List<Long> groupIds; // Filter by groups

    // Rating filters
    @Schema(description = "Minimum average rating", example = "4.0")
    private Double minRating; // Minimum average rating

    @Schema(description = "Maximum average rating", example = "5.0")
    private Double maxRating; // Maximum average rating

    // Date filters
    @Schema(description = "Filter documents created after this date")
    private LocalDateTime fromDate; // Created after this date

    @Schema(description = "Filter documents created before this date")
    private LocalDateTime toDate; // Created before this date

    // Sorting
    @Schema(description = "Sort field", example = "recent",
            allowableValues = {"recent", "oldest", "title", "rating", "popular", "relevance"})
    private String sortBy = "recent"; // "recent", "oldest", "title", "rating", "popular", "relevance"

    @Schema(description = "Sort order", example = "desc", allowableValues = {"asc", "desc"})
    private String sortOrder; // "asc", "desc" (default: depends on sortBy)

    // Pagination
    @Schema(description = "Page number (0-indexed)", example = "0")
    private Integer page = 0;

    @Schema(description = "Page size", example = "10")
    private Integer size = 10;

    // Advanced options
    @Schema(description = "Include archived documents", example = "false")
    private Boolean includeArchived = false; // Include archived documents

    @Schema(description = "Only show favorited documents", example = "false")
    private Boolean onlyFavorited = false; // Only user's favorited documents (requires userId)
}

