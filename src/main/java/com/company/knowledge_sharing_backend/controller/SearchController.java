package com.company.knowledge_sharing_backend.controller;

import com.company.knowledge_sharing_backend.dto.request.DocumentSearchRequest;
import com.company.knowledge_sharing_backend.dto.response.SearchResultResponse;
import com.company.knowledge_sharing_backend.entity.User;
import com.company.knowledge_sharing_backend.service.AuthService;
import com.company.knowledge_sharing_backend.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @Autowired
    private AuthService authService;

    /**
     * Advanced search with all filters
     * GET /api/search/advanced
     *
     * Query parameters:
     * - query: Search keyword
     * - tags: Comma-separated tag names
     * - matchAllTags: true/false (AND/OR logic for tags)
     * - sharingLevel: PRIVATE, GROUP, PUBLIC
     * - fileType: PDF, DOC, IMAGE
     * - ownerId: Filter by owner ID
     * - ownerUsername: Filter by owner username
     * - groupIds: Comma-separated group IDs
     * - minRating: Minimum rating (0-5)
     * - maxRating: Maximum rating (0-5)
     * - fromDate: Created after (ISO format)
     * - toDate: Created before (ISO format)
     * - sortBy: recent, oldest, title, rating, popular, relevance
     * - sortOrder: asc, desc
     * - page: Page number (default: 0)
     * - size: Page size (default: 10)
     * - includeArchived: true/false
     * - onlyFavorited: true/false
     */
    @GetMapping("/advanced")
    public ResponseEntity<SearchResultResponse> advancedSearch(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(required = false, defaultValue = "false") Boolean matchAllTags,
            @RequestParam(required = false) String sharingLevel,
            @RequestParam(required = false) String fileType,
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false) String ownerUsername,
            @RequestParam(required = false) List<Long> groupIds,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Double maxRating,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(required = false, defaultValue = "recent") String sortBy,
            @RequestParam(required = false) String sortOrder,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @RequestParam(required = false, defaultValue = "false") Boolean includeArchived,
            @RequestParam(required = false, defaultValue = "false") Boolean onlyFavorited) {

        User currentUser = authService.getCurrentUser();

        DocumentSearchRequest request = new DocumentSearchRequest();
        request.setQuery(query);
        request.setTags(tags);
        request.setMatchAllTags(matchAllTags);
        request.setSharingLevel(sharingLevel);
        request.setFileType(fileType);
        request.setOwnerId(ownerId);
        request.setOwnerUsername(ownerUsername);
        request.setGroupIds(groupIds);
        request.setMinRating(minRating);
        request.setMaxRating(maxRating);
        request.setFromDate(fromDate);
        request.setToDate(toDate);
        request.setSortBy(sortBy);
        request.setSortOrder(sortOrder);
        request.setPage(page);
        request.setSize(size);
        request.setIncludeArchived(includeArchived);
        request.setOnlyFavorited(onlyFavorited);

        SearchResultResponse response = searchService.advancedSearch(request, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Search with facets for filtering UI
     * POST /api/search/with-facets
     */
    @PostMapping("/with-facets")
    public ResponseEntity<SearchResultResponse> searchWithFacets(
            @RequestBody DocumentSearchRequest request) {

        User currentUser = authService.getCurrentUser();
        SearchResultResponse response = searchService.searchWithFacets(request, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Quick search (simplified version)
     * GET /api/search?q=keyword
     */
    @GetMapping
    public ResponseEntity<SearchResultResponse> quickSearch(
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @RequestParam(required = false, defaultValue = "recent") String sortBy) {

        User currentUser = authService.getCurrentUser();

        DocumentSearchRequest request = new DocumentSearchRequest();
        request.setQuery(query);
        request.setPage(page);
        request.setSize(size);
        request.setSortBy(sortBy);
        request.setIncludeArchived(false);

        SearchResultResponse response = searchService.advancedSearch(request, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Search by tags
     * GET /api/search/by-tags?tags=java,spring&matchAll=false
     */
    @GetMapping("/by-tags")
    public ResponseEntity<SearchResultResponse> searchByTags(
            @RequestParam List<String> tags,
            @RequestParam(required = false, defaultValue = "false") Boolean matchAll,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {

        User currentUser = authService.getCurrentUser();

        DocumentSearchRequest request = new DocumentSearchRequest();
        request.setTags(tags);
        request.setMatchAllTags(matchAll);
        request.setPage(page);
        request.setSize(size);
        request.setSortBy("recent");

        SearchResultResponse response = searchService.advancedSearch(request, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Search user's favorited documents
     * GET /api/search/favorites
     */
    @GetMapping("/favorites")
    public ResponseEntity<SearchResultResponse> searchFavorites(
            @RequestParam(required = false) String query,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {

        User currentUser = authService.getCurrentUser();

        DocumentSearchRequest request = new DocumentSearchRequest();
        request.setQuery(query);
        request.setOnlyFavorited(true);
        request.setPage(page);
        request.setSize(size);
        request.setSortBy("recent");

        SearchResultResponse response = searchService.advancedSearch(request, currentUser.getId());
        return ResponseEntity.ok(response);
    }
}

