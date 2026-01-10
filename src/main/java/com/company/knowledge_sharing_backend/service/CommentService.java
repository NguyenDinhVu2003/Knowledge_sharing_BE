package com.company.knowledge_sharing_backend.service;

import com.company.knowledge_sharing_backend.dto.request.CreateCommentRequest;
import com.company.knowledge_sharing_backend.dto.request.UpdateCommentRequest;
import com.company.knowledge_sharing_backend.dto.response.CommentResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CommentService {

    /**
     * Create a new comment on a document
     */
    CommentResponse createComment(Long documentId, CreateCommentRequest request, Long userId);

    /**
     * Get all top-level comments for a document with pagination
     */
    Page<CommentResponse> getCommentsByDocument(Long documentId, int page, int size, Long currentUserId);

    /**
     * Get all replies for a comment
     */
    List<CommentResponse> getRepliesByComment(Long commentId, Long currentUserId);

    /**
     * Get comment by ID
     */
    CommentResponse getCommentById(Long commentId, Long currentUserId);

    /**
     * Update a comment
     */
    CommentResponse updateComment(Long commentId, UpdateCommentRequest request, Long userId);

    /**
     * Delete a comment
     */
    void deleteComment(Long commentId, Long userId);

    /**
     * Like a comment
     */
    void likeComment(Long commentId, Long userId);

    /**
     * Unlike a comment
     */
    void unlikeComment(Long commentId, Long userId);

    /**
     * Toggle like on a comment
     */
    boolean toggleLike(Long commentId, Long userId);

    /**
     * Get comment count for a document
     */
    Long getCommentCountByDocument(Long documentId);
}

