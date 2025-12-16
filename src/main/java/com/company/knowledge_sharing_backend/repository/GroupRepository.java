package com.company.knowledge_sharing_backend.repository;

import com.company.knowledge_sharing_backend.entity.Group;
import com.company.knowledge_sharing_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    /**
     * Find group by name
     */
    Optional<Group> findByName(String name);

    /**
     * Check if group name exists
     */
    Boolean existsByName(String name);

    /**
     * Find all groups that a user belongs to
     */
    @Query("SELECT g FROM Group g JOIN g.users u WHERE u.id = :userId")
    List<Group> findByUserId(@Param("userId") Long userId);

    /**
     * Find groups by user (alternative using containing)
     */
    List<Group> findByUsersContaining(User user);

    /**
     * Search groups by name (case-insensitive)
     */
    @Query("SELECT g FROM Group g WHERE LOWER(g.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Group> searchByName(@Param("keyword") String keyword);
}

