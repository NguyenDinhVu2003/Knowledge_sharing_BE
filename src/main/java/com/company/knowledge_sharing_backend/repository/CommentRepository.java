package com.company.knowledge_sharing_backend.repository;

import com.company.knowledge_sharing_backend.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * Find all top-level comments (no parent) for a document
     */
    @Query("SELECT c FROM Comment c WHERE c.document.id = :documentId AND c.parentComment IS NULL ORDER BY c.createdAt DESC")
    Page<Comment> findTopLevelCommentsByDocumentId(@Param("documentId") Long documentId, Pageable pageable);

    /**
     * Find all top-level comments (no parent) for a document - list version
     */
    @Query("SELECT c FROM Comment c WHERE c.document.id = :documentId AND c.parentComment IS NULL ORDER BY c.createdAt DESC")
    List<Comment> findTopLevelCommentsByDocumentId(@Param("documentId") Long documentId);

    /**
     * Find all replies for a comment
     */
    @Query("SELECT c FROM Comment c WHERE c.parentComment.id = :parentCommentId ORDER BY c.createdAt ASC")
    List<Comment> findRepliesByParentCommentId(@Param("parentCommentId") Long parentCommentId);

    /**
     * Count comments for a document
     */
    Long countByDocumentId(Long documentId);

    /**
     * Count comments by user
     */
    Long countByUserId(Long userId);

    /**
     * Find comment with user and document
     */
    @Query("SELECT c FROM Comment c JOIN FETCH c.user JOIN FETCH c.document WHERE c.id = :commentId")
    Optional<Comment> findByIdWithUserAndDocument(@Param("commentId") Long commentId);

    /**
     * Delete all comments for a document
     */
    void deleteByDocumentId(Long documentId);

    /**
     * Delete all comments by user
     */
    void deleteByUserId(Long userId);
}

