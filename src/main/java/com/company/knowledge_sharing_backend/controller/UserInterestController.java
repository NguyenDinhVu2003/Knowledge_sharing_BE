package com.company.knowledge_sharing_backend.controller;

import com.company.knowledge_sharing_backend.dto.response.MessageResponse;
import com.company.knowledge_sharing_backend.entity.User;
import com.company.knowledge_sharing_backend.service.AuthService;
import com.company.knowledge_sharing_backend.service.UserInterestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user-interests")
public class UserInterestController {

    @Autowired
    private UserInterestService userInterestService;

    @Autowired
    private AuthService authService;

    /**
     * Get current user's interests
     * GET /api/user-interests
     */
    @GetMapping
    public ResponseEntity<Map<String, List<String>>> getMyInterests() {
        User currentUser = authService.getCurrentUser();
        List<String> interests = userInterestService.getUserInterests(currentUser.getId());

        Map<String, List<String>> response = new HashMap<>();
        response.put("interests", interests);

        return ResponseEntity.ok(response);
    }

    /**
     * Update user interests (replace all)
     * PUT /api/user-interests
     */
    @PutMapping
    public ResponseEntity<MessageResponse> updateInterests(@RequestBody Map<String, List<String>> request) {
        User currentUser = authService.getCurrentUser();
        List<String> tagNames = request.get("interests");

        userInterestService.updateUserInterests(currentUser.getId(), tagNames);

        return ResponseEntity.ok(new MessageResponse("Interests updated successfully"));
    }

    /**
     * Add single interest
     * POST /api/user-interests/{tagName}
     */
    @PostMapping("/{tagName}")
    public ResponseEntity<MessageResponse> addInterest(@PathVariable String tagName) {
        User currentUser = authService.getCurrentUser();
        userInterestService.addInterest(currentUser.getId(), tagName);

        return ResponseEntity.ok(new MessageResponse("Interest added"));
    }

    /**
     * Remove interest
     * DELETE /api/user-interests/{tagName}
     */
    @DeleteMapping("/{tagName}")
    public ResponseEntity<MessageResponse> removeInterest(@PathVariable String tagName) {
        User currentUser = authService.getCurrentUser();
        userInterestService.removeInterest(currentUser.getId(), tagName);

        return ResponseEntity.ok(new MessageResponse("Interest removed"));
    }
}

