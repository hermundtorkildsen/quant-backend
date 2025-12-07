package com.quant.backend.service;

import com.quant.backend.dto.ImportRecipeRequestDto;
import com.quant.backend.dto.RecipeDto;
import com.quant.backend.dto.RecipeMetadataDto;
import com.quant.backend.repository.RecipeRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final RecipeParserService recipeParserService;

    public RecipeService(RecipeRepository recipeRepository, RecipeParserService recipeParserService) {
        this.recipeRepository = recipeRepository;
        this.recipeParserService = recipeParserService;
    }

    public List<RecipeDto> getAllRecipes() {
        return recipeRepository.findAll();
    }

    public Optional<RecipeDto> getRecipeById(String id) {
        return recipeRepository.findById(id);
    }

    public RecipeDto saveRecipe(RecipeDto recipe) {
        // same behavior as before, just moved out of controller
        return recipeRepository.save(recipe);
    }

    public boolean deleteRecipe(String id) {
        return recipeRepository.deleteById(id);
    }

    public RecipeDto importRecipeFromText(ImportRecipeRequestDto request) {
        // Try AI parsing first
        RecipeDto aiRecipe = recipeParserService.parseToRecipe(request.getText(), request.getSourceUrl());

        if (aiRecipe != null) {
            if (aiRecipe.getDescription() == null || aiRecipe.getDescription().isBlank()) {
                aiRecipe.setDescription("Beskrivelse ikke funnet i teksten. Rediger gjerne denne oppskriften.");
            }
            return aiRecipe;
        }
        
        // AI parsing failed - fall back to stub implementation
        System.err.println("RecipeService: AI parsing failed, using stub fallback");
        
        // KEEP behavior compatible with existing stub implementation:
        // same JSON structure, same fields, allowed to change text content
        return RecipeDto.builder()
                .id(UUID.randomUUID().toString())
                .title("Imported: " + request.getText().substring(0, Math.min(50, request.getText().length())))
                .description(request.getText())
                .servings(4)
                .ingredients(new ArrayList<>())
                .steps(new ArrayList<>())
                .metadata(RecipeMetadataDto.builder()
                        .sourceUrl(request.getSourceUrl())
                        .importMethod("stub")
                        .categories(new ArrayList<>())
                        .build())
                .build();
    }

}

