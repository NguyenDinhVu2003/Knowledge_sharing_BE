package com.company.knowledge_sharing_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * User profile information response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    private Long id;
    private String username;
    private String email;
    private String role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Statistics
    private Integer totalDocuments;
    private Integer totalFavorites;
    private Integer totalRatings;
    private Integer totalInterests;

    // User interests (tags user is following)
    private List<TagWithCountResponse> interests;
}

