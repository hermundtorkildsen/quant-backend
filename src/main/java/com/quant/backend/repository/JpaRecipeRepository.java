package com.quant.backend.repository;

import com.quant.backend.dto.*;
import com.quant.backend.entity.*;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaRecipeRepository implements RecipeRepository {

    private final RecipeJpaRepository jpaRepository;

    public JpaRecipeRepository(RecipeJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<RecipeDto> findAllForUser(String userId) {
        return jpaRepository.findAllByOwnerUserId(userId).stream()
                .map(this::entityToDto)
                .toList();
    }

    @Override
    public Optional<RecipeDto> findByIdForUser(String userId, String id) {
        return jpaRepository.findByIdAndOwnerUserId(id, userId)
                .map(this::entityToDto);
    }

    @Override
    public RecipeDto saveForUser(String userId, RecipeDto recipe) {

        if (recipe.getId() == null || recipe.getId().isEmpty()) {
            recipe.setId(UUID.randomUUID().toString());
        }

        RecipeEntity entity = dtoToEntity(recipe);

        // Alltid sett owner eksplisitt
        entity.setOwnerUserId(userId);

        RecipeEntity saved = jpaRepository.save(entity);
        return entityToDto(saved);
    }

    @Override
    public boolean deleteByIdForUser(String userId, String id) {
        if (!jpaRepository.existsByIdAndOwnerUserId(id, userId)) {
            return false;
        }
        jpaRepository.deleteById(id);
        return true;
    }

    @Override
    public boolean existsByIdForUser(String userId, String id) {
        return jpaRepository.existsByIdAndOwnerUserId(id, userId);
    }

    // --------------------------------------------------
    // ENTITY MAPPING
    // --------------------------------------------------

    private RecipeEntity dtoToEntity(RecipeDto dto) {
        RecipeEntity entity = new RecipeEntity();

        entity.setId(dto.getId());
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setServings(dto.getServings());
        entity.setCoverImageId(dto.getCoverImageId());

        entity.setFavorite(Boolean.TRUE.equals(dto.getFavorite()));
        entity.setFavoritedAt(dto.getFavoritedAt());
        entity.setLastViewedAt(dto.getLastViewedAt());
        entity.setViewCount(dto.getViewCount() != null ? dto.getViewCount() : 0);

        entity.setPinned(Boolean.TRUE.equals(dto.getPinned()));
        entity.setPinnedAt(dto.getPinnedAt());

        // ------------------
        // Ingredients
        // ------------------

        if (dto.getIngredients() != null) {
            entity.setIngredients(dto.getIngredients().stream()
                    .map(ing -> new IngredientEmbeddable(
                            ing.getAmount(),
                            ing.getUnit(),
                            ing.getItem(),
                            ing.getNotes(),
                            ing.getSection()
                    ))
                    .toList());
        }

        // ------------------
        // Steps
        // ------------------

        if (dto.getSteps() != null) {
            entity.setSteps(dto.getSteps().stream()
                    .map(step -> new RecipeStepEmbeddable(
                            step.getStep(),
                            step.getInstruction(),
                            step.getNotes()
                    ))
                    .toList());
        }

        // ------------------
        // Categories
        // ------------------

        if (dto.getMetadata() != null && dto.getMetadata().getCategories() != null) {
            entity.setCategories(new ArrayList<>(dto.getMetadata().getCategories()));
        }

        // ------------------
        // Metadata
        // ------------------

        if (dto.getMetadata() != null) {
            RecipeMetadataDto meta = dto.getMetadata();

            RecipeMetadataEmbeddable metadata = new RecipeMetadataEmbeddable();
            metadata.setSourceUrl(meta.getSourceUrl());
            metadata.setAuthor(meta.getAuthor());
            metadata.setLanguage(meta.getLanguage());
            metadata.setImageUrl(meta.getImageUrl());
            metadata.setCalculatorId(meta.getCalculatorId());
            metadata.setImportMethod(meta.getImportMethod());

            entity.setMetadata(metadata);
        }

        // ------------------
        // Share fields (nå sentralt mappet)
        // ------------------

        entity.setSharedFromUserId(dto.getSharedFromUserId());
        entity.setSharedFromUsername(dto.getSharedFromUsername());
        entity.setSharedOriginalRecipeId(dto.getSharedOriginalRecipeId());

        return entity;
    }

    private RecipeDto entityToDto(RecipeEntity entity) {
        RecipeDto dto = new RecipeDto();

        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setServings(entity.getServings());
        dto.setCoverImageId(entity.getCoverImageId());

        dto.setFavorite(entity.isFavorite());
        dto.setFavoritedAt(entity.getFavoritedAt());
        dto.setLastViewedAt(entity.getLastViewedAt());
        dto.setViewCount(entity.getViewCount());

        dto.setPinned(entity.isPinned());
        dto.setPinnedAt(entity.getPinnedAt());

        // ------------------
        // Ingredients
        // ------------------

        if (entity.getIngredients() != null) {
            dto.setIngredients(entity.getIngredients().stream()
                    .map(ing -> new IngredientDto(
                            ing.getAmount(),
                            ing.getUnit(),
                            ing.getItem(),
                            ing.getNotes(),
                            ing.getSection()
                    ))
                    .toList());
        }

        // ------------------
        // Steps
        // ------------------

        if (entity.getSteps() != null) {
            dto.setSteps(entity.getSteps().stream()
                    .map(step -> new RecipeStepDto(
                            step.getStep(),
                            step.getInstruction(),
                            step.getNotes()
                    ))
                    .toList());
        }

        // ------------------
        // Metadata
        // ------------------

        if (entity.getMetadata() != null) {
            RecipeMetadataEmbeddable meta = entity.getMetadata();

            RecipeMetadataDto metadata = RecipeMetadataDto.builder()
                    .sourceUrl(meta.getSourceUrl())
                    .author(meta.getAuthor())
                    .language(meta.getLanguage())
                    .categories(entity.getCategories() != null
                            ? new ArrayList<>(entity.getCategories())
                            : new ArrayList<>())
                    .imageUrl(meta.getImageUrl())
                    .calculatorId(meta.getCalculatorId())
                    .importMethod(meta.getImportMethod())
                    .build();

            dto.setMetadata(metadata);
        }

        // ------------------
        // Share fields
        // ------------------

        dto.setSharedFromUserId(entity.getSharedFromUserId());
        dto.setSharedFromUsername(entity.getSharedFromUsername());
        dto.setSharedOriginalRecipeId(entity.getSharedOriginalRecipeId());

        return dto;
    }

    // --------------------------------------------------
    // ADMIN (kun brukt til read)
    // --------------------------------------------------

    @Override
    public List<RecipeDto> findAllAdmin() {
        return jpaRepository.findAll().stream()
                .map(this::entityToDto)
                .toList();
    }

    @Override
    public Optional<RecipeDto> findByIdAdmin(String id) {
        return jpaRepository.findById(id)
                .map(this::entityToDto);
    }

    @Override
    public RecipeDto saveAdmin(RecipeDto recipe) {

        if (recipe.getId() == null || recipe.getId().isEmpty()) {
            throw new IllegalArgumentException("Admin save requires an existing recipe id");
        }

        var existing = jpaRepository.findById(recipe.getId());
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Admin save can only update existing recipes");
        }

        RecipeEntity entity = dtoToEntity(recipe);

        // Bevar owner fra DB (admin skal aldri ta eierskap)
        entity.setOwnerUserId(existing.get().getOwnerUserId());

        RecipeEntity saved = jpaRepository.save(entity);
        return entityToDto(saved);
    }

    @Override
    public boolean deleteByIdAdmin(String id) {
        if (!jpaRepository.existsById(id)) return false;
        jpaRepository.deleteById(id);
        return true;
    }
}