package com.company.knowledge_sharing_backend.service;

import com.company.knowledge_sharing_backend.dto.request.DocumentSearchRequest;
import com.company.knowledge_sharing_backend.dto.response.SearchResultResponse;

public interface SearchService {

    SearchResultResponse advancedSearch(DocumentSearchRequest request, Long currentUserId);
}

