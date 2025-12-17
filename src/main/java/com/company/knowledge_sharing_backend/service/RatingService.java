package com.company.knowledge_sharing_backend.service;

import com.company.knowledge_sharing_backend.dto.request.RatingRequest;
import com.company.knowledge_sharing_backend.dto.response.DocumentRatingStats;
import com.company.knowledge_sharing_backend.dto.response.RatingResponse;

import java.util.List;

public interface RatingService {

    RatingResponse rateDocument(Long documentId, RatingRequest request, Long userId);

    RatingResponse updateRating(Long documentId, RatingRequest request, Long userId);

    void deleteRating(Long documentId, Long userId);

    RatingResponse getUserRating(Long documentId, Long userId);

    List<RatingResponse> getDocumentRatings(Long documentId);

    DocumentRatingStats getDocumentRatingStats(Long documentId);

    List<RatingResponse> getUserRatings(Long userId);
}

