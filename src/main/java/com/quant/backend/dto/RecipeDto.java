package com.quant.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeDto {
    private String id;
    private String title;
    private String description;
    private Integer servings;
    
    @Builder.Default
    private List<IngredientDto> ingredients = new ArrayList<>();
    
    @Builder.Default
    private List<RecipeStepDto> steps = new ArrayList<>();
    
    private RecipeMetadataDto metadata;
}

