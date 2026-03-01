package com.quant.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class RecipeDto {
    private String id;

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title is too long")
    private String title;

    @Size(max = 5000, message = "Description is too long")
    private String description;

    @Min(value = 1, message = "Servings must be at least 1")
    private Integer servings;

    @Valid
    @Builder.Default
    private List<IngredientDto> ingredients = new ArrayList<>();

    @Valid
    @Builder.Default
    private List<RecipeStepDto> steps = new ArrayList<>();

    @Valid
    private RecipeMetadataDto metadata;

    private String sharedFromUserId;
    private String sharedFromUsername;
    private String sharedOriginalRecipeId;
    private String coverImageId;
    private Boolean favorite;
    private java.time.LocalDateTime favoritedAt;
    private java.time.LocalDateTime lastViewedAt;
    private Integer viewCount;
    private Boolean pinned;
    private java.time.LocalDateTime pinnedAt;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
}
