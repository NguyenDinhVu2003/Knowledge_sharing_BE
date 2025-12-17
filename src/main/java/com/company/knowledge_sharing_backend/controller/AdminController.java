package com.company.knowledge_sharing_backend.controller;

import com.company.knowledge_sharing_backend.dto.request.UpdateUserRoleRequest;
import com.company.knowledge_sharing_backend.dto.response.MessageResponse;
import com.company.knowledge_sharing_backend.dto.response.SystemStatistics;
import com.company.knowledge_sharing_backend.dto.response.UserManagementResponse;
import com.company.knowledge_sharing_backend.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')") // Only admins can access these endpoints
@Tag(name = "Admin", description = "Admin-only endpoints for user management and statistics")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    @Autowired
    private AdminService adminService;

    /**
     * Get all users
     * GET /api/admin/users
     */
    @Operation(
        summary = "Get all users",
        description = "Retrieve all users with their statistics (Admin only)"
    )
    @ApiResponse(responseCode = "200", description = "Users retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
    @GetMapping("/users")
    public ResponseEntity<List<UserManagementResponse>> getAllUsers() {
        List<UserManagementResponse> users = adminService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Get user details
     * GET /api/admin/users/{userId}
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserManagementResponse> getUserDetails(@PathVariable Long userId) {
        UserManagementResponse user = adminService.getUserDetails(userId);
        return ResponseEntity.ok(user);
    }

    /**
     * Update user role
     * PUT /api/admin/users/{userId}/role
     */
    @Operation(
        summary = "Update user role",
        description = "Change user role between EMPLOYEE and ADMIN"
    )
    @ApiResponse(responseCode = "200", description = "Role updated successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    @PutMapping("/users/{userId}/role")
    public ResponseEntity<MessageResponse> updateUserRole(
            @Parameter(description = "User ID")
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRoleRequest request) {

        adminService.updateUserRole(userId, request.getRole());
        return ResponseEntity.ok(new MessageResponse("User role updated successfully"));
    }

    /**
     * Delete user
     * DELETE /api/admin/users/{userId}
     */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<MessageResponse> deleteUser(@PathVariable Long userId) {
        adminService.deleteUser(userId);
        return ResponseEntity.ok(new MessageResponse("User deleted successfully"));
    }

    /**
     * Get system statistics
     * GET /api/admin/statistics
     */
    @Operation(
        summary = "Get system statistics",
        description = "Get comprehensive system statistics including users, documents, ratings, and top performers"
    )
    @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    @GetMapping("/statistics")
    public ResponseEntity<SystemStatistics> getStatistics() {
        SystemStatistics stats = adminService.getSystemStatistics();
        return ResponseEntity.ok(stats);
    }

    /**
     * Delete document (admin override)
     * DELETE /api/admin/documents/{documentId}
     */
    @DeleteMapping("/documents/{documentId}")
    public ResponseEntity<MessageResponse> deleteDocument(@PathVariable Long documentId) {
        adminService.deleteDocument(documentId);
        return ResponseEntity.ok(new MessageResponse("Document deleted successfully"));
    }

    /**
     * Delete rating (content moderation)
     * DELETE /api/admin/ratings/{ratingId}
     */
    @DeleteMapping("/ratings/{ratingId}")
    public ResponseEntity<MessageResponse> deleteRating(@PathVariable Long ratingId) {
        adminService.deleteRating(ratingId);
        return ResponseEntity.ok(new MessageResponse("Rating deleted successfully"));
    }

    /**
     * Delete notification (cleanup)
     * DELETE /api/admin/notifications/{notificationId}
     */
    @DeleteMapping("/notifications/{notificationId}")
    public ResponseEntity<MessageResponse> deleteNotification(@PathVariable Long notificationId) {
        adminService.deleteNotification(notificationId);
        return ResponseEntity.ok(new MessageResponse("Notification deleted successfully"));
    }
}

