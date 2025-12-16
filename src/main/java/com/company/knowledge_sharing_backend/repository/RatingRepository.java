package com.company.knowledge_sharing_backend.repository;

import com.company.knowledge_sharing_backend.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    /**
     * Find rating by document and user (to check if user already rated)
     */
    Optional<Rating> findByDocumentIdAndUserId(Long documentId, Long userId);

    /**
     * Check if user has rated a document
     */
    Boolean existsByDocumentIdAndUserId(Long documentId, Long userId);

    /**
     * Find all ratings for a document
     */
    List<Rating> findByDocumentId(Long documentId);

    /**
     * Find all ratings by a user
     */
    List<Rating> findByUserId(Long userId);

    /**
     * Calculate average rating for a document
     */
    @Query("SELECT AVG(r.ratingValue) FROM Rating r WHERE r.document.id = :documentId")
    Double calculateAverageRating(@Param("documentId") Long documentId);

    /**
     * Count ratings for a document
     */
    Long countByDocumentId(Long documentId);

    /**
     * Delete rating by document and user
     */
    void deleteByDocumentIdAndUserId(Long documentId, Long userId);
}

