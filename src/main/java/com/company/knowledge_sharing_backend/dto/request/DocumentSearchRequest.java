package com.company.knowledge_sharing_backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentSearchRequest {

    // Basic search
    private String query; // Search keyword in title, summary, content

    // Tag filters
    private List<String> tags; // Filter by tag names
    private Boolean matchAllTags = false; // true = AND, false = OR (default: false)

    // Metadata filters
    private String sharingLevel; // PRIVATE, GROUP, PUBLIC
    private String fileType; // PDF, DOC, IMAGE
    private Long ownerId; // Filter by specific owner
    private String ownerUsername; // Filter by owner username

    // Group filters
    private List<Long> groupIds; // Filter by groups

    // Rating filters
    private Double minRating; // Minimum average rating
    private Double maxRating; // Maximum average rating

    // Date filters
    private LocalDateTime fromDate; // Created after this date
    private LocalDateTime toDate; // Created before this date

    // Sorting
    private String sortBy = "recent"; // "recent", "oldest", "title", "rating", "popular", "relevance"
    private String sortOrder; // "asc", "desc" (default: depends on sortBy)

    // Pagination
    private Integer page = 0;
    private Integer size = 10;

    // Advanced options
    private Boolean includeArchived = false; // Include archived documents
    private Boolean onlyFavorited = false; // Only user's favorited documents (requires userId)
}

