package com.company.knowledge_sharing_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteResponse {
    private Long id;
    private Long documentId;
    private String documentTitle;
    private String documentSummary;
    private String ownerUsername;
    private Double averageRating;
    private LocalDateTime createdAt;
}

