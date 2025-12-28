package com.company.knowledge_sharing_backend.controller;

import com.company.knowledge_sharing_backend.dto.request.DocumentRequest;
import com.company.knowledge_sharing_backend.dto.request.DocumentSearchRequest;
import com.company.knowledge_sharing_backend.dto.response.DocumentDetailResponse;
import com.company.knowledge_sharing_backend.dto.response.DocumentResponse;
import com.company.knowledge_sharing_backend.dto.response.DocumentVersionResponse;
import com.company.knowledge_sharing_backend.dto.response.MessageResponse;
import com.company.knowledge_sharing_backend.entity.User;
import com.company.knowledge_sharing_backend.service.AuthService;
import com.company.knowledge_sharing_backend.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@Tag(name = "Documents", description = "Document management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private AuthService authService;

    /**
     * Create new document
     * POST /api/documents
     */
    @Operation(
        summary = "Upload new document",
        description = "Upload a new document with metadata and file"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document uploaded successfully",
                content = @Content(schema = @Schema(implementation = DocumentResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid file or request",
                content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
                content = @Content)
    })
    @PostMapping
    public ResponseEntity<DocumentResponse> createDocument(
            @Parameter(description = "Document metadata")
            @RequestPart("data") @Valid DocumentRequest request,
            @Parameter(description = "Document file (PDF, DOC, or Image)")
            @RequestPart("file") MultipartFile file) {

        User currentUser = authService.getCurrentUser();
        DocumentResponse response = documentService.createDocument(request, file, currentUser.getId());

        return ResponseEntity.ok(response);
    }

    /**
     * Update document
     * PUT /api/documents/{id}
     */
    @Operation(
        summary = "Update document",
        description = "Update document metadata and optionally upload new file version"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document updated successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Not document owner"),
        @ApiResponse(responseCode = "404", description = "Document not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<DocumentResponse> updateDocument(
            @Parameter(description = "Document ID")
            @PathVariable Long id,
            @RequestPart("data") @Valid DocumentRequest request,
            @Parameter(description = "New file version (optional)")
            @RequestPart(value = "file", required = false) MultipartFile file) {

        User currentUser = authService.getCurrentUser();
        DocumentResponse response = documentService.updateDocument(id, request, file, currentUser.getId());

        return ResponseEntity.ok(response);
    }

    /**
     * Get document by ID
     * GET /api/documents/{id}
     */
    @Operation(
        summary = "Get document by ID",
        description = "Retrieve document details including metadata and file information"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Document not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<DocumentDetailResponse> getDocument(
            @Parameter(description = "Document ID")
            @PathVariable Long id) {
        User currentUser = authService.getCurrentUser();
        DocumentDetailResponse response = documentService.getDocumentById(id, currentUser.getId());

        return ResponseEntity.ok(response);
    }

    /**
     * Archive document
     * DELETE /api/documents/{id}/archive
     */
    @DeleteMapping("/{id}/archive")
    public ResponseEntity<MessageResponse> archiveDocument(@PathVariable Long id) {
        User currentUser = authService.getCurrentUser();
        documentService.archiveDocument(id, currentUser.getId());

        return ResponseEntity.ok(new MessageResponse("Document archived successfully"));
    }

    /**
     * Delete document permanently
     * DELETE /api/documents/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteDocument(@PathVariable Long id) {
        User currentUser = authService.getCurrentUser();
        documentService.deleteDocument(id, currentUser.getId());

        return ResponseEntity.ok(new MessageResponse("Document deleted successfully"));
    }

    /**
     * Get recent documents
     * GET /api/documents?sort=recent&limit=5
     */
    @GetMapping
    public ResponseEntity<List<DocumentResponse>> getDocuments(
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String owner) {

        User currentUser = authService.getCurrentUser();
        List<DocumentResponse> documents;

        if (sort != null && sort.equals("recent")) {
            documents = documentService.getRecentDocuments(limit != null ? limit : 5, currentUser.getId());
        } else if (sort != null && sort.equals("popular")) {
            documents = documentService.getPopularDocuments(limit != null ? limit : 5, currentUser.getId());
        } else if (owner != null && owner.equals("me")) {
            documents = documentService.getUserDocuments(currentUser.getId(), limit != null ? limit : 10);
        } else {
            // Default: recent documents
            documents = documentService.getRecentDocuments(10, currentUser.getId());
        }

        return ResponseEntity.ok(documents);
    }


    /**
     * Search suggestions (autocomplete)
     * GET /api/documents/search/suggestions?q=keyword
     */
    @GetMapping("/search/suggestions")
    public ResponseEntity<List<String>> searchSuggestions(
            @RequestParam(value = "q", required = false) String query) {

        // Return top 10 document titles matching the query
        User currentUser = authService.getCurrentUser();

        DocumentSearchRequest request = new DocumentSearchRequest();
        request.setQuery(query);
        request.setPage(0);
        request.setSize(10);

        List<DocumentResponse> documents = documentService.searchDocuments(request, currentUser.getId());
        List<String> suggestions = documents.stream()
                .map(DocumentResponse::getTitle)
                .toList();

        return ResponseEntity.ok(suggestions);
    }

    /**
     * Get document versions
     * GET /api/documents/{id}/versions
     */
    @GetMapping("/{id}/versions")
    public ResponseEntity<List<DocumentVersionResponse>> getDocumentVersions(@PathVariable Long id) {
        User currentUser = authService.getCurrentUser();
        List<DocumentVersionResponse> versions = documentService.getDocumentVersions(id, currentUser.getId());

        return ResponseEntity.ok(versions);
    }
}

