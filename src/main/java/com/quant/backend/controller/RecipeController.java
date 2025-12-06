package com.quant.backend.controller;

import com.quant.backend.dto.IngredientDto;
import com.quant.backend.dto.ImportRecipeRequestDto;
import com.quant.backend.dto.RecipeDto;
import com.quant.backend.dto.RecipeMetadataDto;
import com.quant.backend.dto.RecipeStepDto;
import jakarta.annotation.PostConstruct;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final Map<String, RecipeDto> recipesStore = new ConcurrentHashMap<>();

    @PostConstruct
    public void initializeData() {
        RecipeDto recipe1 = RecipeDto.builder()
                .id("1")
                .title("Classic Margherita Pizza")
                .description("A traditional Italian pizza with fresh tomatoes, mozzarella, and basil")
                .servings(4)
                .ingredients(Arrays.asList(
                        new IngredientDto(500.0, "g", "pizza dough", "Store-bought or homemade"),
                        new IngredientDto(400.0, "g", "canned tomatoes", "San Marzano preferred"),
                        new IngredientDto(250.0, "g", "fresh mozzarella", "Buffalo mozzarella if available"),
                        new IngredientDto(20.0, "g", "fresh basil leaves", null),
                        new IngredientDto(2.0, "tbsp", "extra virgin olive oil", null),
                        new IngredientDto(null, null, "salt", "To taste"),
                        new IngredientDto(null, null, "black pepper", "To taste")
                ))
                .steps(Arrays.asList(
                        new RecipeStepDto(1, "Preheat your oven to 250°C (480°F). If using a pizza stone, place it in the oven while preheating.", null),
                        new RecipeStepDto(2, "Roll out the pizza dough on a floured surface to your desired thickness.", null),
                        new RecipeStepDto(3, "Spread the canned tomatoes evenly over the dough, leaving a small border for the crust.", "Crush the tomatoes by hand for a more rustic texture"),
                        new RecipeStepDto(4, "Tear the mozzarella into pieces and distribute evenly over the tomatoes.", null),
                        new RecipeStepDto(5, "Drizzle with olive oil and season with salt and pepper.", null),
                        new RecipeStepDto(6, "Bake for 10-12 minutes until the crust is golden and cheese is bubbly.", null),
                        new RecipeStepDto(7, "Remove from oven, top with fresh basil leaves, and serve immediately.", null)
                ))
                .metadata(RecipeMetadataDto.builder()
                        .sourceUrl("https://example.com/margherita-pizza")
                        .author("Italian Cooking Academy")
                        .language("en")
                        .categories(Arrays.asList("Italian", "Vegetarian", "Main Course"))
                        .imageUrl("https://example.com/images/margherita.jpg")
                        .calculatorId("calc-001")
                        .importMethod("manual")
                        .build())
                .build();

        RecipeDto recipe2 = RecipeDto.builder()
                .id("2")
                .title("Chocolate Chip Cookies")
                .description("Soft and chewy homemade chocolate chip cookies")
                .servings(24)
                .ingredients(Arrays.asList(
                        new IngredientDto(225.0, "g", "all-purpose flour", null),
                        new IngredientDto(1.0, "tsp", "baking soda", null),
                        new IngredientDto(1.0, "tsp", "salt", null),
                        new IngredientDto(170.0, "g", "butter", "Softened"),
                        new IngredientDto(150.0, "g", "brown sugar", "Packed"),
                        new IngredientDto(100.0, "g", "white sugar", null),
                        new IngredientDto(2.0, null, "eggs", "Large"),
                        new IngredientDto(2.0, "tsp", "vanilla extract", null),
                        new IngredientDto(300.0, "g", "chocolate chips", "Semi-sweet")
                ))
                .steps(Arrays.asList(
                        new RecipeStepDto(1, "Preheat oven to 190°C (375°F). Line baking sheets with parchment paper.", null),
                        new RecipeStepDto(2, "In a medium bowl, whisk together flour, baking soda, and salt. Set aside.", null),
                        new RecipeStepDto(3, "In a large bowl, cream together the softened butter and both sugars until light and fluffy.", null),
                        new RecipeStepDto(4, "Beat in the eggs one at a time, then stir in the vanilla extract.", null),
                        new RecipeStepDto(5, "Gradually blend in the flour mixture until just combined.", "Don't overmix"),
                        new RecipeStepDto(6, "Stir in the chocolate chips.", null),
                        new RecipeStepDto(7, "Drop rounded tablespoons of dough onto the prepared baking sheets, spacing them about 5cm apart.", null),
                        new RecipeStepDto(8, "Bake for 9-11 minutes until edges are golden brown. Centers will look slightly underbaked.", null),
                        new RecipeStepDto(9, "Cool on baking sheet for 5 minutes before transferring to wire rack to cool completely.", null)
                ))
                .metadata(RecipeMetadataDto.builder()
                        .sourceUrl("https://example.com/chocolate-chip-cookies")
                        .author("Grandma's Recipe Collection")
                        .language("en")
                        .categories(Arrays.asList("Dessert", "Baking", "Cookies"))
                        .imageUrl("https://example.com/images/cookies.jpg")
                        .calculatorId("calc-002")
                        .importMethod("manual")
                        .build())
                .build();

        recipesStore.put(recipe1.getId(), recipe1);
        recipesStore.put(recipe2.getId(), recipe2);
    }

    @GetMapping
    public List<RecipeDto> getAllRecipes() {
        return new ArrayList<>(recipesStore.values());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecipeDto> getRecipeById(@PathVariable String id) {
        RecipeDto recipe = recipesStore.get(id);
        if (recipe == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(recipe);
    }

    @PostMapping
    public ResponseEntity<RecipeDto> saveRecipe(@RequestBody RecipeDto recipeDto) {
        // If no ID provided, generate a new UUID
        if (recipeDto.getId() == null || recipeDto.getId().isEmpty()) {
            recipeDto.setId(UUID.randomUUID().toString());
        }

        // Store the recipe
        recipesStore.put(recipeDto.getId(), recipeDto);

        // Return the saved recipe with 200 OK
        return ResponseEntity.ok(recipeDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable String id) {
        RecipeDto removed = recipesStore.remove(id);

        if (removed == null) {
            // Recipe not found - return 404
            return ResponseEntity.notFound().build();
        }

        // Successfully deleted - return 204 No Content
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/import-text")
    public ResponseEntity<RecipeDto> importRecipeFromText(@RequestBody ImportRecipeRequestDto request) {
        // TODO: Later this will call an AI service to parse the text
        // For now, return a minimal recipe with the text as title

        RecipeDto stubRecipe = RecipeDto.builder()
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

        // Don't auto-save it - let the Flutter client decide whether to save it
        return ResponseEntity.ok(stubRecipe);
    }

}

