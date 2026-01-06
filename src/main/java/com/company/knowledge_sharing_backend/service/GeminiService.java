package com.company.knowledge_sharing_backend.service;

import com.company.knowledge_sharing_backend.exception.GPTApiException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    @Value("${gemini.model}")
    private String model;

    @Value("${gemini.max.tokens}")
    private int maxTokens;

    @Value("${gemini.temperature}")
    private double temperature;

    @Value("${gemini.timeout:30}")
    private int timeout;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Generate summary using Gemini API
     */
    public String generateSummary(String extractedText) {
        if (extractedText == null || extractedText.trim().isEmpty()) {
            throw new GPTApiException("Extracted text is empty. Cannot generate summary.");
        }

        try {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(timeout, TimeUnit.SECONDS)
                    .readTimeout(timeout, TimeUnit.SECONDS)
                    .writeTimeout(timeout, TimeUnit.SECONDS)
                    .build();

            String prompt = buildPrompt(extractedText);
            String requestBody = buildRequestBody(prompt);

            // Gemini API URL format: {baseUrl}/{model}:generateContent?key={apiKey}
            String fullUrl = apiUrl + "/" + model + ":generateContent?key=" + apiKey;

            Request request = new Request.Builder()
                    .url(fullUrl)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                    throw new GPTApiException("Gemini API error: " + response.code() + " - " + errorBody);
                }

                String responseBody = response.body().string();
                return parseSummaryFromResponse(responseBody);
            }

        } catch (IOException e) {
            throw new GPTApiException("Failed to call Gemini API: " + e.getMessage(), e);
        }
    }

    /**
     * Build prompt for Gemini
     */
    private String buildPrompt(String extractedText) {
        return """
                You are a professional document summarizer. Analyze the following document and create a concise, informative summary.
                
                Guidelines:
                - Capture the main topic and key points
                - Keep summary between 3-5 sentences (50-100 words)
                - Use clear, professional language
                - Focus on actionable information
                - Avoid unnecessary details
                
                Document content:
                %s
                
                Provide only the summary, no additional commentary.
                """.formatted(extractedText);
    }

    /**
     * Build request body for Gemini API
     * Format: https://ai.google.dev/api/rest/v1beta/models/generateContent
     */
    private String buildRequestBody(String prompt) {
        try {
            Map<String, Object> requestBody = new HashMap<>();

            // Contents array with single part
            Map<String, Object> part = new HashMap<>();
            part.put("text", prompt);

            Map<String, Object> content = new HashMap<>();
            content.put("parts", List.of(part));

            requestBody.put("contents", List.of(content));

            // Generation config
            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("temperature", temperature);
            generationConfig.put("maxOutputTokens", maxTokens);

            requestBody.put("generationConfig", generationConfig);

            return objectMapper.writeValueAsString(requestBody);
        } catch (Exception e) {
            throw new GPTApiException("Failed to build request body", e);
        }
    }

    /**
     * Parse summary from Gemini response
     * Response format: { "candidates": [{ "content": { "parts": [{ "text": "..." }] } }] }
     */
    private String parseSummaryFromResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode candidates = root.get("candidates");

            if (candidates == null || candidates.isEmpty()) {
                throw new GPTApiException("No candidates in Gemini response");
            }

            JsonNode content = candidates.get(0).get("content");
            if (content == null) {
                throw new GPTApiException("No content in Gemini response");
            }

            JsonNode parts = content.get("parts");
            if (parts == null || parts.isEmpty()) {
                throw new GPTApiException("No parts in Gemini response");
            }

            String text = parts.get(0).get("text").asText();
            return text.trim();

        } catch (Exception e) {
            throw new GPTApiException("Failed to parse Gemini response: " + e.getMessage(), e);
        }
    }
}

