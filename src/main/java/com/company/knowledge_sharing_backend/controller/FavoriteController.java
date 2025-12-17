package com.company.knowledge_sharing_backend.controller;

import com.company.knowledge_sharing_backend.dto.response.FavoriteResponse;
import com.company.knowledge_sharing_backend.dto.response.MessageResponse;
import com.company.knowledge_sharing_backend.entity.User;
import com.company.knowledge_sharing_backend.service.AuthService;
import com.company.knowledge_sharing_backend.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private AuthService authService;

    /**
     * Add document to favorites
     * POST /api/favorites/documents/{documentId}
     */
    @PostMapping("/documents/{documentId}")
    public ResponseEntity<FavoriteResponse> addFavorite(@PathVariable Long documentId) {
        User currentUser = authService.getCurrentUser();
        FavoriteResponse response = favoriteService.addFavorite(documentId, currentUser.getId());

        return ResponseEntity.ok(response);
    }

    /**
     * Remove document from favorites
     * DELETE /api/favorites/documents/{documentId}
     */
    @DeleteMapping("/documents/{documentId}")
    public ResponseEntity<MessageResponse> removeFavorite(@PathVariable Long documentId) {
        User currentUser = authService.getCurrentUser();
        favoriteService.removeFavorite(documentId, currentUser.getId());

        return ResponseEntity.ok(new MessageResponse("Removed from favorites"));
    }

    /**
     * Get current user's favorites
     * GET /api/favorites
     */
    @GetMapping
    public ResponseEntity<List<FavoriteResponse>> getMyFavorites() {
        User currentUser = authService.getCurrentUser();
        List<FavoriteResponse> favorites = favoriteService.getUserFavorites(currentUser.getId());

        return ResponseEntity.ok(favorites);
    }

    /**
     * Check if document is favorited by current user
     * GET /api/favorites/documents/{documentId}/check
     */
    @GetMapping("/documents/{documentId}/check")
    public ResponseEntity<Map<String, Boolean>> checkFavorite(@PathVariable Long documentId) {
        User currentUser = authService.getCurrentUser();
        boolean isFavorited = favoriteService.isFavorited(documentId, currentUser.getId());

        Map<String, Boolean> response = new HashMap<>();
        response.put("isFavorited", isFavorited);

        return ResponseEntity.ok(response);
    }

    /**
     * Get favorite count for a document
     * GET /api/favorites/documents/{documentId}/count
     */
    @GetMapping("/documents/{documentId}/count")
    public ResponseEntity<Map<String, Integer>> getFavoriteCount(@PathVariable Long documentId) {
        Integer count = favoriteService.getFavoriteCount(documentId);

        Map<String, Integer> response = new HashMap<>();
        response.put("count", count);

        return ResponseEntity.ok(response);
    }
}

