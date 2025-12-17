package com.company.knowledge_sharing_backend.service;

import com.company.knowledge_sharing_backend.dto.response.FavoriteResponse;

import java.util.List;

public interface FavoriteService {

    FavoriteResponse addFavorite(Long documentId, Long userId);

    void removeFavorite(Long documentId, Long userId);

    List<FavoriteResponse> getUserFavorites(Long userId);

    boolean isFavorited(Long documentId, Long userId);

    Integer getFavoriteCount(Long documentId);
}

