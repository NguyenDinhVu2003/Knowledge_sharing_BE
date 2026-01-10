package com.company.knowledge_sharing_backend.service;

import com.company.knowledge_sharing_backend.dto.response.TagWithCountResponse;
import com.company.knowledge_sharing_backend.dto.response.UserProfileResponse;

import java.util.List;

/**
 * Service interface for managing user interests (tags user follows)
 */
public interface UserInterestService {

    /**
     * Get user interests as tag names (legacy method)
     */
    List<String> getUserInterests(Long userId);

    /**
     * Update user interests by tag names (legacy method)
     */
    void updateUserInterests(Long userId, List<String> tagNames);

    /**
     * Add interest by tag name (legacy method)
     */
    void addInterest(Long userId, String tagName);

    /**
     * Remove interest by tag name (legacy method)
     */
    void removeInterest(Long userId, String tagName);

    /**
     * Get user profile with interests and statistics
     */
    UserProfileResponse getUserProfile(Long userId);

    /**
     * Get current user interests (tag IDs only)
     */
    List<Long> getUserInterestIds(Long userId);

    /**
     * Get popular tags with document count and interest status
     */
    List<TagWithCountResponse> getPopularTags(Long userId, int limit);

    /**
     * Get all available tags with document count and interest status
     */
    List<TagWithCountResponse> getAllTags(Long userId, String searchKeyword);

    /**
     * Update user interests (replace all)
     */
    List<TagWithCountResponse> updateUserInterestsByIds(Long userId, List<Long> tagIds);

    /**
     * Add a single interest by tag ID
     */
    void addInterestById(Long userId, Long tagId);

    /**
     * Remove a single interest by tag ID
     */
    void removeInterestById(Long userId, Long tagId);
}

