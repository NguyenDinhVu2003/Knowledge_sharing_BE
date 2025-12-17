package com.company.knowledge_sharing_backend.service.impl;

import com.company.knowledge_sharing_backend.dto.request.GroupRequest;
import com.company.knowledge_sharing_backend.dto.response.GroupResponse;
import com.company.knowledge_sharing_backend.exception.BadRequestException;
import com.company.knowledge_sharing_backend.exception.ResourceNotFoundException;
import com.company.knowledge_sharing_backend.entity.Group;
import com.company.knowledge_sharing_backend.entity.User;
import com.company.knowledge_sharing_backend.repository.GroupRepository;
import com.company.knowledge_sharing_backend.repository.UserRepository;
import com.company.knowledge_sharing_backend.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class GroupServiceImpl implements GroupService {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public GroupResponse createGroup(GroupRequest request) {
        // Check if group name already exists
        if (groupRepository.existsByName(request.getName())) {
            throw new BadRequestException("Group with name '" + request.getName() + "' already exists");
        }

        Group group = new Group();
        group.setName(request.getName());
        group.setDescription(request.getDescription());

        // Add members if provided
        if (request.getUserIds() != null && !request.getUserIds().isEmpty()) {
            Set<User> members = new HashSet<>();
            for (Long userId : request.getUserIds()) {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
                members.add(user);
            }
            group.setUsers(members);
        }

        group = groupRepository.save(group);

        return mapToResponse(group);
    }

    @Override
    public GroupResponse updateGroup(Long groupId, GroupRequest request) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        // Check if new name conflicts
        if (!group.getName().equals(request.getName()) && groupRepository.existsByName(request.getName())) {
            throw new BadRequestException("Group with name '" + request.getName() + "' already exists");
        }

        group.setName(request.getName());
        group.setDescription(request.getDescription());

        // Update members if provided
        if (request.getUserIds() != null) {
            Set<User> members = new HashSet<>();
            for (Long userId : request.getUserIds()) {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
                members.add(user);
            }
            group.setUsers(members);
        }

        group = groupRepository.save(group);

        return mapToResponse(group);
    }

    @Override
    public void deleteGroup(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        // Check if group has documents
        if (group.getDocuments() != null && !group.getDocuments().isEmpty()) {
            throw new BadRequestException(
                    "Cannot delete group. It is currently associated with " +
                    group.getDocuments().size() + " document(s)");
        }

        groupRepository.delete(group);
    }

    @Override
    @Transactional(readOnly = true)
    public GroupResponse getGroupById(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        return mapToResponse(group);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupResponse> getAllGroups() {
        List<Group> groups = groupRepository.findAll();

        return groups.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupResponse> getUserGroups(Long userId) {
        // Verify user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        List<Group> groups = groupRepository.findByUserId(userId);

        return groups.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupResponse> searchGroups(String keyword) {
        List<Group> groups = groupRepository.searchByName(keyword);

        return groups.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void addMember(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Check if user is already a member
        if (group.getUsers().contains(user)) {
            throw new BadRequestException("User is already a member of this group");
        }

        group.addUser(user);
        groupRepository.save(group);
    }

    @Override
    public void removeMember(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Check if user is a member
        if (!group.getUsers().contains(user)) {
            throw new BadRequestException("User is not a member of this group");
        }

        group.removeUser(user);
        groupRepository.save(group);
    }

    // ==================== HELPER METHODS ====================

    private GroupResponse mapToResponse(Group group) {
        return GroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .memberCount(group.getUsers() != null ? group.getUsers().size() : 0)
                .documentCount(group.getDocuments() != null ? group.getDocuments().size() : 0)
                .memberUsernames(group.getUsers() != null ?
                        group.getUsers().stream()
                                .map(User::getUsername)
                                .collect(Collectors.toList()) :
                        List.of())
                .createdAt(group.getCreatedAt())
                .updatedAt(group.getUpdatedAt())
                .build();
    }
}

