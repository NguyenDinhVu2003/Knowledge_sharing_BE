package com.company.knowledge_sharing_backend.service;

public interface GeminiService {

    /**
     * Generate summary from text using Gemini API
     * @param text Input text to summarize
     * @return Generated summary
     */
    String generateSummary(String text);

    /**
     * Generate embedding vector from text using Gemini API
     * @param text Input text to generate embedding
     * @return Embedding as JSON string (array of floats)
     */
    String generateEmbedding(String text);

    /**
     * Calculate cosine similarity between two embedding vectors
     * @param embedding1 First embedding JSON string
     * @param embedding2 Second embedding JSON string
     * @return Cosine similarity score (0.0 to 1.0)
     */
    double calculateCosineSimilarity(String embedding1, String embedding2);

    /**
     * Parse embedding JSON string to float array
     * @param embeddingJson JSON string of embedding
     * @return Float array
     */
    float[] parseEmbedding(String embeddingJson);

    /**
     * Convert float array to JSON string
     * @param embedding Float array
     * @return JSON string
     */
    String embeddingToJson(float[] embedding);
}

