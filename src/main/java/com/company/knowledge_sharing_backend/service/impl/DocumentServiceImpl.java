package com.company.knowledge_sharing_backend.service.impl;

import com.company.knowledge_sharing_backend.dto.request.DocumentRequest;
import com.company.knowledge_sharing_backend.dto.request.DocumentSearchRequest;
import com.company.knowledge_sharing_backend.dto.response.DocumentDetailResponse;
import com.company.knowledge_sharing_backend.dto.response.DocumentResponse;
import com.company.knowledge_sharing_backend.dto.response.DocumentVersionResponse;
import com.company.knowledge_sharing_backend.exception.BadRequestException;
import com.company.knowledge_sharing_backend.exception.ResourceNotFoundException;
import com.company.knowledge_sharing_backend.exception.UnauthorizedException;
import com.company.knowledge_sharing_backend.entity.*;
import com.company.knowledge_sharing_backend.repository.*;
import com.company.knowledge_sharing_backend.service.DocumentService;
import com.company.knowledge_sharing_backend.service.FileStorageService;
import com.company.knowledge_sharing_backend.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class DocumentServiceImpl implements DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private DocumentVersionRepository versionRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    @Lazy
    private NotificationService notificationService;

    @Override
    public DocumentResponse createDocument(DocumentRequest request, MultipartFile file, Long userId) {
        // Validate file is provided
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is required");
        }

        // Get user
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Store file
        String fileName = fileStorageService.storeFile(file);
        FileType fileType = fileStorageService.determineFileType(file.getOriginalFilename());

        // Create document
        Document document = Document.builder()
                .title(request.getTitle())
                .summary(request.getSummary())
                .content(request.getContent())
                .filePath(fileName)
                .fileType(fileType)
                .fileSize(file.getSize())
                .sharingLevel(SharingLevel.valueOf(request.getSharingLevel()))
                .versionNumber(1)
                .isArchived(false)
                .owner(owner)
                .build();

        // Handle tags
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            Set<Tag> tags = handleTags(request.getTags());
            document.setTags(tags);
        }

        // Handle groups (if GROUP sharing)
        if (request.getSharingLevel().equals("GROUP") && request.getGroupIds() != null) {
            Set<Group> groups = handleGroups(request.getGroupIds());
            document.setGroups(groups);
        }

        // Save document
        document = documentRepository.save(document);

        // Create initial version
        createVersion(document, owner.getUsername(), "Initial version");

        // Trigger notification for new document
        try {
            notificationService.notifyNewDocument(document);
        } catch (Exception e) {
            // Log error but don't fail document creation
            System.err.println("Failed to send notifications: " + e.getMessage());
        }

        return mapToResponse(document);
    }

    @Override
    // Removed cache eviction - not caching documents anymore
    public DocumentResponse updateDocument(Long documentId, DocumentRequest request, MultipartFile file, Long userId) {
        // Get document
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

        // Check ownership
        if (!document.getOwner().getId().equals(userId)) {
            throw new UnauthorizedException("You don't have permission to update this document");
        }

        // Check if archived
        if (document.getIsArchived()) {
            throw new BadRequestException("Cannot update archived document");
        }

        // Update fields
        document.setTitle(request.getTitle());
        document.setSummary(request.getSummary());
        document.setContent(request.getContent());
        document.setSharingLevel(SharingLevel.valueOf(request.getSharingLevel()));

        // Update tags
        if (request.getTags() != null) {
            document.getTags().clear();
            Set<Tag> tags = handleTags(request.getTags());
            document.setTags(tags);
        }

        // Update groups
        if (request.getSharingLevel().equals("GROUP") && request.getGroupIds() != null) {
            document.getGroups().clear();
            Set<Group> groups = handleGroups(request.getGroupIds());
            document.setGroups(groups);
        }

        // Handle file update (creates new version)
        if (file != null && !file.isEmpty()) {
            String newFileName = fileStorageService.storeFile(file);
            FileType newFileType = fileStorageService.determineFileType(file.getOriginalFilename());

            // Delete old file (optional - might want to keep for versions)
            // fileStorageService.deleteFile(document.getFilePath());

            document.setFilePath(newFileName);
            document.setFileType(newFileType);
            document.setFileSize(file.getSize());
            document.setVersionNumber(document.getVersionNumber() + 1);

            // Create new version
            User user = userRepository.findById(userId).orElseThrow();
            createVersion(document, user.getUsername(), request.getChangeNotes());
        }

        document = documentRepository.save(document);

        // Trigger notification for document update
        try {
            notificationService.notifyDocumentUpdate(document);
        } catch (Exception e) {
            System.err.println("Failed to send notifications: " + e.getMessage());
        }

        return mapToResponse(document);
    }

    @Override
    public void archiveDocument(Long documentId, Long userId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

        // Check ownership
        if (!document.getOwner().getId().equals(userId)) {
            throw new UnauthorizedException("You don't have permission to archive this document");
        }

        document.setIsArchived(true);
        documentRepository.save(document);
    }

    @Override
    // Removed cache eviction - not caching documents anymore
    public void deleteDocument(Long documentId, Long userId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

        // Check ownership
        if (!document.getOwner().getId().equals(userId)) {
            throw new UnauthorizedException("You don't have permission to delete this document");
        }

        // Delete file
        fileStorageService.deleteFile(document.getFilePath());

        // Delete document (cascade will delete versions, ratings, favorites, etc.)
        documentRepository.delete(document);
    }

    @Override
    //@Cacheable(value = "documentDetails", key = "#documentId + '_' + #userId", unless = "#result == null")
    @Transactional(readOnly = true)
    public DocumentDetailResponse getDocumentById(Long documentId, Long userId) {
        // Use optimized query with fetch joins to avoid N+1 queries
        Document document = documentRepository.findByIdWithDetails(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

        // Check access permission
        if (!canAccessDocument(document, userId)) {
            throw new UnauthorizedException("You don't have permission to access this document");
        }

        return mapToDetailResponse(document, userId);
    }

    @Override
    // Cache removed - document data changes frequently and causes ClassCastException with Redis
    @Transactional(readOnly = true)
    public List<DocumentResponse> getRecentDocuments(int limit, Long userId) {
        List<Document> documents = documentRepository.findTopRecentDocuments(limit);

        // Filter by access permission
        return documents.stream()
                .filter(doc -> canAccessDocument(doc, userId))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    // Cache removed - document data changes frequently and causes ClassCastException with Redis
    @Transactional(readOnly = true)
    public List<DocumentResponse> getPopularDocuments(int limit, Long userId) {
        List<Document> documents = documentRepository.findTopPopularDocuments(limit);

        // Filter by access permission
        return documents.stream()
                .filter(doc -> canAccessDocument(doc, userId))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponse> getUserDocuments(Long userId, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by("createdAt").descending());
        Page<Document> documents = documentRepository.findByOwnerIdAndIsArchivedFalse(userId, pageable);

        return documents.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponse> searchDocuments(DocumentSearchRequest request, Long userId) {
        Pageable pageable = PageRequest.of(
            request.getPage(),
            request.getSize(),
            getSortOrder(request.getSortBy())
        );

        Page<Document> documents;

        if (request.getQuery() != null && !request.getQuery().isEmpty()) {
            documents = documentRepository.searchByKeyword(request.getQuery(), pageable);
        } else {
            String sharingLevel = request.getSharingLevel();

            List<Long> tagIds = null;
            if (request.getTags() != null && !request.getTags().isEmpty()) {
                tagIds = tagRepository.findByNameIn(request.getTags()).stream()
                        .map(Tag::getId)
                        .collect(Collectors.toList());
            }

            documents = documentRepository.advancedSearch(
                null,
                sharingLevel,
                request.getFromDate(),
                request.getToDate(),
                tagIds,
                pageable
            );
        }

        // Filter by access permission
        return documents.stream()
                .filter(doc -> canAccessDocument(doc, userId))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentVersionResponse> getDocumentVersions(Long documentId, Long userId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

        // Check access permission
        if (!canAccessDocument(document, userId)) {
            throw new UnauthorizedException("You don't have permission to access this document");
        }

        List<DocumentVersion> versions = versionRepository.findByDocumentIdOrderByVersionNumberDesc(documentId);

        return versions.stream()
                .map(this::mapToVersionResponse)
                .collect(Collectors.toList());
    }

    // ==================== HELPER METHODS ====================

    private Set<Tag> handleTags(List<String> tagNames) {
        Set<Tag> tags = new HashSet<>();

        for (String tagName : tagNames) {
            Tag tag = tagRepository.findByName(tagName)
                    .orElseGet(() -> {
                        Tag newTag = new Tag();
                        newTag.setName(tagName);
                        return tagRepository.save(newTag);
                    });
            tags.add(tag);
        }

        return tags;
    }

    private Set<Group> handleGroups(List<Long> groupIds) {
        Set<Group> groups = new HashSet<>();

        for (Long groupId : groupIds) {
            Group group = groupRepository.findById(groupId)
                    .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));
            groups.add(group);
        }

        return groups;
    }

    private void createVersion(Document document, String updatedBy, String changeNotes) {
        DocumentVersion version = DocumentVersion.builder()
                .document(document)
                .versionNumber(document.getVersionNumber())
                .updatedBy(updatedBy)
                .changeNotes(changeNotes != null ? changeNotes : "No notes provided")
                .filePath(document.getFilePath())
                .build();

        versionRepository.save(version);
    }

    private boolean canAccessDocument(Document document, Long userId) {
        // Owner can always access
        if (document.getOwner().getId().equals(userId)) {
            return true;
        }

        // Public documents
        if (document.getSharingLevel() == SharingLevel.PUBLIC) {
            return true;
        }

        // Group documents - check if user is in any of the groups
        if (document.getSharingLevel() == SharingLevel.GROUP) {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                return document.getGroups().stream()
                        .anyMatch(group -> group.getUsers().contains(user));
            }
        }

        // Private documents - only owner
        return false;
    }

    private Sort getSortOrder(String sortBy) {
        if (sortBy == null) {
            return Sort.by("createdAt").descending();
        }

        switch (sortBy.toLowerCase()) {
            case "oldest":
                return Sort.by("createdAt").ascending();
            case "title":
                return Sort.by("title").ascending();
            case "popular":
                // Note: This is a simplified sort. For true rating sort, use custom query
                return Sort.by("createdAt").descending();
            case "recent":
            default:
                return Sort.by("createdAt").descending();
        }
    }

    // ==================== MAPPING METHODS ====================

    private DocumentResponse mapToResponse(Document document) {
        return DocumentResponse.builder()
                .id(document.getId())
                .title(document.getTitle())
                .summary(document.getSummary())
                .content(document.getContent())
                .filePath(document.getFilePath())
                .fileType(document.getFileType().name())
                .fileSize(document.getFileSize())
                .sharingLevel(document.getSharingLevel().name())
                .versionNumber(document.getVersionNumber())
                .isArchived(document.getIsArchived())
                .ownerId(document.getOwner().getId())
                .ownerUsername(document.getOwner().getUsername())
                .averageRating(document.getAverageRating())
                .ratingCount(document.getRatingCount())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .tags(document.getTags().stream().map(Tag::getName).collect(Collectors.toList()))
                .groupIds(document.getGroups().stream().map(Group::getId).collect(Collectors.toList()))
                .build();
    }

    private DocumentDetailResponse mapToDetailResponse(Document document, Long userId) {
        // Get user's rating
        Integer userRating = ratingRepository.findByDocumentIdAndUserId(document.getId(), userId)
                .map(Rating::getRatingValue)
                .orElse(null);

        // Check if favorited
        Boolean isFavorited = favoriteRepository.existsByDocumentIdAndUserId(document.getId(), userId);

        // Get versions
        List<DocumentVersion> versions = versionRepository.findByDocumentIdOrderByVersionNumberDesc(document.getId());

        return DocumentDetailResponse.builder()
                .id(document.getId())
                .title(document.getTitle())
                .summary(document.getSummary())
                .content(document.getContent())
                .filePath(document.getFilePath())
                .fileType(document.getFileType().name())
                .fileSize(document.getFileSize())
                .sharingLevel(document.getSharingLevel().name())
                .versionNumber(document.getVersionNumber())
                .isArchived(document.getIsArchived())
                .ownerId(document.getOwner().getId())
                .ownerUsername(document.getOwner().getUsername())
                .ownerEmail(document.getOwner().getEmail())
                .averageRating(document.getAverageRating())
                .ratingCount(document.getRatingCount())
                .favoriteCount(favoriteRepository.countByDocumentId(document.getId()).intValue())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .tags(document.getTags().stream().map(Tag::getName).collect(Collectors.toList()))
                .groupIds(document.getGroups().stream().map(Group::getId).collect(Collectors.toList()))
                .versions(versions.stream().map(this::mapToVersionResponse).collect(Collectors.toList()))
                .userRating(userRating)
                .isFavorited(isFavorited)
                .build();
    }

    private DocumentVersionResponse mapToVersionResponse(DocumentVersion version) {
        return DocumentVersionResponse.builder()
                .id(version.getId())
                .versionNumber(version.getVersionNumber())
                .updatedBy(version.getUpdatedBy())
                .changeNotes(version.getChangeNotes())
                .filePath(version.getFilePath())
                .createdAt(version.getCreatedAt())
                .build();
    }
}

