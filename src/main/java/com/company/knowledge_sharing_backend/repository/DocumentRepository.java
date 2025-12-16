package com.company.knowledge_sharing_backend.repository;

import com.company.knowledge_sharing_backend.entity.Document;
import com.company.knowledge_sharing_backend.entity.SharingLevel;
import com.company.knowledge_sharing_backend.entity.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    /**
     * Find documents by owner ID
     */
    Page<Document> findByOwnerId(Long ownerId, Pageable pageable);

    /**
     * Find documents by owner ID (not archived)
     */
    Page<Document> findByOwnerIdAndIsArchivedFalse(Long ownerId, Pageable pageable);

    /**
     * Find documents by sharing level
     */
    Page<Document> findBySharingLevel(SharingLevel sharingLevel, Pageable pageable);

    /**
     * Find documents by sharing level (not archived)
     */
    Page<Document> findBySharingLevelAndIsArchivedFalse(SharingLevel sharingLevel, Pageable pageable);

    /**
     * Find top N recent documents (not archived)
     */
    @Query("SELECT d FROM Document d WHERE d.isArchived = false ORDER BY d.createdAt DESC LIMIT :limit")
    List<Document> findTopRecentDocuments(@Param("limit") int limit);

    /**
     * Find top N popular documents by rating (not archived)
     */
    @Query("SELECT d FROM Document d LEFT JOIN d.ratings r " +
           "WHERE d.isArchived = false " +
           "GROUP BY d.id " +
           "ORDER BY AVG(r.ratingValue) DESC, COUNT(r.id) DESC " +
           "LIMIT :limit")
    List<Document> findTopPopularDocuments(@Param("limit") int limit);

    /**
     * Find documents by tag
     */
    @Query("SELECT d FROM Document d JOIN d.tags t WHERE t.id = :tagId AND d.isArchived = false")
    Page<Document> findByTagId(@Param("tagId") Long tagId, Pageable pageable);

    /**
     * Find documents containing any of the tags
     */
    @Query("SELECT DISTINCT d FROM Document d JOIN d.tags t WHERE t IN :tags AND d.isArchived = false")
    Page<Document> findByTagsIn(@Param("tags") List<Tag> tags, Pageable pageable);

    /**
     * Search documents by title or content (case-insensitive using database function)
     */
    @Query(value = "SELECT * FROM documents d WHERE " +
           "d.is_archived = false AND " +
           "(LOWER(d.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(d.summary) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(d.content) LIKE LOWER(CONCAT('%', :keyword, '%')))",
           countQuery = "SELECT COUNT(*) FROM documents d WHERE " +
           "d.is_archived = false AND " +
           "(LOWER(d.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(d.summary) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(d.content) LIKE LOWER(CONCAT('%', :keyword, '%')))",
           nativeQuery = true)
    Page<Document> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Advanced search with multiple filters - using native query
     */
    @Query(value = "SELECT DISTINCT d.* FROM documents d " +
           "LEFT JOIN document_tags dt ON d.id = dt.document_id " +
           "WHERE d.is_archived = false " +
           "AND (:keyword IS NULL OR LOWER(d.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "    OR LOWER(d.summary) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:sharingLevel IS NULL OR d.sharing_level = :sharingLevel) " +
           "AND (:fromDate IS NULL OR d.created_at >= :fromDate) " +
           "AND (:toDate IS NULL OR d.created_at <= :toDate) " +
           "AND (:tagIds IS NULL OR dt.tag_id IN :tagIds)",
           countQuery = "SELECT COUNT(DISTINCT d.id) FROM documents d " +
           "LEFT JOIN document_tags dt ON d.id = dt.document_id " +
           "WHERE d.is_archived = false " +
           "AND (:keyword IS NULL OR LOWER(d.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "    OR LOWER(d.summary) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:sharingLevel IS NULL OR d.sharing_level = :sharingLevel) " +
           "AND (:fromDate IS NULL OR d.created_at >= :fromDate) " +
           "AND (:toDate IS NULL OR d.created_at <= :toDate) " +
           "AND (:tagIds IS NULL OR dt.tag_id IN :tagIds)",
           nativeQuery = true)
    Page<Document> advancedSearch(
        @Param("keyword") String keyword,
        @Param("sharingLevel") String sharingLevel,
        @Param("fromDate") LocalDateTime fromDate,
        @Param("toDate") LocalDateTime toDate,
        @Param("tagIds") List<Long> tagIds,
        Pageable pageable
    );

    /**
     * Find documents accessible by user (public or user is owner or in group)
     */
    @Query("SELECT DISTINCT d FROM Document d " +
           "LEFT JOIN d.groups g " +
           "LEFT JOIN g.users u " +
           "WHERE d.isArchived = false AND " +
           "(d.sharingLevel = 'PUBLIC' OR d.owner.id = :userId OR u.id = :userId)")
    Page<Document> findAccessibleByUser(@Param("userId") Long userId, Pageable pageable);

    /**
     * Count documents by owner
     */
    Long countByOwnerId(Long ownerId);

    /**
     * Count documents by sharing level
     */
    Long countBySharingLevel(SharingLevel sharingLevel);
}

