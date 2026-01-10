package com.company.knowledge_sharing_backend.service.impl;

import com.company.knowledge_sharing_backend.dto.response.TagWithCountResponse;
import com.company.knowledge_sharing_backend.dto.response.UserProfileResponse;
import com.company.knowledge_sharing_backend.exception.BadRequestException;
import com.company.knowledge_sharing_backend.exception.ResourceNotFoundException;
import com.company.knowledge_sharing_backend.entity.Tag;
import com.company.knowledge_sharing_backend.entity.User;
import com.company.knowledge_sharing_backend.entity.UserInterest;
import com.company.knowledge_sharing_backend.repository.DocumentRepository;
import com.company.knowledge_sharing_backend.repository.TagRepository;
import com.company.knowledge_sharing_backend.repository.UserInterestRepository;
import com.company.knowledge_sharing_backend.repository.UserRepository;
import com.company.knowledge_sharing_backend.service.UserInterestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class UserInterestServiceImpl implements UserInterestService {

    @Autowired
    private UserInterestRepository userInterestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Override
    @Transactional(readOnly = true)
    public List<String> getUserInterests(Long userId) {
        // Verify user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        List<UserInterest> interests = userInterestRepository.findByUserIdWithTag(userId);

        return interests.stream()
                .map(interest -> interest.getTag().getName())
                .collect(Collectors.toList());
    }

    @Override
    public void updateUserInterests(Long userId, List<String> tagNames) {
        // Verify user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Delete existing interests
        userInterestRepository.deleteByUserId(userId);

        // Add new interests
        for (String tagName : tagNames) {
            Tag tag = tagRepository.findByName(tagName)
                    .orElseGet(() -> {
                        Tag newTag = new Tag();
                        newTag.setName(tagName);
                        return tagRepository.save(newTag);
                    });

            UserInterest interest = UserInterest.builder()
                    .user(user)
                    .tag(tag)
                    .build();

            userInterestRepository.save(interest);
        }
    }

    @Override
    public void addInterest(Long userId, String tagName) {
        // Verify user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Get or create tag
        Tag tag = tagRepository.findByName(tagName)
                .orElseGet(() -> {
                    Tag newTag = new Tag();
                    newTag.setName(tagName);
                    return tagRepository.save(newTag);
                });

        // Check if interest already exists
        if (userInterestRepository.existsByUserIdAndTagId(userId, tag.getId())) {
            throw new BadRequestException("You are already interested in this tag");
        }

        // Create interest
        UserInterest interest = UserInterest.builder()
                .user(user)
                .tag(tag)
                .build();

        userInterestRepository.save(interest);
    }

    @Override
    public void removeInterest(Long userId, String tagName) {
        // Verify user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        // Get tag
        Tag tag = tagRepository.findByName(tagName)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with name: " + tagName));

        // Delete interest
        userInterestRepository.deleteByUserIdAndTagId(userId, tag.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Get statistics
        Long totalDocuments = documentRepository.countByOwnerId(userId);
        int totalFavorites = user.getFavorites().size();
        int totalRatings = user.getRatings().size();

        // Get user interests with document count
        List<UserInterest> interests = userInterestRepository.findByUserId(userId);
        List<TagWithCountResponse> interestTags = interests.stream()
                .map(interest -> {
                    Tag tag = interest.getTag();
                    long docCount = tag.getDocuments().size();
                    return TagWithCountResponse.builder()
                            .id(tag.getId())
                            .name(tag.getName())
                            .description(tag.getDescription())
                            .documentCount(docCount)
                            .isInterested(true)
                            .build();
                })
                .collect(Collectors.toList());

        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().toString())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .totalDocuments(totalDocuments.intValue())
                .totalFavorites(totalFavorites)
                .totalRatings(totalRatings)
                .totalInterests(interests.size())
                .interests(interestTags)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getUserInterestIds(Long userId) {
        List<UserInterest> interests = userInterestRepository.findByUserId(userId);
        return interests.stream()
                .map(interest -> interest.getTag().getId())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagWithCountResponse> getPopularTags(Long userId, int limit) {
        List<Tag> popularTags = tagRepository.findTopPopularTags(limit);
        Set<Long> userInterestTagIds = userInterestRepository.findByUserId(userId)
                .stream()
                .map(interest -> interest.getTag().getId())
                .collect(Collectors.toSet());

        return popularTags.stream()
                .map(tag -> TagWithCountResponse.builder()
                        .id(tag.getId())
                        .name(tag.getName())
                        .description(tag.getDescription())
                        .documentCount((long) tag.getDocuments().size())
                        .isInterested(userInterestTagIds.contains(tag.getId()))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagWithCountResponse> getAllTags(Long userId, String searchKeyword) {
        List<Tag> tags;

        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            tags = tagRepository.searchByName(searchKeyword);
        } else {
            tags = tagRepository.findAll();
        }

        Set<Long> userInterestTagIds = userInterestRepository.findByUserId(userId)
                .stream()
                .map(interest -> interest.getTag().getId())
                .collect(Collectors.toSet());

        return tags.stream()
                .map(tag -> TagWithCountResponse.builder()
                        .id(tag.getId())
                        .name(tag.getName())
                        .description(tag.getDescription())
                        .documentCount((long) tag.getDocuments().size())
                        .isInterested(userInterestTagIds.contains(tag.getId()))
                        .build())
                .sorted((t1, t2) -> Long.compare(t2.getDocumentCount(), t1.getDocumentCount()))
                .collect(Collectors.toList());
    }

    @Override
    public List<TagWithCountResponse> updateUserInterestsByIds(Long userId, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            throw new IllegalArgumentException("At least one tag must be selected");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Remove all existing interests
        List<UserInterest> existingInterests = userInterestRepository.findByUserId(userId);
        userInterestRepository.deleteAll(existingInterests);

        // Add new interests
        List<UserInterest> newInterests = new ArrayList<>();
        for (Long tagId : tagIds) {
            Tag tag = tagRepository.findById(tagId)
                    .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + tagId));

            UserInterest interest = UserInterest.builder()
                    .user(user)
                    .tag(tag)
                    .build();
            newInterests.add(interest);
        }

        List<UserInterest> savedInterests = userInterestRepository.saveAll(newInterests);

        // Return updated interests
        return savedInterests.stream()
                .map(interest -> {
                    Tag tag = interest.getTag();
                    return TagWithCountResponse.builder()
                            .id(tag.getId())
                            .name(tag.getName())
                            .description(tag.getDescription())
                            .documentCount((long) tag.getDocuments().size())
                            .isInterested(true)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public void addInterestById(Long userId, Long tagId) {
        if (userInterestRepository.existsByUserIdAndTagId(userId, tagId)) {
            log.warn("User {} already interested in tag {}", userId, tagId);
            return;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + tagId));

        UserInterest interest = UserInterest.builder()
                .user(user)
                .tag(tag)
                .build();

        userInterestRepository.save(interest);
    }

    @Override
    public void removeInterestById(Long userId, Long tagId) {
        UserInterest interest = userInterestRepository.findByUserIdAndTagId(userId, tagId)
                .orElseThrow(() -> new ResourceNotFoundException("Interest not found"));

        userInterestRepository.delete(interest);
    }
}

