package com.company.knowledge_sharing_backend.service.impl;

import com.company.knowledge_sharing_backend.dto.request.DocumentSearchRequest;
import com.company.knowledge_sharing_backend.dto.response.DocumentResponse;
import com.company.knowledge_sharing_backend.dto.response.SearchResultResponse;
import com.company.knowledge_sharing_backend.entity.Document;
import com.company.knowledge_sharing_backend.entity.FileType;
import com.company.knowledge_sharing_backend.entity.SharingLevel;
import com.company.knowledge_sharing_backend.entity.Tag;
import com.company.knowledge_sharing_backend.repository.DocumentRepository;
import com.company.knowledge_sharing_backend.service.SearchService;
import com.company.knowledge_sharing_backend.specification.DocumentSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class SearchServiceImpl implements SearchService {

    @Autowired
    private DocumentRepository documentRepository;

    @Override
    public SearchResultResponse advancedSearch(DocumentSearchRequest request, Long currentUserId) {
        long startTime = System.currentTimeMillis();

        // Build specification
        Specification<Document> spec = DocumentSpecification.buildSpecification(request, currentUserId);

        // Build pageable with sorting
        Pageable pageable = buildPageable(request);

        // Execute search
        Page<Document> page = documentRepository.findAll(spec, pageable);

        // Convert to response
        List<DocumentResponse> documents = page.getContent().stream()
                .map(this::mapToDocumentResponse)
                .collect(Collectors.toList());

        // Post-process sorting for rating-based sorts (since averageRating is transient)
        String sortBy = request.getSortBy() != null ? request.getSortBy().toLowerCase() : "recent";
        if ("rating".equals(sortBy) || "popular".equals(sortBy)) {
            boolean ascending = request.getSortOrder() != null &&
                              request.getSortOrder().equalsIgnoreCase("asc");
            documents = documents.stream()
                    .sorted((d1, d2) -> {
                        Double rating1 = d1.getAverageRating() != null ? d1.getAverageRating() : 0.0;
                        Double rating2 = d2.getAverageRating() != null ? d2.getAverageRating() : 0.0;
                        return ascending ? rating1.compareTo(rating2) : rating2.compareTo(rating1);
                    })
                    .collect(Collectors.toList());
        }

        long searchTime = System.currentTimeMillis() - startTime;

        return SearchResultResponse.builder()
                .documents(documents)
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .pageSize(page.getSize())
                .query(request.getQuery())
                .searchTimeMs(searchTime)
                .build();
    }


    // ==================== HELPER METHODS ====================

    private Pageable buildPageable(DocumentSearchRequest request) {
        Sort sort = buildSort(request);
        return PageRequest.of(request.getPage(), request.getSize(), sort);
    }

    private Sort buildSort(DocumentSearchRequest request) {
        String sortBy = request.getSortBy() != null ? request.getSortBy() : "recent";
        String sortOrder = request.getSortOrder();

        Sort.Direction direction;
        String property;

        switch (sortBy.toLowerCase()) {
            case "oldest":
                direction = Sort.Direction.ASC;
                property = "createdAt";
                break;
            case "title":
                direction = sortOrder != null && sortOrder.equalsIgnoreCase("desc")
                    ? Sort.Direction.DESC : Sort.Direction.ASC;
                property = "title";
                break;
            case "rating":
            case "popular":
                // Cannot sort by transient field in database
                // Will sort in memory after fetching results
                direction = Sort.Direction.DESC;
                property = "createdAt";
                break;
            case "relevance":
                // For relevance, we'd typically use full-text search scores
                // For now, use recent as fallback
                direction = Sort.Direction.DESC;
                property = "createdAt";
                break;
            case "recent":
            default:
                direction = Sort.Direction.DESC;
                property = "createdAt";
                break;
        }

        return Sort.by(direction, property);
    }


    private DocumentResponse mapToDocumentResponse(Document document) {
        return DocumentResponse.builder()
                .id(document.getId())
                .title(document.getTitle())
                .summary(document.getSummary())
                .ownerUsername(document.getOwner().getUsername())
                .ownerId(document.getOwner().getId())
                .tags(document.getTags().stream()
                        .map(Tag::getName)
                        .collect(Collectors.toList()))
                .fileType(document.getFileType().name())
                .fileSize(document.getFileSize())
                .sharingLevel(document.getSharingLevel().name())
                .versionNumber(document.getVersionNumber())
                .averageRating(document.getAverageRating())
                .isArchived(document.getIsArchived())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }
}

