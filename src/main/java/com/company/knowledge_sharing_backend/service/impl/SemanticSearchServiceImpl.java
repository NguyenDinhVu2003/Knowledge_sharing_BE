package com.company.knowledge_sharing_backend.service.impl;

import com.company.knowledge_sharing_backend.dto.response.DocumentResponse;
import com.company.knowledge_sharing_backend.entity.Document;
import com.company.knowledge_sharing_backend.entity.Group;
import com.company.knowledge_sharing_backend.entity.SharingLevel;
import com.company.knowledge_sharing_backend.entity.Tag;
import com.company.knowledge_sharing_backend.entity.User;
import com.company.knowledge_sharing_backend.exception.ResourceNotFoundException;
import com.company.knowledge_sharing_backend.repository.DocumentRepository;
import com.company.knowledge_sharing_backend.repository.UserRepository;
import com.company.knowledge_sharing_backend.service.GeminiService;
import com.company.knowledge_sharing_backend.service.SemanticSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SemanticSearchServiceImpl implements SemanticSearchService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GeminiService geminiService;

    @Override
    @Transactional(readOnly = true)
    public List<DocumentResponse> semanticSearch(String query, Long userId, int limit) {
        // Generate embedding for user query
        log.info("Generating embedding for query: {}", query);
        String queryEmbedding = geminiService.generateEmbedding(query);

        // Get all accessible documents
        List<Document> allDocuments = documentRepository.findAll();

        // Calculate similarity scores
        List<DocumentWithScore> scoredDocuments = new ArrayList<>();

        for (Document doc : allDocuments) {
            // Skip if archived
            if (doc.getIsArchived()) {
                continue;
            }

            // Check access permission
            if (!canAccessDocument(doc, userId)) {
                continue;
            }

            // Skip if no embedding
            if (doc.getContentEmbedding() == null || doc.getContentEmbedding().isEmpty()) {
                continue;
            }

            try {
                // Calculate cosine similarity
                double similarity = geminiService.calculateCosineSimilarity(
                        queryEmbedding,
                        doc.getContentEmbedding()
                );

                scoredDocuments.add(new DocumentWithScore(doc, similarity));
            } catch (Exception e) {
                log.warn("Failed to calculate similarity for document {}: {}", doc.getId(), e.getMessage());
            }
        }

        // Sort by similarity score (descending) and limit
        return scoredDocuments.stream()
                .sorted(Comparator.comparingDouble(DocumentWithScore::getScore).reversed())
                .limit(limit)
                .map(ds -> mapToResponse(ds.getDocument(), ds.getScore()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void generateDocumentEmbedding(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

        try {
            // Combine title, summary, and content for embedding
            StringBuilder textBuilder = new StringBuilder();
            textBuilder.append(document.getTitle());

            if (document.getSummary() != null && !document.getSummary().isEmpty()) {
                textBuilder.append(" ").append(document.getSummary());
            }

            if (document.getContent() != null && !document.getContent().isEmpty()) {
                textBuilder.append(" ").append(document.getContent());
            }

            String text = textBuilder.toString();

            // Generate embedding
            log.info("Generating embedding for document {}: {}", document.getId(), document.getTitle());
            String embedding = geminiService.generateEmbedding(text);

            // Save embedding
            document.setContentEmbedding(embedding);
            documentRepository.save(document);

            log.info("Successfully generated embedding for document {}", document.getId());
        } catch (Exception e) {
            log.error("Failed to generate embedding for document {}: {}", document.getId(), e.getMessage());
            throw new RuntimeException("Failed to generate embedding", e);
        }
    }

    @Override
    @Transactional
    @Scheduled(fixedDelay = 3600000) // Run every 1 hour
    public void generateMissingEmbeddings() {
        log.info("Starting background job to generate missing embeddings");

        List<Document> documentsWithoutEmbedding = documentRepository.findAll().stream()
                .filter(doc -> !doc.getIsArchived())
                .filter(doc -> doc.getContentEmbedding() == null || doc.getContentEmbedding().isEmpty())
                .collect(Collectors.toList());

        log.info("Found {} documents without embeddings", documentsWithoutEmbedding.size());

        for (Document doc : documentsWithoutEmbedding) {
            try {
                generateDocumentEmbedding(doc.getId());
            } catch (Exception e) {
                log.error("Failed to generate embedding for document {}: {}", doc.getId(), e.getMessage());
                // Continue with next document
            }
        }

        log.info("Finished generating missing embeddings");
    }

    // Helper methods

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

    private DocumentResponse mapToResponse(Document document, double semanticScore) {
        return DocumentResponse.builder()
                .id(document.getId())
                .title(document.getTitle())
                .summary(document.getSummary())
                .content(document.getContent())
                .filePath(null) // Don't expose file path
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
                .semanticScore(semanticScore) // Add semantic similarity score
                .build();
    }

    // Inner class for holding document with score
    private static class DocumentWithScore {
        private final Document document;
        private final double score;

        public DocumentWithScore(Document document, double score) {
            this.document = document;
            this.score = score;
        }

        public Document getDocument() {
            return document;
        }

        public double getScore() {
            return score;
        }
    }
}

