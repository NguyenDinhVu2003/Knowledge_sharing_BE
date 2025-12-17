package com.company.knowledge_sharing_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchResultResponse {

    // Results
    private List<DocumentResponse> documents;

    // Pagination
    private Integer currentPage;
    private Integer totalPages;
    private Long totalElements;
    private Integer pageSize;

    // Facets (for filtering UI)
    private Map<String, Long> tagFacets; // tag name -> document count
    private Map<String, Long> fileTypeFacets; // file type -> count
    private Map<String, Long> sharingLevelFacets; // sharing level -> count
    private Map<String, Long> ownerFacets; // owner username -> count

    // Search metadata
    private String query;
    private Long searchTimeMs; // Search execution time
}

