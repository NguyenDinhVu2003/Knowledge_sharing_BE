package com.company.knowledge_sharing_backend.service;

import com.company.knowledge_sharing_backend.dto.response.SystemStatistics;
import com.company.knowledge_sharing_backend.dto.response.UserManagementResponse;

import java.util.List;

public interface AdminService {

    // User management
    List<UserManagementResponse> getAllUsers();

    UserManagementResponse getUserDetails(Long userId);

    void updateUserRole(Long userId, String role);

    void deleteUser(Long userId);

    // Statistics
    SystemStatistics getSystemStatistics();

    // Content moderation
    void deleteDocument(Long documentId);

    void deleteRating(Long ratingId);

    void deleteNotification(Long notificationId);
}

