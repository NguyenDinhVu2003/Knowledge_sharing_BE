package com.company.knowledge_sharing_backend.service.impl;

import com.company.knowledge_sharing_backend.dto.request.CreateCommentRequest;
import com.company.knowledge_sharing_backend.dto.request.UpdateCommentRequest;
import com.company.knowledge_sharing_backend.dto.response.CommentResponse;
import com.company.knowledge_sharing_backend.entity.Comment;
import com.company.knowledge_sharing_backend.entity.CommentLike;
import com.company.knowledge_sharing_backend.entity.Document;
import com.company.knowledge_sharing_backend.entity.User;
import com.company.knowledge_sharing_backend.exception.ResourceNotFoundException;
import com.company.knowledge_sharing_backend.exception.UnauthorizedException;
import com.company.knowledge_sharing_backend.repository.CommentLikeRepository;
import com.company.knowledge_sharing_backend.repository.CommentRepository;
import com.company.knowledge_sharing_backend.repository.DocumentRepository;
import com.company.knowledge_sharing_backend.repository.UserRepository;
import com.company.knowledge_sharing_backend.service.CommentService;
import com.company.knowledge_sharing_backend.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CommentLikeRepository commentLikeRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Override
    public CommentResponse createComment(Long documentId, CreateCommentRequest request, Long userId) {
        // Validate document exists
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // If this is a reply, validate parent comment exists
        Comment parentComment = null;
        if (request.getParentCommentId() != null) {
            parentComment = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent comment not found with id: " + request.getParentCommentId()));

            // Validate parent comment belongs to the same document
            if (!parentComment.getDocument().getId().equals(documentId)) {
                throw new IllegalArgumentException("Parent comment does not belong to this document");
            }
        }

        // Create comment
        Comment comment = Comment.builder()
                .content(request.getContent())
                .document(document)
                .user(user)
                .parentComment(parentComment)
                .isEdited(false)
                .build();

        Comment savedComment = commentRepository.save(comment);

        log.info("User {} created comment {} on document {}", userId, savedComment.getId(), documentId);

        // Send notifications
        if (parentComment != null) {
            // This is a reply - notify the parent comment author
            Long parentAuthorId = parentComment.getUser().getId();
            log.info("Sending reply notification: parentAuthorId={}, replyAuthorId={}, documentId={}",
                    parentAuthorId, userId, documentId);

            notificationService.notifyCommentReply(
                    parentAuthorId,
                    userId,
                    request.getContent(),
                    documentId
            );
        } else {
            // This is a new comment - notify document owner
            log.info("Sending new comment notification: documentId={}, commentAuthorId={}",
                    documentId, userId);
            notificationService.notifyNewComment(documentId, userId, request.getContent());
        }

        return mapToCommentResponse(savedComment, userId, false);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentResponse> getCommentsByDocument(Long documentId, int page, int size, Long currentUserId) {
        // Validate document exists
        if (!documentRepository.existsById(documentId)) {
            throw new ResourceNotFoundException("Document not found with id: " + documentId);
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Comment> comments = commentRepository.findTopLevelCommentsByDocumentId(documentId, pageable);

        return comments.map(comment -> mapToCommentResponse(comment, currentUserId, false));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getRepliesByComment(Long commentId, Long currentUserId) {
        // Validate comment exists
        if (!commentRepository.existsById(commentId)) {
            throw new ResourceNotFoundException("Comment not found with id: " + commentId);
        }

        List<Comment> replies = commentRepository.findRepliesByParentCommentId(commentId);

        return replies.stream()
                .map(reply -> mapToCommentResponse(reply, currentUserId, false))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CommentResponse getCommentById(Long commentId, Long currentUserId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));

        // Include replies for this specific comment
        return mapToCommentResponse(comment, currentUserId, true);
    }

    @Override
    public CommentResponse updateComment(Long commentId, UpdateCommentRequest request, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));

        // Check ownership
        if (!comment.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You can only edit your own comments");
        }

        // Update content
        comment.setContent(request.getContent());
        comment.setIsEdited(true);

        Comment updatedComment = commentRepository.save(comment);

        log.info("User {} updated comment {}", userId, commentId);

        return mapToCommentResponse(updatedComment, userId, false);
    }

    @Override
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));

        // Check ownership
        if (!comment.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You can only delete your own comments");
        }

        // Delete comment (cascade will delete replies and likes)
        commentRepository.delete(comment);

        log.info("User {} deleted comment {}", userId, commentId);
    }

    @Override
    public void likeComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Check if already liked
        if (commentLikeRepository.existsByCommentIdAndUserId(commentId, userId)) {
            log.warn("User {} already liked comment {}", userId, commentId);
            return;
        }

        // Create like
        CommentLike like = CommentLike.builder()
                .comment(comment)
                .user(user)
                .build();

        commentLikeRepository.save(like);

        log.info("User {} liked comment {}", userId, commentId);
    }

    @Override
    public void unlikeComment(Long commentId, Long userId) {
        CommentLike like = commentLikeRepository.findByCommentIdAndUserId(commentId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Like not found"));

        commentLikeRepository.delete(like);

        log.info("User {} unliked comment {}", userId, commentId);
    }

    @Override
    public boolean toggleLike(Long commentId, Long userId) {
        // Check if exists
        if (!commentRepository.existsById(commentId)) {
            throw new ResourceNotFoundException("Comment not found with id: " + commentId);
        }

        boolean isLiked = commentLikeRepository.existsByCommentIdAndUserId(commentId, userId);

        if (isLiked) {
            unlikeComment(commentId, userId);
            return false; // Now unliked
        } else {
            likeComment(commentId, userId);
            return true; // Now liked
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long getCommentCountByDocument(Long documentId) {
        return commentRepository.countByDocumentId(documentId);
    }

    // ==================== HELPER METHODS ====================

    private CommentResponse mapToCommentResponse(Comment comment, Long currentUserId, boolean includeReplies) {
        boolean isLiked = currentUserId != null &&
                commentLikeRepository.existsByCommentIdAndUserId(comment.getId(), currentUserId);

        boolean isOwned = currentUserId != null &&
                comment.getUser().getId().equals(currentUserId);

        List<CommentResponse> replies = null;
        if (includeReplies) {
            replies = commentRepository.findRepliesByParentCommentId(comment.getId())
                    .stream()
                    .map(reply -> mapToCommentResponse(reply, currentUserId, false))
                    .collect(Collectors.toList());
        }

        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .documentId(comment.getDocument().getId())
                .userId(comment.getUser().getId())
                .username(comment.getUser().getUsername())
                .parentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null)
                .likeCount(comment.getLikeCount())
                .replyCount(comment.getReplyCount())
                .isLikedByCurrentUser(isLiked)
                .isOwnedByCurrentUser(isOwned)
                .isEdited(comment.getIsEdited())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .replies(replies)
                .build();
    }
}

