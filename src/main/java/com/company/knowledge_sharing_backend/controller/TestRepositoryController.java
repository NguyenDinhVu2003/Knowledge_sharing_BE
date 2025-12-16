package com.company.knowledge_sharing_backend.controller;

import com.company.knowledge_sharing_backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test/repositories")
public class TestRepositoryController {

    @Autowired private UserRepository userRepository;
    @Autowired private GroupRepository groupRepository;
    @Autowired private TagRepository tagRepository;
    @Autowired private DocumentRepository documentRepository;
    @Autowired private DocumentVersionRepository versionRepository;
    @Autowired private RatingRepository ratingRepository;
    @Autowired private FavoriteRepository favoriteRepository;
    @Autowired private UserInterestRepository interestRepository;
    @Autowired private NotificationRepository notificationRepository;

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> countAllEntities() {
        Map<String, Long> counts = new HashMap<>();

        counts.put("users", userRepository.count());
        counts.put("groups", groupRepository.count());
        counts.put("tags", tagRepository.count());
        counts.put("documents", documentRepository.count());
        counts.put("documentVersions", versionRepository.count());
        counts.put("ratings", ratingRepository.count());
        counts.put("favorites", favoriteRepository.count());
        counts.put("userInterests", interestRepository.count());
        counts.put("notifications", notificationRepository.count());

        return ResponseEntity.ok(counts);
    }

    @GetMapping("/status")
    public ResponseEntity<String> status() {
        return ResponseEntity.ok("All repositories are working! ✅");
    }
}

