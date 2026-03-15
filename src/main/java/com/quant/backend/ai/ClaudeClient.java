package com.quant.backend.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;

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
            String requestBody = buildTextRequestBody(prompt);

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

    public String completeWithImage(String prompt, byte[] imageBytes, String mediaType) {
        if (!isConfigured()) {
            System.err.println("ClaudeClient: ANTHROPIC_API_KEY not configured");
            return null;
        }

        try {
            String normalizedMediaType = normalizeMediaType(mediaType, imageBytes);
            System.out.println("ClaudeClient image mediaType input=" + mediaType + ", normalized=" + normalizedMediaType);
            String requestBody = buildImageRequestBody(prompt, imageBytes, normalizedMediaType);

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
            System.err.println("ClaudeClient image error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private String buildTextRequestBody(String prompt) {
        return String.format(
                "{\"model\":\"claude-3-haiku-20240307\",\"max_tokens\":4096,\"messages\":[{\"role\":\"user\",\"content\":\"%s\"}]}",
                escapeJson(prompt)
        );
    }

    private String buildImageRequestBody(String prompt, byte[] imageBytes, String mediaType) {
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        return String.format(
                "{\"model\":\"claude-3-haiku-20240307\",\"max_tokens\":4096,\"messages\":[{\"role\":\"user\",\"content\":[{\"type\":\"image\",\"source\":{\"type\":\"base64\",\"media_type\":\"%s\",\"data\":\"%s\"}},{\"type\":\"text\",\"text\":\"%s\"}]}]}",
                escapeJson(mediaType),
                base64Image,
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

    private String normalizeMediaType(String mediaType, byte[] imageBytes) {
        String detectedMediaType = detectMediaTypeFromBytes(imageBytes);
        if (detectedMediaType != null) {
            return detectedMediaType;
        }

        if (mediaType == null || mediaType.isBlank()) {
            return "image/jpeg";
        }

        String normalized = mediaType.trim().toLowerCase();

        if (normalized.equals("image/jpg") || normalized.equals("image/pjpeg")) {
            return "image/jpeg";
        }

        if (normalized.equals("image/x-png")) {
            return "image/png";
        }

        if (normalized.equals("image/jpeg")
                || normalized.equals("image/png")
                || normalized.equals("image/gif")
                || normalized.equals("image/webp")) {
            return normalized;
        }

        return "image/jpeg";
    }

    private String detectMediaTypeFromBytes(byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length < 12) {
            return null;
        }

        if ((imageBytes[0] & 0xFF) == 0x89
                && imageBytes[1] == 0x50
                && imageBytes[2] == 0x4E
                && imageBytes[3] == 0x47
                && imageBytes[4] == 0x0D
                && imageBytes[5] == 0x0A
                && imageBytes[6] == 0x1A
                && imageBytes[7] == 0x0A) {
            return "image/png";
        }

        if ((imageBytes[0] & 0xFF) == 0xFF
                && (imageBytes[1] & 0xFF) == 0xD8
                && (imageBytes[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }

        if (imageBytes[0] == 'G'
                && imageBytes[1] == 'I'
                && imageBytes[2] == 'F'
                && imageBytes[3] == '8') {
            return "image/gif";
        }

        if (imageBytes[0] == 'R'
                && imageBytes[1] == 'I'
                && imageBytes[2] == 'F'
                && imageBytes[3] == 'F'
                && imageBytes[8] == 'W'
                && imageBytes[9] == 'E'
                && imageBytes[10] == 'B'
                && imageBytes[11] == 'P') {
            return "image/webp";
        }

        return null;
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