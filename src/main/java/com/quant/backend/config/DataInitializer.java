package com.quant.backend.config;

import com.quant.backend.dto.*;
import com.quant.backend.repository.RecipeRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class DataInitializer {

    private final RecipeRepository recipeRepository;

    public DataInitializer(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    @PostConstruct
    public void initializeSampleData() {
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

        recipeRepository.save(recipe1);
        recipeRepository.save(recipe2);
    }

}

