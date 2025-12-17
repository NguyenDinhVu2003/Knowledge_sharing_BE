package com.company.knowledge_sharing_backend.service.impl;

import com.company.knowledge_sharing_backend.dto.request.RatingRequest;
import com.company.knowledge_sharing_backend.dto.response.DocumentRatingStats;
import com.company.knowledge_sharing_backend.dto.response.RatingResponse;
import com.company.knowledge_sharing_backend.exception.BadRequestException;
import com.company.knowledge_sharing_backend.exception.ResourceNotFoundException;
import com.company.knowledge_sharing_backend.entity.Document;
import com.company.knowledge_sharing_backend.entity.Rating;
import com.company.knowledge_sharing_backend.entity.User;
import com.company.knowledge_sharing_backend.repository.DocumentRepository;
import com.company.knowledge_sharing_backend.repository.RatingRepository;
import com.company.knowledge_sharing_backend.repository.UserRepository;
import com.company.knowledge_sharing_backend.service.RatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class RatingServiceImpl implements RatingService {

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public RatingResponse rateDocument(Long documentId, RatingRequest request, Long userId) {
        // Get document
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

        // Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Check if user already rated this document
        if (ratingRepository.existsByDocumentIdAndUserId(documentId, userId)) {
            throw new BadRequestException("You have already rated this document. Use update instead.");
        }

        // Create rating
        Rating rating = Rating.builder()
                .document(document)
                .user(user)
                .ratingValue(request.getRatingValue())
                .build();

        rating = ratingRepository.save(rating);

        return mapToResponse(rating);
    }

    @Override
    public RatingResponse updateRating(Long documentId, RatingRequest request, Long userId) {
        // Get existing rating
        Rating rating = ratingRepository.findByDocumentIdAndUserId(documentId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Rating not found for this document and user"));

        // Update rating value
        rating.setRatingValue(request.getRatingValue());
        rating = ratingRepository.save(rating);

        return mapToResponse(rating);
    }

    @Override
    public void deleteRating(Long documentId, Long userId) {
        // Check if rating exists
        if (!ratingRepository.existsByDocumentIdAndUserId(documentId, userId)) {
            throw new ResourceNotFoundException("Rating not found for this document and user");
        }

        ratingRepository.deleteByDocumentIdAndUserId(documentId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public RatingResponse getUserRating(Long documentId, Long userId) {
        Rating rating = ratingRepository.findByDocumentIdAndUserId(documentId, userId)
                .orElse(null);

        return rating != null ? mapToResponse(rating) : null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RatingResponse> getDocumentRatings(Long documentId) {
        // Verify document exists
        if (!documentRepository.existsById(documentId)) {
            throw new ResourceNotFoundException("Document not found with id: " + documentId);
        }

        List<Rating> ratings = ratingRepository.findByDocumentId(documentId);

        return ratings.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentRatingStats getDocumentRatingStats(Long documentId) {
        // Verify document exists
        documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with id: " + documentId));

        List<Rating> ratings = ratingRepository.findByDocumentId(documentId);

        if (ratings.isEmpty()) {
            return DocumentRatingStats.builder()
                    .documentId(documentId)
                    .averageRating(0.0)
                    .totalRatings(0)
                    .fiveStars(0)
                    .fourStars(0)
                    .threeStars(0)
                    .twoStars(0)
                    .oneStar(0)
                    .build();
        }

        // Calculate statistics
        int total = ratings.size();
        double average = ratings.stream()
                .mapToInt(Rating::getRatingValue)
                .average()
                .orElse(0.0);

        int fiveStars = (int) ratings.stream().filter(r -> r.getRatingValue() == 5).count();
        int fourStars = (int) ratings.stream().filter(r -> r.getRatingValue() == 4).count();
        int threeStars = (int) ratings.stream().filter(r -> r.getRatingValue() == 3).count();
        int twoStars = (int) ratings.stream().filter(r -> r.getRatingValue() == 2).count();
        int oneStar = (int) ratings.stream().filter(r -> r.getRatingValue() == 1).count();

        return DocumentRatingStats.builder()
                .documentId(documentId)
                .averageRating(Math.round(average * 100.0) / 100.0)
                .totalRatings(total)
                .fiveStars(fiveStars)
                .fourStars(fourStars)
                .threeStars(threeStars)
                .twoStars(twoStars)
                .oneStar(oneStar)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RatingResponse> getUserRatings(Long userId) {
        // Verify user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        List<Rating> ratings = ratingRepository.findByUserId(userId);

        return ratings.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ==================== HELPER METHODS ====================

    private RatingResponse mapToResponse(Rating rating) {
        return RatingResponse.builder()
                .id(rating.getId())
                .documentId(rating.getDocument().getId())
                .documentTitle(rating.getDocument().getTitle())
                .userId(rating.getUser().getId())
                .username(rating.getUser().getUsername())
                .ratingValue(rating.getRatingValue())
                .createdAt(rating.getCreatedAt())
                .updatedAt(rating.getUpdatedAt())
                .build();
    }
}

