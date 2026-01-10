package com.company.knowledge_sharing_backend.controller;

import com.company.knowledge_sharing_backend.dto.request.CreateCommentRequest;
import com.company.knowledge_sharing_backend.dto.request.UpdateCommentRequest;
import com.company.knowledge_sharing_backend.dto.response.CommentResponse;
import com.company.knowledge_sharing_backend.dto.response.MessageResponse;
import com.company.knowledge_sharing_backend.entity.User;
import com.company.knowledge_sharing_backend.service.AuthService;
import com.company.knowledge_sharing_backend.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documents/{documentId}/comments")
@Tag(name = "Comments", description = "Comment management endpoints - create, read, update, delete, like, reply")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private AuthService authService;

    /**
     * POST /api/documents/{documentId}/comments
     * Create a new comment or reply
     */
    @Operation(
        summary = "Create comment or reply",
        description = "Create a new comment on a document. If parentCommentId is provided, creates a reply to that comment."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Comment created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Document or parent comment not found")
    })
    @PostMapping
    public ResponseEntity<CommentResponse> createComment(
            @Parameter(description = "Document ID", example = "1")
            @PathVariable Long documentId,
            @Valid @RequestBody CreateCommentRequest request) {

        User currentUser = authService.getCurrentUser();
        CommentResponse response = commentService.createComment(documentId, request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/documents/{documentId}/comments
     * Get all top-level comments for a document with pagination
     */
    @Operation(
        summary = "Get comments for document",
        description = "Get all top-level comments (not replies) for a document with pagination"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Comments retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Document not found")
    })
    @GetMapping
    public ResponseEntity<Page<CommentResponse>> getComments(
            @Parameter(description = "Document ID", example = "1")
            @PathVariable Long documentId,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        User currentUser = authService.getCurrentUser();
        Page<CommentResponse> comments = commentService.getCommentsByDocument(
                documentId, page, size, currentUser.getId());
        return ResponseEntity.ok(comments);
    }

    /**
     * GET /api/documents/{documentId}/comments/count
     * Get comment count for a document
     */
    @Operation(
        summary = "Get comment count",
        description = "Get total number of comments (including replies) for a document"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Count retrieved successfully")
    })
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getCommentCount(
            @Parameter(description = "Document ID", example = "1")
            @PathVariable Long documentId) {

        Long count = commentService.getCommentCountByDocument(documentId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * GET /api/documents/{documentId}/comments/{commentId}
     * Get a specific comment with its replies
     */
    @Operation(
        summary = "Get comment by ID",
        description = "Get a specific comment with all its replies"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Comment retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Comment not found")
    })
    @GetMapping("/{commentId}")
    public ResponseEntity<CommentResponse> getComment(
            @Parameter(description = "Document ID", example = "1")
            @PathVariable Long documentId,
            @Parameter(description = "Comment ID", example = "5")
            @PathVariable Long commentId) {

        User currentUser = authService.getCurrentUser();
        CommentResponse comment = commentService.getCommentById(commentId, currentUser.getId());
        return ResponseEntity.ok(comment);
    }

    /**
     * GET /api/documents/{documentId}/comments/{commentId}/replies
     * Get all replies for a comment
     */
    @Operation(
        summary = "Get replies for comment",
        description = "Get all replies for a specific comment"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Replies retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Comment not found")
    })
    @GetMapping("/{commentId}/replies")
    public ResponseEntity<List<CommentResponse>> getReplies(
            @Parameter(description = "Document ID", example = "1")
            @PathVariable Long documentId,
            @Parameter(description = "Comment ID", example = "5")
            @PathVariable Long commentId) {

        User currentUser = authService.getCurrentUser();
        List<CommentResponse> replies = commentService.getRepliesByComment(commentId, currentUser.getId());
        return ResponseEntity.ok(replies);
    }

    /**
     * PUT /api/documents/{documentId}/comments/{commentId}
     * Update a comment (only owner can update)
     */
    @Operation(
        summary = "Update comment",
        description = "Update comment content. Only the comment owner can update it."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Comment updated successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Not comment owner"),
        @ApiResponse(responseCode = "404", description = "Comment not found")
    })
    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @Parameter(description = "Document ID", example = "1")
            @PathVariable Long documentId,
            @Parameter(description = "Comment ID", example = "5")
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentRequest request) {

        User currentUser = authService.getCurrentUser();
        CommentResponse response = commentService.updateComment(commentId, request, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/documents/{documentId}/comments/{commentId}
     * Delete a comment (only owner can delete)
     */
    @Operation(
        summary = "Delete comment",
        description = "Delete a comment. Only the comment owner can delete it. Deletes all replies and likes as well."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Comment deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Not comment owner"),
        @ApiResponse(responseCode = "404", description = "Comment not found")
    })
    @DeleteMapping("/{commentId}")
    public ResponseEntity<MessageResponse> deleteComment(
            @Parameter(description = "Document ID", example = "1")
            @PathVariable Long documentId,
            @Parameter(description = "Comment ID", example = "5")
            @PathVariable Long commentId) {

        User currentUser = authService.getCurrentUser();
        commentService.deleteComment(commentId, currentUser.getId());
        return ResponseEntity.ok(new MessageResponse("Comment deleted successfully"));
    }

    /**
     * POST /api/documents/{documentId}/comments/{commentId}/like
     * Toggle like on a comment
     */
    @Operation(
        summary = "Toggle like on comment",
        description = "Like or unlike a comment. If already liked, will unlike. If not liked, will like."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Like toggled successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Comment not found")
    })
    @PostMapping("/{commentId}/like")
    public ResponseEntity<Map<String, Boolean>> toggleLike(
            @Parameter(description = "Document ID", example = "1")
            @PathVariable Long documentId,
            @Parameter(description = "Comment ID", example = "5")
            @PathVariable Long commentId) {

        User currentUser = authService.getCurrentUser();
        boolean isLiked = commentService.toggleLike(commentId, currentUser.getId());
        return ResponseEntity.ok(Map.of("isLiked", isLiked));
    }
}

