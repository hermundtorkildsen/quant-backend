package com.quant.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quant.backend.ai.ClaudeClient;
import com.quant.backend.dto.RecipeDto;
import com.quant.backend.dto.RecipeMetadataDto;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

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

        String normalizedRecipeText = normalizeRecipeText(recipeText);
        if (normalizedRecipeText == null || normalizedRecipeText.isEmpty()) {
            return null;
        }

        logBlock("RECIPE_IMPORT INPUT", normalizedRecipeText);


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
    - If you are unsure, do NOT guess. Keep unknown values as null and preserve only what is supported by the input text.    
    - Do NOT generate an id. Always set "id": null.
    - LANGUAGE RULE: The entire output must be written in the same language as the input text.
      - If the input text is Norwegian, generate all fields (including generated descriptions) in Norwegian (Bokmål).
      - Do NOT translate content that already exists; preserve original wording when extracting.
      - Only generated fallback descriptions should follow the detected input language.
    - The input may come from copied text, PDF extraction, DOCX extraction, or OCR-like text.
    - Preserve recipe meaning even if spacing or line breaks are imperfect.
    - If an ingredient is split across nearby lines, combine it into one logical ingredient when the meaning is clear.
    - Ignore obvious non-recipe noise such as page numbers, isolated headers/footers, repeated file artifacts, and standalone decorative text.
    - Do NOT treat section headings as ingredients or steps.

    TARGET JSON STRUCTURE (this is an example, not literal output):

    {
      "id": null,
      "title": "Recipe title",
      "description": "Short description",
      "servings": 4,
      "ingredients": [
        {
          "amount": 400.0,
          "unit": "g",
          "item": "spaghetti",
          "notes": "finhakket",
          "section": null
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
      - If amount, unit, and ingredient name are split across adjacent lines because of formatting, combine them when the meaning is clear.
      - Ignore isolated formatting fragments that are clearly not ingredients.
      - Each ingredient object MUST have:
        - amount: number or null (e.g. 400, 1.5)
        - unit: string or null (normalized where possible, e.g. "g", "kg", "ml", "dl", "l", "ts", "ss", "cup")
        - item: ingredient name without amount or unit (e.g. "spaghetti", "hvetemel", "olivenolje")
        - notes: extra info like "finhakket", "romtemperert", or null
        - section: string or null (e.g. "Saus", "Salat", "Topping"). Use null if no grouping.
      - If amount is missing or unclear, set amount = null.
      - If unit is missing or unnecessary (e.g. "2 egg"), set unit = null.

      - OPTIONAL GROUPING (sections):
        - Many recipes group ingredients under headings like "Saus", "Salat", "Topping", "Marinade".
        - If the input has such headings, set "section" on each ingredient to the heading it belongs to.
        - For ingredients not under any heading, set "section": null.
        - Headings are usually a standalone line without amount/unit (e.g. "Saus").
        - Do NOT create new ingredients for headings. Headings should not appear as ingredients.
        - If there are NO clear headings, you MAY still set "section" when the text clearly labels a group in-line,
          e.g. "Til sausen:", "Saus:", "Dressing:", "Til marinaden:".
        - In that case, set section to that label (normalized, e.g. "Saus", "Dressing", "Marinade") for the following relevant ingredients.
        - If you are not confident, keep section = null. Do NOT invent sections.

    - steps (array):
      - Merge broken lines that clearly belong to the same instruction.
      - Break the method/instructions into logical steps.
      - Keep the original order.
      - A step MUST describe a concrete cooking action (an instruction the user should perform).
      - DO NOT include as steps:
        - references/links to other recipes ("find the recipe here", "see recipe", URLs)
        - shopping advice ("you can buy", "available in stores")
        - personal opinions or general commentary ("I like", "I prefer", "it varies")
        - long background/serving traditions (move to description or notes instead)
      - If a paragraph mixes action + commentary:
        - Keep ONLY the actionable instruction in "instruction"
        - Move the rest to "notes" (or drop it if not useful)
      - Never output URLs anywhere in steps.
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
    """ + "\n\n" + normalizedRecipeText;


        // TODO remove
        String response = claudeClient.complete(prompt);

        System.out.println("=== RECIPE_IMPORT RAW AI RESPONSE START ===");
        System.out.println(response);
        System.out.println("=== RECIPE_IMPORT RAW AI RESPONSE END ===");

        return response;

        //return claudeClient.complete(prompt);
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

            // TODO remove
            System.out.println("=== RECIPE_IMPORT PARSED DTO TITLE ===");
            System.out.println(dto.getTitle());
            System.out.println("=== RECIPE_IMPORT PARSED DTO DESCRIPTION ===");
            System.out.println(dto.getDescription());

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
            e.printStackTrace();
            return null;
        }
    }

    public String parseImageRaw(MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            return null;
        }

        try {
            String mediaType = imageFile.getContentType();
            if (mediaType == null || mediaType.isBlank()) {
                mediaType = "image/jpeg";
            }

            String prompt = """
    You are a strict JSON-only recipe converter.

    Your ONLY job is to read a recipe from the provided image and return a single JSON object that matches EXACTLY the structure described below.

    IMPORTANT OUTPUT RULES:
    - Output MUST be valid JSON.
    - Output MUST NOT be wrapped in markdown.
    - Do NOT include comments or explanations.
    - Do NOT include any extra fields not defined in the schema.
    - Top-level keys use camelCase (id, title, description, servings, ingredients, steps, metadata).
    - Metadata keys use snake_case (source_url, image_url, calculator_id, import_method).
    - If some information is missing, set the corresponding field to null or an empty list where appropriate.
    - If you are unsure, do NOT guess. Keep unknown values as null and preserve only what is supported by the image.
    - Do NOT generate an id. Always set "id": null.
    - LANGUAGE RULE: The entire output must be written in the same language as the recipe in the image.
      - If the recipe is Norwegian, generate all fields in Norwegian (Bokmål).
      - Do NOT translate content that already exists; preserve original wording when extracting.
      - Only generated fallback descriptions should follow the detected language.

    TARGET JSON STRUCTURE:

    {
      "id": null,
      "title": "Recipe title",
      "description": "Short description",
      "servings": 4,
      "ingredients": [
        {
          "amount": 400.0,
          "unit": "g",
          "item": "spaghetti",
          "notes": "finhakket",
          "section": null
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
        "import_method": "image"
      }
    }

    DETAILED FIELD RULES:

    - id:
      - MUST always be present
      - MUST always be null

    - title:
      - Short recipe title
      - Required. If not obvious, invent a reasonable title based on the visible recipe.

    - description:
      - If an explicit description, introduction, or summary exists in the image, extract it as-is.
      - If no description is present, generate a concise 1–2 sentence description based on the visible recipe.
      - Do NOT invent ingredients that are not visible.
      - Do NOT return null or empty string. Always provide a description.

    - servings:
      - Integer or null.
      - Parse if clearly visible.
      - If unsure, use null.

    - ingredients (array):
      - Extract one ingredient per logical line where possible.
      - Each ingredient object MUST have:
        - amount: number or null
        - unit: string or null
        - item: ingredient name
        - notes: extra info or null
        - section: string or null
      - If amount is missing or unclear, set amount = null.
      - If unit is missing or unnecessary, set unit = null.
      - If section headings are visible, use them. Otherwise section = null.

    - steps (array):
      - Break visible instructions into logical steps.
      - Keep original order.
      - A step MUST describe a concrete cooking action.
      - Do NOT include links, URLs, shopping advice, or commentary as steps.
      - Each step object MUST have:
        - step: 1-based integer
        - instruction: the main step text
        - notes: null or small extra note if needed

    - metadata:
      - source_url: null
      - author: null
      - language: "no" or "en" if obvious, otherwise null
      - categories: array of strings, or []
      - image_url: null
      - calculator_id: null
      - import_method: always "image"

    IMPORTANT IMAGE RULES:
    - Read only what is actually visible in the image.
    - Ignore decorative/background text that is not part of the recipe.
    - If parts are unreadable, do not guess.
    - Handwritten recipes may be difficult to read. Only extract what is reasonably legible.
    - If text is partially unreadable, keep unknown values as null instead of guessing.
    - Do not invent missing lines, ingredients, or quantities.
    - Ignore decorative elements, shadows, table backgrounds, and irrelevant surrounding text.

    RETURN EXACTLY ONE JSON OBJECT.
    OUTPUT ONLY JSON.
    """;

            String response = claudeClient.completeWithImage(
                    prompt,
                    imageFile.getBytes(),
                    mediaType
            );

            System.out.println("=== RECIPE_IMAGE_IMPORT RAW AI RESPONSE START ===");
            System.out.println(response);
            System.out.println("=== RECIPE_IMAGE_IMPORT RAW AI RESPONSE END ===");

            return response;
        } catch (Exception e) {
            System.err.println("RecipeParserService: Failed to parse image raw - " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public RecipeDto parseImageToRecipe(MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            return null;
        }

        try {
            String json = parseImageRaw(imageFile);
            if (json == null || json.trim().isEmpty()) {
                return null;
            }

            RecipeDto dto = objectMapper.readValue(json, RecipeDto.class);

            if (dto.getMetadata() == null) {
                dto.setMetadata(RecipeMetadataDto.builder().build());
            }

            RecipeMetadataDto metadata = dto.getMetadata();
            RecipeMetadataDto updatedMetadata = RecipeMetadataDto.builder()
                    .sourceUrl(null)
                    .author(metadata.getAuthor())
                    .language(metadata.getLanguage())
                    .categories(metadata.getCategories() != null ? metadata.getCategories() : new java.util.ArrayList<>())
                    .imageUrl(metadata.getImageUrl())
                    .calculatorId(metadata.getCalculatorId())
                    .importMethod("image")
                    .build();

            dto.setMetadata(updatedMetadata);

            return dto;
        } catch (Exception e) {
            System.err.println("RecipeParserService: Failed to parse image to recipe - " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public String extractTextFromFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String lowerName = originalFilename != null ? originalFilename.toLowerCase() : "";
            String contentType = file.getContentType();

            if (lowerName.endsWith(".txt") || "text/plain".equalsIgnoreCase(contentType)) {
                return new String(file.getBytes(), StandardCharsets.UTF_8);
            }

            if (lowerName.endsWith(".pdf") || "application/pdf".equalsIgnoreCase(contentType)) {
                try (var pdf = Loader.loadPDF(file.getBytes())) {
                    PDFTextStripper stripper = new PDFTextStripper();
                    return stripper.getText(pdf);
                }
            }

            if (lowerName.endsWith(".docx")
                    || "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                    .equalsIgnoreCase(contentType)) {
                try (ByteArrayInputStream inputStream = new ByteArrayInputStream(file.getBytes());
                     XWPFDocument document = new XWPFDocument(inputStream);
                     XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
                    return extractor.getText();
                }
            }

            return null;
        } catch (Exception e) {
            System.err.println("RecipeParserService: Failed to extract text from file - " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public RecipeDto parseFileToRecipe(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            String extractedText = extractTextFromFile(file);
            if (extractedText == null || extractedText.trim().isEmpty()) {
                return null;
            }

            String normalizedExtractedText = normalizeRecipeText(extractedText);
            if (normalizedExtractedText == null || normalizedExtractedText.isEmpty()) {
                return null;
            }

            logBlock("RECIPE_FILE_EXTRACTED_TEXT", normalizedExtractedText);

            return parseToRecipe(normalizedExtractedText, null);
        } catch (Exception e) {
            System.err.println("RecipeParserService: Failed to parse file to recipe - " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private String normalizeRecipeText(String input) {
        if (input == null) {
            return null;
        }

        String normalized = input;

        normalized = normalized.replace("\r\n", "\n");
        normalized = normalized.replace('\r', '\n');

        normalized = normalized.replace('\u00A0', ' ');

        normalized = normalized.replaceAll("[\\t ]+", " ");
        normalized = normalized.replaceAll(" *\\n *", "\n");
        normalized = normalized.replaceAll("\\n{3,}", "\n\n");

        return normalized.trim();
    }

    private void logBlock(String label, String value) {
        System.out.println("=== " + label + " START ===");
        System.out.println(value);
        System.out.println("=== " + label + " END ===");
    }

}

