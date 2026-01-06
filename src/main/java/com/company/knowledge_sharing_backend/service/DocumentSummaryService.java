package com.company.knowledge_sharing_backend.service;

import com.company.knowledge_sharing_backend.dto.response.SummaryResponse;
import com.company.knowledge_sharing_backend.exception.RateLimitExceededException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class DocumentSummaryService {

    @Autowired
    private TextExtractionService textExtractionService;

    @Autowired
    private GeminiService geminiService;

    @Value("${api.rate.limit.summary.requests-per-minute:10}")
    private int maxRequestsPerMinute;

    // Rate limiting: userId -> [count, timestamp]
    private final Map<Long, RateLimitInfo> rateLimitMap = new ConcurrentHashMap<>();

    private static final int MAX_WORDS = 3000; // Truncate to avoid token limits

    /**
     * Generate summary from uploaded file (preview only, not saved)
     */
    public SummaryResponse generateSummary(MultipartFile file, Long userId) {
        // Check rate limit
        checkRateLimit(userId);

        // Extract text from file
        String extractedText = textExtractionService.extractText(file);

        // Truncate if too long
        extractedText = textExtractionService.truncateText(extractedText, MAX_WORDS);

        // Generate summary using Gemini
        String summary = geminiService.generateSummary(extractedText);

        return SummaryResponse.builder()
                .summary(summary)
                .build();
    }

    /**
     * Check rate limit per user
     */
    private void checkRateLimit(Long userId) {
        long currentTime = System.currentTimeMillis();

        rateLimitMap.compute(userId, (key, info) -> {
            if (info == null) {
                return new RateLimitInfo(1, currentTime);
            }

            // Reset if 1 minute has passed
            if (currentTime - info.timestamp > 60000) {
                return new RateLimitInfo(1, currentTime);
            }

            // Check limit
            if (info.count >= maxRequestsPerMinute) {
                throw new RateLimitExceededException(
                        "Rate limit exceeded. Maximum " + maxRequestsPerMinute + " requests per minute."
                );
            }

            // Increment count
            info.count++;
            return info;
        });
    }

    /**
     * Inner class for rate limit tracking
     */
    private static class RateLimitInfo {
        int count;
        long timestamp;

        RateLimitInfo(int count, long timestamp) {
            this.count = count;
            this.timestamp = timestamp;
        }
    }
}

