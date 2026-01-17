package com.poxju.proksi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class HuggingFaceService {

    private static final Logger logger = LoggerFactory.getLogger(HuggingFaceService.class);
    private static final OkHttpClient SHARED_CLIENT;
    private final ObjectMapper objectMapper;
    private final String apiToken;
    
    private static final String HF_API_URL = "https://api-inference.huggingface.co/models/facebook/bart-large-cnn";
    
    static {
        // Shared OkHttpClient with connection pooling for better resource usage
        SHARED_CLIENT = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .connectionPool(new okhttp3.ConnectionPool(5, 5, TimeUnit.MINUTES))
                .build();
    }
    
    public HuggingFaceService(@Value("${huggingface.api.token:}") String apiToken) {
        this.apiToken = apiToken;
        this.objectMapper = new ObjectMapper();
    }
    
    public String summarizeText(String text) {
        try {
            if (apiToken == null || apiToken.isEmpty() || apiToken.equals("${HUGGINGFACE_API_TOKEN}")) {
                logger.debug("HuggingFace API token not configured, using fallback");
                return generateFallbackSummary(text);
            }
            
            String truncatedText = truncateText(text, 800);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("inputs", truncatedText);
            
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("max_length", 150);
            parameters.put("min_length", 50);
            parameters.put("do_sample", false);
            requestBody.put("parameters", parameters);
            
            String requestBodyJson = objectMapper.writeValueAsString(requestBody);
            logger.debug("Sending request to HuggingFace API");
            
            RequestBody body = RequestBody.create(
                requestBodyJson, 
                MediaType.get("application/json; charset=utf-8")
            );
            
            Request request = new Request.Builder()
                    .url(HF_API_URL)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer " + apiToken)
                    .build();
            
            try (Response response = SHARED_CLIENT.newCall(request).execute()) {
                logger.debug("HuggingFace API response code: {}", response.code());
                
                ResponseBody responseBody = response.body();
                if (responseBody == null) {
                    logger.warn("HuggingFace API returned null response body");
                    return generateFallbackSummary(text);
                }
                
                if (!response.isSuccessful()) {
                    logger.warn("HuggingFace API error: {} - {}", response.code(), response.message());
                    // Consume the error response body to allow connection reuse
                    try {
                        responseBody.string();
                    } catch (Exception ignored) {
                        // Ignore errors when consuming error response
                    }
                    return generateFallbackSummary(text);
                }
                
                // Read response body - for API responses this should be reasonable size
                String responseBodyString = responseBody.string();
                return parseSummaryResponse(responseBodyString, text);
            }
            
        } catch (Exception e) {
            logger.error("Error calling HuggingFace API", e);
            return generateFallbackSummary(text);
        }
    }
    
    private String parseSummaryResponse(String responseBody, String originalText) {
        try {
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            
            if (jsonNode.has("error")) {
                String errorMsg = jsonNode.get("error").asText();
                logger.warn("HuggingFace API error: {}", errorMsg);
                
                if (errorMsg.contains("currently loading")) {
                    return "Model is loading, please try again in a few seconds. Meanwhile: " + 
                           generateFallbackSummary(originalText);
                }
                return generateFallbackSummary(originalText);
            }
            
            if (jsonNode.isArray() && jsonNode.size() > 0) {
                JsonNode firstResult = jsonNode.get(0);
                if (firstResult.has("summary_text")) {
                    String summary = firstResult.get("summary_text").asText();
                    if (summary.equals(originalText.trim())) {
                        logger.debug("Summary equals original text, using fallback");
                        return generateFallbackSummary(originalText);
                    }
                    return summary;
                }
            }
            
            if (jsonNode.has("summary_text")) {
                String summary = jsonNode.get("summary_text").asText();
                if (summary.equals(originalText.trim())) {
                    logger.debug("Summary equals original text, using fallback");
                    return generateFallbackSummary(originalText);
                }
                return summary;
            }
            
            logger.warn("Unexpected response format from HuggingFace API");
            return generateFallbackSummary(originalText);
            
        } catch (Exception e) {
            logger.error("Error parsing HuggingFace response", e);
            return generateFallbackSummary(originalText);
        }
    }
    
    private String truncateText(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        
        String truncated = text.substring(0, maxLength);
        int lastSpace = truncated.lastIndexOf(' ');
        if (lastSpace > maxLength * 0.8) { 
            truncated = truncated.substring(0, lastSpace);
        }
        
        return truncated + "...";
    }
    
    private String generateFallbackSummary(String text) {
        try {
            String cleanText = text.trim().replaceAll("\\s+", " ");
            String[] sentences = cleanText.split("\\. ");
            
            if (sentences.length <= 1) {
                if (cleanText.length() < 50) {
                    return "📝 Short note: " + cleanText;
                } else {
                    return "📝 " + (cleanText.length() > 100 ? 
                           cleanText.substring(0, 97) + "..." : cleanText);
                }
            }
            
            StringBuilder summary = new StringBuilder("📝 Summary: ");
            int sentenceCount = Math.min(2, sentences.length);
            
            for (int i = 0; i < sentenceCount; i++) {
                String sentence = sentences[i].trim();
                if (!sentence.isEmpty()) {
                    summary.append(sentence);
                    if (!sentence.endsWith(".")) {
                        summary.append(".");
                    }
                    if (i < sentenceCount - 1) {
                        summary.append(" ");
                    }
                }
            }
            
            String keywords = extractKeywords(cleanText);
            if (!keywords.isEmpty()) {
                summary.append(" [Key topics: ").append(keywords).append("]");
            }
            
            return summary.toString();
            
        } catch (Exception e) {
            return "📝 " + (text.length() > 100 ? text.substring(0, 97) + "..." : text) + 
                   " [Fallback summary]";
        }
    }
    
    private String extractKeywords(String text) {
        try {
            // Early return for very short texts
            if (text == null || text.length() < 10) {
                return "";
            }
            
            String[] words = text.toLowerCase()
                    .replaceAll("[^a-zA-Z0-9\\s]", "")
                    .split("\\s+");
            
            // Estimate initial capacity to reduce HashMap resizing
            int estimatedSize = Math.min(words.length / 2, 50);
            Map<String, Integer> wordCount = new HashMap<>(estimatedSize);
            
            // Single pass through words array - O(n) complexity
            for (String word : words) {
                if (word != null && word.length() > 3) { 
                    wordCount.merge(word, 1, Integer::sum);
                }
            }
            
            // Early return if no keywords found
            if (wordCount.isEmpty()) {
                return "";
            }
            
            // Stream processing - limit to top 3 keywords
            return wordCount.entrySet().stream()
                    .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                    .limit(3)
                    .map(Map.Entry::getKey)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");
                    
        } catch (Exception e) {
            logger.debug("Error extracting keywords", e);
            return "";
        }
    }
}