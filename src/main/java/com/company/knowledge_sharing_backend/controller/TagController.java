package com.company.knowledge_sharing_backend.controller;

import com.company.knowledge_sharing_backend.dto.request.TagRequest;
import com.company.knowledge_sharing_backend.dto.response.MessageResponse;
import com.company.knowledge_sharing_backend.dto.response.TagResponse;
import com.company.knowledge_sharing_backend.service.TagService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    @Autowired
    private TagService tagService;

    /**
     * Create new tag
     * POST /api/tags
     */
    @PostMapping
    public ResponseEntity<TagResponse> createTag(@Valid @RequestBody TagRequest request) {
        TagResponse response = tagService.createTag(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update tag
     * PUT /api/tags/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<TagResponse> updateTag(
            @PathVariable Long id,
            @Valid @RequestBody TagRequest request) {

        TagResponse response = tagService.updateTag(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete tag
     * DELETE /api/tags/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return ResponseEntity.ok(new MessageResponse("Tag deleted successfully"));
    }

    /**
     * Get tag by ID
     * GET /api/tags/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<TagResponse> getTag(@PathVariable Long id) {
        TagResponse response = tagService.getTagById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all tags
     * GET /api/tags
     */
    @GetMapping
    public ResponseEntity<List<TagResponse>> getAllTags() {
        List<TagResponse> tags = tagService.getAllTags();
        return ResponseEntity.ok(tags);
    }

    /**
     * Get popular tags
     * GET /api/tags/popular?limit=10
     */
    @GetMapping("/popular")
    public ResponseEntity<List<TagResponse>> getPopularTags(
            @RequestParam(defaultValue = "10") int limit) {

        List<TagResponse> tags = tagService.getPopularTags(limit);
        return ResponseEntity.ok(tags);
    }

    /**
     * Search tags
     * GET /api/tags/search?keyword=angular
     */
    @GetMapping("/search")
    public ResponseEntity<List<TagResponse>> searchTags(@RequestParam String keyword) {
        List<TagResponse> tags = tagService.searchTags(keyword);
        return ResponseEntity.ok(tags);
    }

    /**
     * Get tags with document count
     * GET /api/tags/with-counts
     */
    @GetMapping("/with-counts")
    public ResponseEntity<List<TagResponse>> getTagsWithCounts() {
        List<TagResponse> tags = tagService.getTagsWithDocumentCount();
        return ResponseEntity.ok(tags);
    }
}

