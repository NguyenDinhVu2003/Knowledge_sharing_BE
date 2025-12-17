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
public class RatingResponse {
    private Long id;
    private Long documentId;
    private String documentTitle;
    private Long userId;
    private String username;
    private Integer ratingValue;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

