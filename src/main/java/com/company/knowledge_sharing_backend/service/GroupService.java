package com.company.knowledge_sharing_backend.service;

import com.company.knowledge_sharing_backend.dto.request.GroupRequest;
import com.company.knowledge_sharing_backend.dto.response.GroupResponse;

import java.util.List;

public interface GroupService {

    GroupResponse createGroup(GroupRequest request);

    GroupResponse updateGroup(Long groupId, GroupRequest request);

    void deleteGroup(Long groupId);

    GroupResponse getGroupById(Long groupId);

    List<GroupResponse> getAllGroups();

    List<GroupResponse> getUserGroups(Long userId);

    List<GroupResponse> searchGroups(String keyword);

    void addMember(Long groupId, Long userId);

    void removeMember(Long groupId, Long userId);
}

