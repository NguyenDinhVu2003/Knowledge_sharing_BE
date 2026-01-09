package com.company.knowledge_sharing_backend.service;

import com.company.knowledge_sharing_backend.dto.response.DocumentResponse;

import java.util.List;

public interface SemanticSearchService {

    /**
     * Search documents using AI semantic similarity
     * @param query User search query
     * @param userId Current user ID
     * @param limit Maximum number of results
     * @return List of documents ranked by semantic similarity
     */
    List<DocumentResponse> semanticSearch(String query, Long userId, int limit);

    /**
     * Generate and save embedding for a document
     * @param documentId Document ID
     */
    void generateDocumentEmbedding(Long documentId);

    /**
     * Generate embeddings for all documents without embeddings (background job)
     */
    void generateMissingEmbeddings();
}

