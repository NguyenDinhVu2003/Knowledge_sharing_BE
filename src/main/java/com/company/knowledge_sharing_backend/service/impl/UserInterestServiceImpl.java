package com.company.knowledge_sharing_backend.service.impl;

import com.company.knowledge_sharing_backend.exception.BadRequestException;
import com.company.knowledge_sharing_backend.exception.ResourceNotFoundException;
import com.company.knowledge_sharing_backend.entity.Tag;
import com.company.knowledge_sharing_backend.entity.User;
import com.company.knowledge_sharing_backend.entity.UserInterest;
import com.company.knowledge_sharing_backend.repository.TagRepository;
import com.company.knowledge_sharing_backend.repository.UserInterestRepository;
import com.company.knowledge_sharing_backend.repository.UserRepository;
import com.company.knowledge_sharing_backend.service.UserInterestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserInterestServiceImpl implements UserInterestService {

    @Autowired
    private UserInterestRepository userInterestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TagRepository tagRepository;

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
}

