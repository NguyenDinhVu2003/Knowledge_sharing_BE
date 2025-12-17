package com.company.knowledge_sharing_backend.service.impl;

import com.company.knowledge_sharing_backend.dto.response.FavoriteResponse;
import com.company.knowledge_sharing_backend.exception.BadRequestException;
import com.company.knowledge_sharing_backend.exception.ResourceNotFoundException;
import com.company.knowledge_sharing_backend.entity.Document;
import com.company.knowledge_sharing_backend.entity.Favorite;
import com.company.knowledge_sharing_backend.entity.User;
import com.company.knowledge_sharing_backend.repository.DocumentRepository;
import com.company.knowledge_sharing_backend.repository.FavoriteRepository;
import com.company.knowledge_sharing_backend.repository.UserRepository;
import com.company.knowledge_sharing_backend.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class FavoriteServiceImpl implements FavoriteService {

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public FavoriteResponse addFavorite(Long documentId, Long userId) {
        // Get document
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

        // Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Check if already favorited
        if (favoriteRepository.existsByDocumentIdAndUserId(documentId, userId)) {
            throw new BadRequestException("Document is already in your favorites");
        }

        // Create favorite
        Favorite favorite = Favorite.builder()
                .document(document)
                .user(user)
                .build();

        favorite = favoriteRepository.save(favorite);

        return mapToResponse(favorite);
    }

    @Override
    public void removeFavorite(Long documentId, Long userId) {
        // Check if favorite exists
        if (!favoriteRepository.existsByDocumentIdAndUserId(documentId, userId)) {
            throw new ResourceNotFoundException("Favorite not found for this document");
        }

        favoriteRepository.deleteByDocumentIdAndUserId(documentId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FavoriteResponse> getUserFavorites(Long userId) {
        // Verify user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        // Get favorites with document details (using fetch join to avoid N+1)
        List<Favorite> favorites = favoriteRepository.findByUserIdWithDocument(userId);

        return favorites.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFavorited(Long documentId, Long userId) {
        return favoriteRepository.existsByDocumentIdAndUserId(documentId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getFavoriteCount(Long documentId) {
        return favoriteRepository.countByDocumentId(documentId).intValue();
    }

    // ==================== HELPER METHODS ====================

    private FavoriteResponse mapToResponse(Favorite favorite) {
        Document document = favorite.getDocument();

        return FavoriteResponse.builder()
                .id(favorite.getId())
                .documentId(document.getId())
                .documentTitle(document.getTitle())
                .documentSummary(document.getSummary())
                .ownerUsername(document.getOwner().getUsername())
                .averageRating(document.getAverageRating())
                .createdAt(favorite.getCreatedAt())
                .build();
    }
}

