package com.company.knowledge_sharing_backend.controller;

import com.company.knowledge_sharing_backend.dto.request.GroupRequest;
import com.company.knowledge_sharing_backend.dto.response.GroupResponse;
import com.company.knowledge_sharing_backend.dto.response.MessageResponse;
import com.company.knowledge_sharing_backend.entity.User;
import com.company.knowledge_sharing_backend.service.AuthService;
import com.company.knowledge_sharing_backend.service.GroupService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@Tag(name = "Groups", description = "Group management for document sharing")
@SecurityRequirement(name = "bearerAuth")
public class GroupController {

    @Autowired
    private GroupService groupService;

    @Autowired
    private AuthService authService;

    /**
     * Create new group
     * POST /api/groups
     */
    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(@Valid @RequestBody GroupRequest request) {
        GroupResponse response = groupService.createGroup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update group
     * PUT /api/groups/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<GroupResponse> updateGroup(
            @PathVariable Long id,
            @Valid @RequestBody GroupRequest request) {

        GroupResponse response = groupService.updateGroup(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete group
     * DELETE /api/groups/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteGroup(@PathVariable Long id) {
        groupService.deleteGroup(id);
        return ResponseEntity.ok(new MessageResponse("Group deleted successfully"));
    }

    /**
     * Get group by ID
     * GET /api/groups/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<GroupResponse> getGroup(@PathVariable Long id) {
        GroupResponse response = groupService.getGroupById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all groups
     * GET /api/groups
     */
    @GetMapping
    public ResponseEntity<List<GroupResponse>> getAllGroups() {
        List<GroupResponse> groups = groupService.getAllGroups();
        return ResponseEntity.ok(groups);
    }

    /**
     * Get current user's groups
     * GET /api/groups/my-groups
     */
    @GetMapping("/my-groups")
    public ResponseEntity<List<GroupResponse>> getMyGroups() {
        User currentUser = authService.getCurrentUser();
        List<GroupResponse> groups = groupService.getUserGroups(currentUser.getId());
        return ResponseEntity.ok(groups);
    }

    /**
     * Search groups
     * GET /api/groups/search?keyword=team
     */
    @GetMapping("/search")
    public ResponseEntity<List<GroupResponse>> searchGroups(@RequestParam String keyword) {
        List<GroupResponse> groups = groupService.searchGroups(keyword);
        return ResponseEntity.ok(groups);
    }

    /**
     * Add member to group
     * POST /api/groups/{groupId}/members/{userId}
     */
    @PostMapping("/{groupId}/members/{userId}")
    public ResponseEntity<MessageResponse> addMember(
            @PathVariable Long groupId,
            @PathVariable Long userId) {

        groupService.addMember(groupId, userId);
        return ResponseEntity.ok(new MessageResponse("Member added successfully"));
    }

    /**
     * Remove member from group
     * DELETE /api/groups/{groupId}/members/{userId}
     */
    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<MessageResponse> removeMember(
            @PathVariable Long groupId,
            @PathVariable Long userId) {

        groupService.removeMember(groupId, userId);
        return ResponseEntity.ok(new MessageResponse("Member removed successfully"));
    }
}

