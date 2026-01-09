package com.company.knowledge_sharing_backend.controller;

import com.company.knowledge_sharing_backend.dto.request.DocumentSearchRequest;
import com.company.knowledge_sharing_backend.dto.response.DocumentResponse;
import com.company.knowledge_sharing_backend.dto.response.SearchResultResponse;
import com.company.knowledge_sharing_backend.entity.User;
import com.company.knowledge_sharing_backend.service.AuthService;
import com.company.knowledge_sharing_backend.service.SearchService;
import com.company.knowledge_sharing_backend.service.SemanticSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/search")
@Tag(name = "Search", description = "Document search endpoints including AI semantic search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @Autowired
    private SemanticSearchService semanticSearchService;

    @Autowired
    private AuthService authService;

    /**
     * UNIFIED SEARCH ENDPOINT - All searches use this ONE endpoint
     * GET /api/search
     *
     * ========================================================================
     * WHY ONE ENDPOINT INSTEAD OF MULTIPLE?
     * ========================================================================
     * ✅ All parameters are optional - simple searches work fine
     * ✅ No need to remember multiple endpoints
     * ✅ Frontend only needs ONE method
     * ✅ Easier to maintain and test
     * ✅ RESTful best practice: use query params for filtering, not different endpoints
     *
     * ========================================================================
     * USAGE EXAMPLES:
     * ========================================================================
     * Simple searches:
     *   GET /api/search?q=angular
     *   GET /api/search?tags=Java,Spring
     *   GET /api/search?sharingLevel=PUBLIC
     *   GET /api/search?fileType=PDF
     *
     * Advanced searches:
     *   GET /api/search?q=spring&minRating=4.0&fromDate=2025-01-01T00:00:00
     *   GET /api/search?ownerUsername=admin&fileType=PDF&sortBy=rating
     *   GET /api/search?tags=Angular,TypeScript&matchAllTags=true
     *
     * Favorites search:
     *   GET /api/search?onlyFavorited=true
     *
     * Tag-based search:
     *   GET /api/search?tags=Java,Spring,Backend&matchAllTags=false
     *
     * ========================================================================
     * ALL PARAMETERS (ALL OPTIONAL):
     * ========================================================================
     * @param q              Search keyword (searches in title, summary, content)
     * @param tags           Comma-separated tag names (e.g., "Java,Spring,Backend")
     * @param matchAllTags   true=AND logic (must have ALL tags), false=OR logic (default: false)
     * @param sharingLevel   PRIVATE, GROUP, PUBLIC
     * @param fileType       PDF, DOCX, XLSX, PPTX, TXT, IMAGE
     * @param ownerId        Filter by owner user ID
     * @param ownerUsername  Filter by owner username
     * @param groupIds       Comma-separated group IDs (e.g., "1,2,3")
     * @param minRating      Minimum average rating (0.0-5.0)
     * @param maxRating      Maximum average rating (0.0-5.0)
     * @param fromDate       Created after this date (ISO-8601: 2025-12-01T00:00:00)
     * @param toDate         Created before this date (ISO-8601: 2025-12-31T23:59:59)
     * @param sortBy         recent, oldest, title, rating, popular, relevance (default: recent)
     * @param sortOrder      asc, desc (default: desc)
     * @param page           Page number, zero-based (default: 0)
     * @param size           Page size (default: 10, max: 100)
     * @param includeArchived Include archived documents (default: false)
     * @param onlyFavorited  Search only in user's favorites (default: false)
     *
     * @return SearchResultResponse with documents, pagination, and optional facets
     */
    @GetMapping
    public ResponseEntity<SearchResultResponse> search(
            @RequestParam(value = "q", required = false) String q,
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
        request.setQuery(q);
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
     * AI SEMANTIC SEARCH - Search documents using AI semantic understanding
     * GET /api/search/semantic
     *
     * ========================================================================
     * WHAT IS SEMANTIC SEARCH?
     * ========================================================================
     * Unlike keyword search which only matches exact words, semantic search
     * understands the MEANING of your query and finds documents with similar
     * concepts, even if they don't contain the exact keywords.
     *
     * Example:
     *   Query: "machine learning best practices"
     *   Will find documents about:
     *   - AI model training
     *   - Neural network optimization
     *   - Deep learning techniques
     *   Even if they don't contain the exact phrase "machine learning"
     *
     * ========================================================================
     * HOW IT WORKS:
     * ========================================================================
     * 1. Your query is converted to an embedding vector using Gemini AI
     * 2. Each document has a pre-computed embedding vector
     * 3. We calculate cosine similarity between query and all documents
     * 4. Return documents ranked by similarity score (0.0-1.0)
     *
     * ========================================================================
     * WHEN TO USE:
     * ========================================================================
     * ✅ Exploratory search - "What documents are related to X?"
     * ✅ Conceptual search - "Find documents about Y concept"
     * ✅ When you don't know exact keywords
     * ✅ Cross-language understanding
     *
     * @param query Search query (natural language)
     * @param limit Maximum number of results (default: 10, max: 50)
     * @return List of documents ranked by semantic similarity with scores
     */
    @Operation(
        summary = "AI Semantic Search",
        description = "Search documents using AI semantic understanding. Returns documents ranked by meaning similarity, not just keyword matching."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid query"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "AI service error")
    })
    @GetMapping("/semantic")
    public ResponseEntity<List<DocumentResponse>> semanticSearch(
            @Parameter(description = "Search query (natural language)", example = "machine learning best practices")
            @RequestParam("q") String query,
            @Parameter(description = "Maximum number of results (max: 50)", example = "10")
            @RequestParam(value = "limit", defaultValue = "10") Integer limit) {

        User currentUser = authService.getCurrentUser();

        // Validate limit
        if (limit < 1 || limit > 50) {
            limit = 10;
        }

        List<DocumentResponse> results = semanticSearchService.semanticSearch(query, currentUser.getId(), limit);
        return ResponseEntity.ok(results);
    }
}

