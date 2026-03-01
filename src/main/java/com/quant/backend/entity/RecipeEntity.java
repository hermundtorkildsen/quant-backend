package com.quant.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recipes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeEntity {
    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "owner_user_id")
    private String ownerUserId;

    @Column(name = "title", nullable = false)
    private String title;
    
    @Column(name = "description", length = 2000)
    private String description;
    
    @Column(name = "servings")
    private Integer servings;

    @Column(name = "shared_from_user_id")
    private String sharedFromUserId;

    @Column(name = "shared_from_username")
    private String sharedFromUsername;

    @Column(name = "shared_original_recipe_id")
    private String sharedOriginalRecipeId;

    @Column(name = "cover_image_id")
    private String coverImageId;

    @Column(name = "is_favorite", nullable = false)
    private boolean favorite;

    @Column(name = "favorited_at")
    private LocalDateTime favoritedAt;

    @Column(name = "last_viewed_at")
    private LocalDateTime lastViewedAt;

    @Column(name = "view_count", nullable = false)
    private int viewCount;

    @Column(name = "is_pinned", nullable = false)
    private boolean pinned;

    @Column(name = "pinned_at")
    private LocalDateTime pinnedAt;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "recipe_ingredients", joinColumns = @JoinColumn(name = "recipe_id"))
    @OrderColumn(name = "ingredient_order")
    private List<IngredientEmbeddable> ingredients = new ArrayList<>();
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "recipe_steps", joinColumns = @JoinColumn(name = "recipe_id"))
    @OrderColumn(name = "step_order")
    private List<RecipeStepEmbeddable> steps = new ArrayList<>();
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "recipe_categories", joinColumns = @JoinColumn(name = "recipe_id"))
    @Column(name = "category")
    @OrderColumn(name = "category_order")
    private List<String> categories = new ArrayList<>();
    
    @Embedded
    private RecipeMetadataEmbeddable metadata;
}

