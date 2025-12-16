package com.company.knowledge_sharing_backend.repository;

import com.company.knowledge_sharing_backend.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    /**
     * Find all favorites by user
     */
    List<Favorite> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Find favorite by document and user
     */
    Optional<Favorite> findByDocumentIdAndUserId(Long documentId, Long userId);

    /**
     * Check if user has favorited a document
     */
    Boolean existsByDocumentIdAndUserId(Long documentId, Long userId);

    /**
     * Count favorites by user
     */
    Long countByUserId(Long userId);

    /**
     * Count favorites for a document
     */
    Long countByDocumentId(Long documentId);

    /**
     * Delete favorite by document and user
     */
    void deleteByDocumentIdAndUserId(Long documentId, Long userId);

    /**
     * Delete all favorites by user
     */
    void deleteByUserId(Long userId);

    /**
     * Get favorites with document details (fetch join to avoid N+1 problem)
     */
    @Query("SELECT f FROM Favorite f JOIN FETCH f.document d WHERE f.user.id = :userId ORDER BY f.createdAt DESC")
    List<Favorite> findByUserIdWithDocument(@Param("userId") Long userId);
}

