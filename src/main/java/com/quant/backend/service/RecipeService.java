package com.quant.backend.service;

import com.quant.backend.dto.ImportRecipeRequestDto;
import com.quant.backend.dto.RecipeDto;
import com.quant.backend.dto.RecipeMetadataDto;
import com.quant.backend.repository.RecipeRepository;
import org.springframework.stereotype.Service;
import com.quant.backend.auth.QuantPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;

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
        return recipeRepository.findAllForUser(currentUserId());
    }

    public Optional<RecipeDto> getRecipeById(String id) {
        return recipeRepository.findByIdForUser(currentUserId(), id);
    }

    public RecipeDto saveRecipe(RecipeDto recipe) {
        return recipeRepository.saveForUser(currentUserId(), recipe);
    }

    public boolean deleteRecipe(String id) {
        return recipeRepository.deleteByIdForUser(currentUserId(), id);
    }

    // 👇 ALT UNDER HER ER UENDRET

    public RecipeDto importRecipeFromText(ImportRecipeRequestDto request) {

        RecipeDto aiRecipe = recipeParserService.parseToRecipe(
                request.getText(),
                request.getSourceUrl()
        );

        if (aiRecipe != null) {
            if (aiRecipe.getDescription() == null || aiRecipe.getDescription().isBlank()) {
                aiRecipe.setDescription(
                        "Beskrivelse ikke funnet i teksten. Rediger gjerne denne oppskriften."
                );
            }
            return aiRecipe;
        }

        System.err.println("RecipeService: AI parsing failed, using stub fallback");

        return RecipeDto.builder()
                .id(UUID.randomUUID().toString())
                .title("Imported: " +
                        request.getText().substring(0,
                                Math.min(50, request.getText().length())))
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

    private String currentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new IllegalArgumentException("Not authenticated");
        }
        Object p = auth.getPrincipal();
        if (p instanceof QuantPrincipal qp) {
            return qp.userId();
        }
        throw new IllegalArgumentException("Not authenticated");
    }


}
