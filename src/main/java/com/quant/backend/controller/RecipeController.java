package com.quant.backend.controller;

import com.quant.backend.dto.ImportRecipeRequestDto;
import com.quant.backend.dto.RecipeDto;
import com.quant.backend.service.RecipeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @GetMapping
    public List<RecipeDto> getAllRecipes() {
        return recipeService.getAllRecipes();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecipeDto> getRecipeById(@PathVariable String id) {
        return recipeService.getRecipeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<RecipeDto> saveRecipe(@Valid @RequestBody RecipeDto recipeDto) {
        RecipeDto saved = recipeService.saveRecipe(recipeDto);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable String id) {
        boolean deleted = recipeService.deleteRecipe(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/import-text")
    public ResponseEntity<RecipeDto> importRecipeFromText(@RequestBody ImportRecipeRequestDto request) {
        RecipeDto recipe = recipeService.importRecipeFromText(request);
        return ResponseEntity.ok(recipe);
    }

    public record ShareRecipeRequest(String toUsername) {}

    @PostMapping("/{id}/share")
    public ResponseEntity<RecipeDto> shareRecipe(
            @PathVariable String id,
            @RequestBody ShareRecipeRequest req
    ) {
        RecipeDto sharedCopy = recipeService.shareRecipe(id, req.toUsername());
        return ResponseEntity.ok(sharedCopy);
    }

}

