package com.company.knowledge_sharing_backend.repository;

import com.company.knowledge_sharing_backend.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    /**
     * Find tag by name
     */
    Optional<Tag> findByName(String name);

    /**
     * Check if tag exists by name
     */
    Boolean existsByName(String name);

    /**
     * Find tags by name in list (for bulk operations)
     */
    List<Tag> findByNameIn(List<String> names);

    /**
     * Get popular tags (most used in documents)
     */
    @Query("SELECT t FROM Tag t JOIN t.documents d GROUP BY t.id ORDER BY COUNT(d.id) DESC")
    List<Tag> findPopularTags();

    /**
     * Get top N popular tags
     */
    @Query("SELECT t FROM Tag t JOIN t.documents d GROUP BY t.id ORDER BY COUNT(d.id) DESC LIMIT :limit")
    List<Tag> findTopPopularTags(@Param("limit") int limit);

    /**
     * Search tags by name (case-insensitive)
     */
    @Query("SELECT t FROM Tag t WHERE LOWER(CAST(t.name AS string)) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Tag> searchByName(@Param("keyword") String keyword);

    /**
     * Get tags with document count
     */
    @Query("SELECT t, COUNT(d.id) as docCount FROM Tag t LEFT JOIN t.documents d GROUP BY t.id ORDER BY docCount DESC")
    List<Object[]> findAllWithDocumentCount();
}

