package com.company.knowledge_sharing_backend.service.impl;

import com.company.knowledge_sharing_backend.service.GeminiService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class GeminiServiceImpl implements GeminiService {

    // API Key (shared for both summary and embedding)
    @Value("${gemini.api.key}")
    private String apiKey;

    // Summary Configuration
    @Value("${gemini.summary.api.url}")
    private String summaryApiUrl;

    @Value("${gemini.summary.model}")
    private String summaryModel;

    @Value("${gemini.summary.max.tokens:500}")
    private int summaryMaxTokens;

    @Value("${gemini.summary.temperature:0.7}")
    private double summaryTemperature;

    // Embedding Configuration
    @Value("${gemini.embedding.api.url}")
    private String embeddingApiUrl;

    @Value("${gemini.embedding.model}")
    private String embeddingModel;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String generateSummary(String text) {
        try {
            // Build endpoint from configuration
            String endpoint = summaryApiUrl + "/models/" + summaryModel + ":generateContent?key=" + apiKey;

            // Build request
            Map<String, Object> request = new HashMap<>();
            Map<String, Object> content = new HashMap<>();
            List<Map<String, String>> parts = new ArrayList<>();

            Map<String, String> part = new HashMap<>();
            part.put("text", "Summarize the following text in a concise manner (max 200 words):\n\n" + text);
            parts.add(part);

            content.put("parts", parts);
            request.put("contents", Collections.singletonList(content));

            // Add generation config
            Map<String, Object> generationConfig = new HashMap<>();
            generationConfig.put("maxOutputTokens", summaryMaxTokens);
            generationConfig.put("temperature", summaryTemperature);
            request.put("generationConfig", generationConfig);

            // Call API
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            // Parse response
            JsonNode root = objectMapper.readTree(response.getBody());
            return root.at("/candidates/0/content/parts/0/text").asText();

        } catch (Exception e) {
            throw new RuntimeException("Gemini API error: " + e.getMessage(), e);
        }
    }

    @Override
    public String generateEmbedding(String text) {
        try {
            // Build endpoint from configuration
            String endpoint = embeddingApiUrl + "/models/" + embeddingModel + ":embedContent?key=" + apiKey;

            // Build request according to Gemini API spec
            Map<String, Object> request = new HashMap<>();
            Map<String, Object> content = new HashMap<>();
            List<Map<String, String>> parts = new ArrayList<>();

            Map<String, String> part = new HashMap<>();
            part.put("text", text);
            parts.add(part);

            content.put("parts", parts);
            request.put("content", content);

            // Call API
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            // Parse response and extract embedding values
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode valuesNode = root.at("/embedding/values");

            List<Float> embedding = new ArrayList<>();
            if (valuesNode.isArray()) {
                for (JsonNode node : valuesNode) {
                    embedding.add((float) node.asDouble());
                }
            }

            // Convert to JSON string
            return objectMapper.writeValueAsString(embedding);

        } catch (Exception e) {
            throw new RuntimeException("Gemini API error: " + e.getMessage(), e);
        }
    }

    @Override
    public double calculateCosineSimilarity(String embedding1Json, String embedding2Json) {
        float[] vec1 = parseEmbedding(embedding1Json);
        float[] vec2 = parseEmbedding(embedding2Json);

        if (vec1.length != vec2.length) {
            throw new IllegalArgumentException("Embedding vectors must have the same dimension");
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vec1.length; i++) {
            dotProduct += vec1[i] * vec2[i];
            norm1 += vec1[i] * vec1[i];
            norm2 += vec2[i] * vec2[i];
        }

        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    @Override
    public float[] parseEmbedding(String embeddingJson) {
        try {
            JsonNode node = objectMapper.readTree(embeddingJson);
            float[] result = new float[node.size()];

            for (int i = 0; i < node.size(); i++) {
                result[i] = (float) node.get(i).asDouble();
            }

            return result;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse embedding JSON", e);
        }
    }

    @Override
    public String embeddingToJson(float[] embedding) {
        try {
            return objectMapper.writeValueAsString(embedding);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert embedding to JSON", e);
        }
    }
}

