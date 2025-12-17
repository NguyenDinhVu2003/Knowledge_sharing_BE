package com.company.knowledge_sharing_backend.controller;

import com.company.knowledge_sharing_backend.dto.request.RatingRequest;
import com.company.knowledge_sharing_backend.dto.response.DocumentRatingStats;
import com.company.knowledge_sharing_backend.dto.response.MessageResponse;
import com.company.knowledge_sharing_backend.dto.response.RatingResponse;
import com.company.knowledge_sharing_backend.entity.User;
import com.company.knowledge_sharing_backend.service.AuthService;
import com.company.knowledge_sharing_backend.service.RatingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    @Autowired
    private RatingService ratingService;

    @Autowired
    private AuthService authService;

    /**
     * Rate a document
     * POST /api/ratings/documents/{documentId}
     */
    @PostMapping("/documents/{documentId}")
    public ResponseEntity<RatingResponse> rateDocument(
            @PathVariable Long documentId,
            @Valid @RequestBody RatingRequest request) {

        User currentUser = authService.getCurrentUser();
        RatingResponse response = ratingService.rateDocument(documentId, request, currentUser.getId());

        return ResponseEntity.ok(response);
    }

    /**
     * Update rating
     * PUT /api/ratings/documents/{documentId}
     */
    @PutMapping("/documents/{documentId}")
    public ResponseEntity<RatingResponse> updateRating(
            @PathVariable Long documentId,
            @Valid @RequestBody RatingRequest request) {

        User currentUser = authService.getCurrentUser();
        RatingResponse response = ratingService.updateRating(documentId, request, currentUser.getId());

        return ResponseEntity.ok(response);
    }

    /**
     * Delete rating
     * DELETE /api/ratings/documents/{documentId}
     */
    @DeleteMapping("/documents/{documentId}")
    public ResponseEntity<MessageResponse> deleteRating(@PathVariable Long documentId) {
        User currentUser = authService.getCurrentUser();
        ratingService.deleteRating(documentId, currentUser.getId());

        return ResponseEntity.ok(new MessageResponse("Rating deleted successfully"));
    }

    /**
     * Get current user's rating for a document
     * GET /api/ratings/documents/{documentId}/my-rating
     */
    @GetMapping("/documents/{documentId}/my-rating")
    public ResponseEntity<RatingResponse> getMyRating(@PathVariable Long documentId) {
        User currentUser = authService.getCurrentUser();
        RatingResponse response = ratingService.getUserRating(documentId, currentUser.getId());

        if (response == null) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Get all ratings for a document
     * GET /api/ratings/documents/{documentId}
     */
    @GetMapping("/documents/{documentId}")
    public ResponseEntity<List<RatingResponse>> getDocumentRatings(@PathVariable Long documentId) {
        List<RatingResponse> ratings = ratingService.getDocumentRatings(documentId);
        return ResponseEntity.ok(ratings);
    }

    /**
     * Get rating statistics for a document
     * GET /api/ratings/documents/{documentId}/stats
     */
    @GetMapping("/documents/{documentId}/stats")
    public ResponseEntity<DocumentRatingStats> getRatingStats(@PathVariable Long documentId) {
        DocumentRatingStats stats = ratingService.getDocumentRatingStats(documentId);
        return ResponseEntity.ok(stats);
    }

    /**
     * Get current user's all ratings
     * GET /api/ratings/my-ratings
     */
    @GetMapping("/my-ratings")
    public ResponseEntity<List<RatingResponse>> getMyRatings() {
        User currentUser = authService.getCurrentUser();
        List<RatingResponse> ratings = ratingService.getUserRatings(currentUser.getId());

        return ResponseEntity.ok(ratings);
    }
}

