package com.company.knowledge_sharing_backend.service;

import java.util.List;

public interface UserInterestService {

    List<String> getUserInterests(Long userId);

    void updateUserInterests(Long userId, List<String> tagNames);

    void addInterest(Long userId, String tagName);

    void removeInterest(Long userId, String tagName);
}

