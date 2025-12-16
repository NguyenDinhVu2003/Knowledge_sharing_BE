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
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private AuthService authService;

    /**
     * Create new document
     * POST /api/documents
     */
    @PostMapping
    public ResponseEntity<DocumentResponse> createDocument(
            @RequestPart("data") @Valid DocumentRequest request,
            @RequestPart("file") MultipartFile file) {

        User currentUser = authService.getCurrentUser();
        DocumentResponse response = documentService.createDocument(request, file, currentUser.getId());

        return ResponseEntity.ok(response);
    }

    /**
     * Update document
     * PUT /api/documents/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<DocumentResponse> updateDocument(
            @PathVariable Long id,
            @RequestPart("data") @Valid DocumentRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        User currentUser = authService.getCurrentUser();
        DocumentResponse response = documentService.updateDocument(id, request, file, currentUser.getId());

        return ResponseEntity.ok(response);
    }

    /**
     * Get document by ID
     * GET /api/documents/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<DocumentDetailResponse> getDocument(@PathVariable Long id) {
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
     * Search documents
     * GET /api/documents/search?query=...&tags=...
     */
    @GetMapping("/search")
    public ResponseEntity<List<DocumentResponse>> searchDocuments(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(required = false) String sharingLevel,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            @RequestParam(required = false) String sortBy) {

        User currentUser = authService.getCurrentUser();

        DocumentSearchRequest request = new DocumentSearchRequest();
        request.setQuery(query);
        request.setTags(tags);
        request.setSharingLevel(sharingLevel);
        request.setPage(page);
        request.setSize(size);
        request.setSortBy(sortBy);

        List<DocumentResponse> documents = documentService.searchDocuments(request, currentUser.getId());

        return ResponseEntity.ok(documents);
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

