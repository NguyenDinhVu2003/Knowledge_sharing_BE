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
public class GroupResponse {
    private Long id;
    private String name;
    private String description;
    private Integer memberCount;
    private Integer documentCount;
    private List<String> memberUsernames;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

