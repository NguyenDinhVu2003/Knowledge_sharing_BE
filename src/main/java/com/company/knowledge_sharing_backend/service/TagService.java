package com.company.knowledge_sharing_backend.service;

import com.company.knowledge_sharing_backend.dto.request.TagRequest;
import com.company.knowledge_sharing_backend.dto.response.TagResponse;

import java.util.List;

public interface TagService {

    TagResponse createTag(TagRequest request);

    TagResponse updateTag(Long tagId, TagRequest request);

    void deleteTag(Long tagId);

    TagResponse getTagById(Long tagId);

    List<TagResponse> getAllTags();

    List<TagResponse> getPopularTags(int limit);

    List<TagResponse> searchTags(String keyword);

    List<TagResponse> getTagsWithDocumentCount();
}

