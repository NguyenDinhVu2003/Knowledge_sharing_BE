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
    private String query; // Search keyword
    private List<String> tags; // Filter by tags
    private String sharingLevel; // Filter by sharing level
    private LocalDateTime fromDate; // Filter by creation date
    private LocalDateTime toDate;
    private String sortBy; // "recent", "popular", "title"
    private Integer page = 0;
    private Integer size = 10;
}

