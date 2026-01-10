package com.company.knowledge_sharing_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Tag with document count and interest status
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagWithCountResponse {

    private Long id;
    private String name;
    private String description;
    private Long documentCount;
    private Boolean isInterested; // Whether current user is interested in this tag
}

