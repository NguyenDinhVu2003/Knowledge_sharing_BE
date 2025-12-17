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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    @Override
    public SearchResultResponse searchWithFacets(DocumentSearchRequest request, Long currentUserId) {
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

        // Calculate facets (from all matching documents without pagination)
        List<Document> allMatches = documentRepository.findAll(spec);
        Map<String, Long> tagFacets = calculateTagFacets(allMatches);
        Map<String, Long> fileTypeFacets = calculateFileTypeFacets(allMatches);
        Map<String, Long> sharingLevelFacets = calculateSharingLevelFacets(allMatches);
        Map<String, Long> ownerFacets = calculateOwnerFacets(allMatches);

        long searchTime = System.currentTimeMillis() - startTime;

        return SearchResultResponse.builder()
                .documents(documents)
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .pageSize(page.getSize())
                .tagFacets(tagFacets)
                .fileTypeFacets(fileTypeFacets)
                .sharingLevelFacets(sharingLevelFacets)
                .ownerFacets(ownerFacets)
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
                direction = sortOrder != null && sortOrder.equalsIgnoreCase("asc")
                    ? Sort.Direction.ASC : Sort.Direction.DESC;
                property = "averageRating";
                break;
            case "popular":
                // Sort by average rating as proxy for popularity
                direction = Sort.Direction.DESC;
                property = "averageRating";
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

    private Map<String, Long> calculateTagFacets(List<Document> documents) {
        return documents.stream()
                .flatMap(doc -> doc.getTags().stream())
                .collect(Collectors.groupingBy(
                        Tag::getName,
                        Collectors.counting()
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (v1, v2) -> v1,
                        LinkedHashMap::new
                ));
    }

    private Map<String, Long> calculateFileTypeFacets(List<Document> documents) {
        return documents.stream()
                .collect(Collectors.groupingBy(
                        doc -> doc.getFileType().name(),
                        Collectors.counting()
                ));
    }

    private Map<String, Long> calculateSharingLevelFacets(List<Document> documents) {
        return documents.stream()
                .collect(Collectors.groupingBy(
                        doc -> doc.getSharingLevel().name(),
                        Collectors.counting()
                ));
    }

    private Map<String, Long> calculateOwnerFacets(List<Document> documents) {
        return documents.stream()
                .collect(Collectors.groupingBy(
                        doc -> doc.getOwner().getUsername(),
                        Collectors.counting()
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(20) // Top 20 contributors
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (v1, v2) -> v1,
                        LinkedHashMap::new
                ));
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

