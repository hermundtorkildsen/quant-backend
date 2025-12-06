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

    public RecipeService(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
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
        // KEEP behavior compatible with existing stub implementation:
        // same JSON structure, same fields, allowed to change text content
        return RecipeDto.builder()
                .id(UUID.randomUUID().toString())
                .title("Imported: " + request.getText().substring(0, Math.min(50, request.getText().length())))
                .description("This is a stub. AI parsing will be implemented later.")
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

