package com.company.knowledge_sharing_backend.service.impl;

import com.company.knowledge_sharing_backend.dto.request.TagRequest;
import com.company.knowledge_sharing_backend.dto.response.TagResponse;
import com.company.knowledge_sharing_backend.exception.BadRequestException;
import com.company.knowledge_sharing_backend.exception.ResourceNotFoundException;
import com.company.knowledge_sharing_backend.entity.Tag;
import com.company.knowledge_sharing_backend.repository.TagRepository;
import com.company.knowledge_sharing_backend.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TagServiceImpl implements TagService {

    @Autowired
    private TagRepository tagRepository;

    @Override
    @CacheEvict(value = {"tags", "popularTags"}, allEntries = true)
    public TagResponse createTag(TagRequest request) {
        // Check if tag already exists
        if (tagRepository.existsByName(request.getName())) {
            throw new BadRequestException("Tag with name '" + request.getName() + "' already exists");
        }

        Tag tag = new Tag();
        tag.setName(request.getName());
        tag.setDescription(request.getDescription());

        tag = tagRepository.save(tag);

        return mapToResponse(tag);
    }

    @Override
    @CacheEvict(value = {"tags", "popularTags"}, allEntries = true)
    public TagResponse updateTag(Long tagId, TagRequest request) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + tagId));

        // Check if new name conflicts with existing tag
        if (!tag.getName().equals(request.getName()) && tagRepository.existsByName(request.getName())) {
            throw new BadRequestException("Tag with name '" + request.getName() + "' already exists");
        }

        tag.setName(request.getName());
        tag.setDescription(request.getDescription());

        tag = tagRepository.save(tag);

        return mapToResponse(tag);
    }

    @Override
    @CacheEvict(value = {"tags", "popularTags"}, allEntries = true)
    public void deleteTag(Long tagId) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + tagId));

        // Check if tag is used by documents
        if (tag.getDocuments() != null && !tag.getDocuments().isEmpty()) {
            throw new BadRequestException(
                    "Cannot delete tag. It is currently used by " + tag.getDocuments().size() + " document(s)");
        }

        tagRepository.delete(tag);
    }

    @Override
    @Transactional(readOnly = true)
    public TagResponse getTagById(Long tagId) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + tagId));

        return mapToResponse(tag);
    }

    @Override
    @Cacheable(value = "tags", key = "'all'")
    @Transactional(readOnly = true)
    public List<TagResponse> getAllTags() {
        List<Tag> tags = tagRepository.findAll();

        return tags.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "popularTags", key = "#limit")
    @Transactional(readOnly = true)
    public List<TagResponse> getPopularTags(int limit) {
        List<Tag> tags = tagRepository.findTopPopularTags(limit);

        return tags.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagResponse> searchTags(String keyword) {
        List<Tag> tags = tagRepository.searchByName(keyword);

        return tags.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagResponse> getTagsWithDocumentCount() {
        List<Object[]> results = tagRepository.findAllWithDocumentCount();

        return results.stream()
                .map(result -> {
                    Tag tag = (Tag) result[0];
                    Long count = (Long) result[1];
                    return mapToResponse(tag, count.intValue());
                })
                .collect(Collectors.toList());
    }

    // ==================== HELPER METHODS ====================

    private TagResponse mapToResponse(Tag tag) {
        return TagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .description(tag.getDescription())
                .documentCount(tag.getDocuments() != null ? tag.getDocuments().size() : 0)
                .createdAt(tag.getCreatedAt())
                .build();
    }

    private TagResponse mapToResponse(Tag tag, Integer documentCount) {
        return TagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .description(tag.getDescription())
                .documentCount(documentCount)
                .createdAt(tag.getCreatedAt())
                .build();
    }
}

