package com.quant.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quant.backend.ai.ClaudeClient;
import com.quant.backend.dto.RecipeDto;
import com.quant.backend.dto.RecipeMetadataDto;
import org.springframework.stereotype.Service;

@Service
public class RecipeParserService {

    private final ClaudeClient claudeClient;
    private final ObjectMapper objectMapper;

    public RecipeParserService(ClaudeClient claudeClient) {
        this.claudeClient = claudeClient;
        this.objectMapper = new ObjectMapper();
    }

    public String parseRaw(String recipeText) {
        if (recipeText == null || recipeText.trim().isEmpty()) {
            return null;
        }

        String prompt = """
            You are a strict JSON-only recipe converter.

            Your ONLY job is to read free-form recipe text and return a single JSON object that matches EXACTLY the structure described below.

            IMPORTANT OUTPUT RULES:
            - Output MUST be valid JSON.
            - Output MUST NOT be wrapped in markdown.
            - Do NOT include comments or explanations.
            - Do NOT include any extra fields not defined in the schema.
            - Top-level keys use camelCase (id, title, description, servings, ingredients, steps, metadata).
            - Metadata keys use snake_case (source_url, image_url, calculator_id, import_method).
            - If some information is missing, set the corresponding field to null or an empty list where appropriate.
            - If you are unsure about something, still make your best guess, but keep unknown values as null.
            - Do NOT generate an id. Always set "id": null.
            - LANGUAGE RULE: The entire output must be written in the same language as the input text.
              - If the input text is Norwegian, generate all fields (including generated descriptions) in Norwegian (Bokmål).
              - Do NOT translate content that already exists; preserve original wording when extracting.
              - Only generated fallback descriptions should follow the detected input language.

            TARGET JSON STRUCTURE (this is an example, not literal output):

            {
              "id": null,
              "title": "Recipe title",
              "description": "Short description or null",
              "servings": 4,
              "ingredients": [
                {
                  "amount": 400.0,
                  "unit": "g",
                  "item": "spaghetti",
                  "notes": "finhakket"
                }
              ],
              "steps": [
                {
                  "step": 1,
                  "instruction": "First step.",
                  "notes": null
                }
              ],
              "metadata": {
                "source_url": null,
                "author": null,
                "language": null,
                "categories": [],
                "image_url": null,
                "calculator_id": null,
                "import_method": "plain_text"
              }
            }

            DETAILED FIELD RULES:

            - id:
              - MUST always be present
              - MUST always be null (the backend will generate IDs)

            - title:
              - Short recipe title
              - Required. If not obvious, invent a reasonable title based on the recipe text.

            - description:
              - If an explicit description, introduction, or summary paragraph exists in the input text, extract it as-is.
              - If no description is present in the input, generate a concise 1–2 sentence description based on:
                - The recipe title
                - The general cooking method implied by the steps (e.g., baked, fried, simmered, roasted, grilled)
              - The generated description must NOT invent ingredients that are not in the input.
              - Keep it concise, neutral, and factual.
              - Do NOT return null or empty string. Always provide a description.

            - servings:
              - Integer or null.
              - Parse from phrases like "4 porsjoner", "serves 2", "til 6 personer".
              - If you are unsure, use null.

            - ingredients (array):
              - Try to extract one ingredient per logical line.
              - Each ingredient object MUST have:
                - amount: number or null (e.g. 400, 1.5)
                - unit: string or null (normalized where possible, e.g. "g", "kg", "ml", "dl", "l", "ts", "ss", "cup")
                - item: ingredient name without amount or unit (e.g. "spaghetti", "hvetemel", "olivenolje")
                - notes: extra info like "finhakket", "romtemperert", or null
              - If amount is missing or unclear, set amount = null.
              - If unit is missing or unnecessary (e.g. "2 egg"), set unit = null.

            - steps (array):
              - Break the method/instructions into logical steps.
              - Keep the original order.
              - Each step object MUST have:
                - step: 1-based integer (1, 2, 3, ...)
                - instruction: the main step text as a single string
                - notes: null or small extra note if needed.

            - metadata:
              - source_url:
                - Set to null (the backend may fill this later).
              - author:
                - Set to null.
              - language:
                - If obvious (e.g. Norwegian vs English), you MAY set "no" or "en".
                - Otherwise, set to null.
              - categories:
                - Array of strings like ["pasta", "middag", "vegetar"].
                - If nothing obvious, use [].
              - image_url:
                - Set to null.
              - calculator_id:
                - Set to null.
              - import_method:
                - Always set to "plain_text".

            NOW CONVERT THE FOLLOWING RECIPE TEXT INTO EXACTLY ONE JSON OBJECT WITH THIS STRUCTURE.

            REMEMBER: OUTPUT ONLY JSON, NO MARKDOWN, NO EXPLANATIONS.

            RECIPE TEXT:

            """ + "\n\n" + recipeText;
        
        return claudeClient.complete(prompt);
    }

    public RecipeDto parseToRecipe(String recipeText, String sourceUrl) {
        if (recipeText == null || recipeText.trim().isEmpty()) {
            return null;
        }

        try {
            String json = parseRaw(recipeText);
            if (json == null || json.trim().isEmpty()) {
                return null;
            }

            RecipeDto dto = objectMapper.readValue(json, RecipeDto.class);

            if (dto.getMetadata() == null) {
                dto.setMetadata(RecipeMetadataDto.builder().build());
            }

            RecipeMetadataDto metadata = dto.getMetadata();
            RecipeMetadataDto updatedMetadata = RecipeMetadataDto.builder()
                    .sourceUrl(sourceUrl)
                    .author(metadata.getAuthor())
                    .language(metadata.getLanguage())
                    .categories(metadata.getCategories() != null ? metadata.getCategories() : new java.util.ArrayList<>())
                    .imageUrl(metadata.getImageUrl())
                    .calculatorId(metadata.getCalculatorId())
                    .importMethod("plain_text")
                    .build();

            dto.setMetadata(updatedMetadata);

            return dto;
        } catch (Exception e) {
            System.err.println("RecipeParserService: Failed to parse recipe - " + e.getMessage());
            return null;
        }
    }

}

