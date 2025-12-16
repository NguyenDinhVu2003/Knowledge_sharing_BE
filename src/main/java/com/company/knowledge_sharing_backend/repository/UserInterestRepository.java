package com.company.knowledge_sharing_backend.repository;

import com.company.knowledge_sharing_backend.entity.UserInterest;
import com.company.knowledge_sharing_backend.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserInterestRepository extends JpaRepository<UserInterest, Long> {

    /**
     * Find all interests by user
     */
    List<UserInterest> findByUserId(Long userId);

    /**
     * Find interest by user and tag
     */
    Optional<UserInterest> findByUserIdAndTagId(Long userId, Long tagId);

    /**
     * Check if user is interested in a tag
     */
    Boolean existsByUserIdAndTagId(Long userId, Long tagId);

    /**
     * Delete interest by user and tag
     */
    void deleteByUserIdAndTagId(Long userId, Long tagId);

    /**
     * Delete all interests by user
     */
    void deleteByUserId(Long userId);

    /**
     * Get user interests with tag details (fetch join)
     */
    @Query("SELECT ui FROM UserInterest ui JOIN FETCH ui.tag WHERE ui.user.id = :userId")
    List<UserInterest> findByUserIdWithTag(@Param("userId") Long userId);

    /**
     * Find users interested in specific tags (for notifications)
     */
    @Query("SELECT ui.user.id FROM UserInterest ui WHERE ui.tag IN :tags")
    List<Long> findUserIdsByTags(@Param("tags") List<Tag> tags);

    /**
     * Count interests by user
     */
    Long countByUserId(Long userId);
}

