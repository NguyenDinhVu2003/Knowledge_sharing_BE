package com.company.knowledge_sharing_backend.controller;

import com.company.knowledge_sharing_backend.dto.request.UpdateUserInterestsRequest;
import com.company.knowledge_sharing_backend.dto.response.TagWithCountResponse;
import com.company.knowledge_sharing_backend.dto.response.UserProfileResponse;
import com.company.knowledge_sharing_backend.entity.User;
import com.company.knowledge_sharing_backend.service.AuthService;
import com.company.knowledge_sharing_backend.service.UserInterestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profile")
@Tag(name = "User Profile", description = "User profile and interests management endpoints")
public class ProfileController {

    @Autowired
    private UserInterestService userInterestService;

    @Autowired
    private AuthService authService;

    /**
     * GET /api/profile
     * Get current user profile with interests and statistics
     *
     * Returns:
     * - User basic info (id, username, email, role)
     * - Statistics (totalDocuments, totalFavorites, totalRatings, totalInterests)
     * - List of interests (tags user is following) with document count
     */
    @Operation(
        summary = "Get user profile",
        description = "Get current user profile with interests and statistics"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping
    public ResponseEntity<UserProfileResponse> getProfile() {
        User currentUser = authService.getCurrentUser();
        UserProfileResponse profile = userInterestService.getUserProfile(currentUser.getId());
        return ResponseEntity.ok(profile);
    }

    /**
     * GET /api/profile/interests
     * Get current user interests (tag IDs only)
     *
     * Returns: List of tag IDs that user is interested in
     */
    @Operation(
        summary = "Get user interest IDs",
        description = "Get list of tag IDs that current user is interested in"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Interests retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/interests")
    public ResponseEntity<List<Long>> getUserInterests() {
        User currentUser = authService.getCurrentUser();
        List<Long> interestIds = userInterestService.getUserInterestIds(currentUser.getId());
        return ResponseEntity.ok(interestIds);
    }

    /**
     * GET /api/profile/tags/popular
     * Get popular tags with document count and interest status
     *
     * Query params:
     * - limit: Number of tags to return (default: 10, max: 50)
     *
     * Returns: List of popular tags sorted by document count
     * Each tag includes:
     * - id, name, description
     * - documentCount: Number of documents with this tag
     * - isInterested: Whether current user is interested in this tag
     */
    @Operation(
        summary = "Get popular tags",
        description = "Get most popular tags sorted by document count, with current user interest status"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Popular tags retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/tags/popular")
    public ResponseEntity<List<TagWithCountResponse>> getPopularTags(
            @Parameter(description = "Maximum number of tags to return (max: 50)", example = "10")
            @RequestParam(value = "limit", defaultValue = "10") Integer limit) {

        User currentUser = authService.getCurrentUser();

        // Validate limit
        if (limit < 1 || limit > 50) {
            limit = 10;
        }

        List<TagWithCountResponse> tags = userInterestService.getPopularTags(currentUser.getId(), limit);
        return ResponseEntity.ok(tags);
    }

    /**
     * GET /api/profile/tags
     * Get all available tags with document count and interest status
     *
     * Query params:
     * - search: Search keyword for tag name or description (optional)
     *
     * Returns: List of all tags sorted by document count
     * Each tag includes:
     * - id, name, description
     * - documentCount: Number of documents with this tag
     * - isInterested: Whether current user is interested in this tag
     *
     * Frontend can use this for:
     * - Showing all available tags
     * - Real-time search with debounce
     * - Tag selection interface
     */
    @Operation(
        summary = "Get all tags",
        description = "Get all available tags with document count and interest status. Supports search."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tags retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/tags")
    public ResponseEntity<List<TagWithCountResponse>> getAllTags(
            @Parameter(description = "Search keyword for tag name or description", example = "Java")
            @RequestParam(value = "search", required = false) String search) {

        User currentUser = authService.getCurrentUser();
        List<TagWithCountResponse> tags = userInterestService.getAllTags(currentUser.getId(), search);
        return ResponseEntity.ok(tags);
    }

    /**
     * PUT /api/profile/interests
     * Update user interests (replace all existing interests)
     *
     * Request body:
     * {
     *   "tagIds": [1, 2, 3, 5, 8]
     * }
     *
     * Validation:
     * - At least 1 tag ID is required
     * - All tag IDs must exist
     *
     * Logic:
     * 1. Remove all existing interests
     * 2. Add new interests from tagIds
     * 3. Return updated interests with document counts
     *
     * Returns: List of updated interests
     */
    @Operation(
        summary = "Update user interests",
        description = "Update user interests by replacing all existing interests with new ones. At least 1 tag is required."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Interests updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request - at least 1 tag required"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Tag not found")
    })
    @PutMapping("/interests")
    public ResponseEntity<List<TagWithCountResponse>> updateInterests(
            @Valid @RequestBody UpdateUserInterestsRequest request) {

        User currentUser = authService.getCurrentUser();
        List<TagWithCountResponse> updatedInterests = userInterestService.updateUserInterestsByIds(
                currentUser.getId(),
                request.getTagIds());
        return ResponseEntity.ok(updatedInterests);
    }

    /**
     * POST /api/profile/interests/{tagId}
     * Add a single interest (toggle on)
     *
     * Path param:
     * - tagId: ID of the tag to add
     *
     * Logic:
     * - If already interested, do nothing (idempotent)
     * - Otherwise, add the interest
     *
     * Returns: 200 OK
     */
    @Operation(
        summary = "Add single interest",
        description = "Add a single tag to user interests. Idempotent operation."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Interest added successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Tag not found")
    })
    @PostMapping("/interests/{tagId}")
    public ResponseEntity<Void> addInterest(
            @Parameter(description = "Tag ID to add", example = "1")
            @PathVariable Long tagId) {

        User currentUser = authService.getCurrentUser();
        userInterestService.addInterestById(currentUser.getId(), tagId);
        return ResponseEntity.ok().build();
    }

    /**
     * DELETE /api/profile/interests/{tagId}
     * Remove a single interest (toggle off)
     *
     * Path param:
     * - tagId: ID of the tag to remove
     *
     * Logic:
     * - Remove the interest if it exists
     * - If not exists, throw 404
     *
     * Returns: 204 No Content
     */
    @Operation(
        summary = "Remove single interest",
        description = "Remove a single tag from user interests"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Interest removed successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Interest not found")
    })
    @DeleteMapping("/interests/{tagId}")
    public ResponseEntity<Void> removeInterest(
            @Parameter(description = "Tag ID to remove", example = "1")
            @PathVariable Long tagId) {

        User currentUser = authService.getCurrentUser();
        userInterestService.removeInterestById(currentUser.getId(), tagId);
        return ResponseEntity.noContent().build();
    }
}

