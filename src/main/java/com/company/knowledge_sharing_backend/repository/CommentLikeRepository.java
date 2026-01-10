package com.company.knowledge_sharing_backend.repository;

import com.company.knowledge_sharing_backend.entity.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    /**
     * Check if user has liked a comment
     */
    Boolean existsByCommentIdAndUserId(Long commentId, Long userId);

    /**
     * Find like by comment and user
     */
    Optional<CommentLike> findByCommentIdAndUserId(Long commentId, Long userId);

    /**
     * Count likes for a comment
     */
    Long countByCommentId(Long commentId);

    /**
     * Find all likes by user
     */
    List<CommentLike> findByUserId(Long userId);

    /**
     * Delete all likes for a comment
     */
    void deleteByCommentId(Long commentId);

    /**
     * Delete all likes by user
     */
    void deleteByUserId(Long userId);
}

