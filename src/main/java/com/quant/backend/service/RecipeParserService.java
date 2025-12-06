package com.quant.backend.service;

import com.quant.backend.ai.ClaudeClient;
import org.springframework.stereotype.Service;

@Service
public class RecipeParserService {

    private final ClaudeClient claudeClient;

    public RecipeParserService(ClaudeClient claudeClient) {
        this.claudeClient = claudeClient;
    }

    public String parseRaw(String recipeText) {
        if (recipeText == null || recipeText.trim().isEmpty()) {
            return null;
        }

        String prompt = "Parse the following recipe text and return a structured JSON object. Return ONLY JSON.\n\n" + recipeText;
        
        return claudeClient.complete(prompt);
    }

}

