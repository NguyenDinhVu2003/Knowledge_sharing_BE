package com.company.knowledge_sharing_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/cache")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Cache Management", description = "Admin endpoints for Redis cache management")
@SecurityRequirement(name = "bearerAuth")
public class CacheController {

    @Autowired
    private CacheManager cacheManager;

    /**
     * Clear all caches
     * DELETE /api/cache/clear
     */
    @Operation(
        summary = "Clear all caches",
        description = "Clear all Redis caches (Admin only) - Use this to fix cache issues"
    )
    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, String>> clearAllCaches() {
        cacheManager.getCacheNames()
                .forEach(cacheName -> Objects.requireNonNull(cacheManager.getCache(cacheName)).clear());

        Map<String, String> response = new HashMap<>();
        response.put("message", "All caches cleared successfully");
        response.put("caches", String.join(", ", cacheManager.getCacheNames()));
        response.put("timestamp", java.time.LocalDateTime.now().toString());

        return ResponseEntity.ok(response);
    }

    /**
     * Clear specific cache
     * DELETE /api/cache/clear/{cacheName}
     */
    @Operation(
        summary = "Clear specific cache",
        description = "Clear a specific Redis cache by name"
    )
    @DeleteMapping("/clear/{cacheName}")
    public ResponseEntity<Map<String, String>> clearCache(
            @Parameter(description = "Cache name to clear")
            @PathVariable String cacheName) {
        var cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            Map<String, String> response = new HashMap<>();
            response.put("message", "Cache '" + cacheName + "' cleared successfully");
            return ResponseEntity.ok(response);
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Cache '" + cacheName + "' not found");
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * List all cache names
     * GET /api/cache/list
     */
    @Operation(
        summary = "List all caches",
        description = "Get list of all available cache names"
    )
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> listCaches() {
        Map<String, Object> response = new HashMap<>();
        response.put("caches", cacheManager.getCacheNames());
        response.put("count", cacheManager.getCacheNames().size());
        return ResponseEntity.ok(response);
    }
}

