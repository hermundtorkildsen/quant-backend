package com.quant.backend.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ClaudeClient {

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final ObjectMapper objectMapper;

    public ClaudeClient() {
        this.apiKey = System.getenv("ANTHROPIC_API_KEY");
        this.objectMapper = new ObjectMapper();
        this.restTemplate = new RestTemplate();
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isEmpty();
    }

    public String complete(String prompt) {
        if (!isConfigured()) {
            System.err.println("ClaudeClient: ANTHROPIC_API_KEY not configured");
            return null;
        }

        try {
            String requestBody = buildRequestBody(prompt);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", apiKey != null ? apiKey : "");
            headers.set("anthropic-version", "2023-06-01");
            
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            
            String responseJson = restTemplate.postForObject(
                    "https://api.anthropic.com/v1/messages",
                    entity,
                    String.class
            );

            if (responseJson == null) {
                System.err.println("ClaudeClient: Received null response");
                return null;
            }

            return parseResponse(responseJson);
        } catch (Exception e) {
            System.err.println("ClaudeClient error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private String buildRequestBody(String prompt) {
        return String.format(
            "{\"model\":\"claude-3-haiku-20240307\",\"max_tokens\":1024,\"messages\":[{\"role\":\"user\",\"content\":\"%s\"}]}",
            escapeJson(prompt)
        );
    }

    private String escapeJson(String input) {
        return input.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }

    private String parseResponse(String responseJson) {
        try {
            JsonNode root = objectMapper.readTree(responseJson);
            JsonNode content = root.path("content");
            if (content.isArray() && content.size() > 0) {
                JsonNode firstContent = content.get(0);
                JsonNode text = firstContent.path("text");
                if (text.isTextual()) {
                    return text.asText();
                }
            }
            System.err.println("ClaudeClient: Unexpected response structure");
            return null;
        } catch (Exception e) {
            System.err.println("ClaudeClient: Failed to parse response - " + e.getMessage());
            return null;
        }
    }

}

