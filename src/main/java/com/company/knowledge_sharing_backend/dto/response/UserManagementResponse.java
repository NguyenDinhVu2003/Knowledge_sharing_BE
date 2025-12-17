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
public class UserManagementResponse {
    private Long id;
    private String username;
    private String email;
    private String role;
    private Integer documentCount;
    private Integer ratingCount;
    private Integer favoriteCount;
    private LocalDateTime createdAt;
    private LocalDateTime lastActivity;
}

