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
        return jpaRepository.findByIdAndOwnerUserId(id, userId).map(this::entityToDto);
    }

    @Override
    public RecipeDto saveForUser(String userId, RecipeDto recipe) {
        if (recipe.getId() == null || recipe.getId().isEmpty()) {
            recipe.setId(UUID.randomUUID().toString());
        }

        RecipeEntity entity = dtoToEntity(recipe);
        entity.setOwnerUserId(userId);

        // settes FØR save (og helst i dtoToEntity også, men ok her)
        entity.setSharedFromUserId(recipe.getSharedFromUserId());
        entity.setSharedFromUsername(recipe.getSharedFromUsername());
        entity.setSharedOriginalRecipeId(recipe.getSharedOriginalRecipeId());

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

    private RecipeEntity dtoToEntity(RecipeDto dto) {
        RecipeEntity entity = new RecipeEntity();
        entity.setId(dto.getId());
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setServings(dto.getServings());

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

        if (dto.getSteps() != null) {
            entity.setSteps(dto.getSteps().stream()
                    .map(step -> new RecipeStepEmbeddable(
                            step.getStep(),
                            step.getInstruction(),
                            step.getNotes()
                    ))
                    .toList());
        }

        if (dto.getMetadata() != null && dto.getMetadata().getCategories() != null) {
            entity.setCategories(new ArrayList<>(dto.getMetadata().getCategories()));
        }

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

        return entity;
    }

    private RecipeDto entityToDto(RecipeEntity entity) {
        RecipeDto dto = new RecipeDto();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setServings(entity.getServings());

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

        if (entity.getSteps() != null) {
            dto.setSteps(entity.getSteps().stream()
                    .map(step -> new RecipeStepDto(
                            step.getStep(),
                            step.getInstruction(),
                            step.getNotes()
                    ))
                    .toList());
        }

        if (entity.getMetadata() != null) {
            RecipeMetadataEmbeddable meta = entity.getMetadata();
            RecipeMetadataDto metadata = RecipeMetadataDto.builder()
                    .sourceUrl(meta.getSourceUrl())
                    .author(meta.getAuthor())
                    .language(meta.getLanguage())
                    .categories(entity.getCategories() != null ? new ArrayList<>(entity.getCategories()) : new ArrayList<>())
                    .imageUrl(meta.getImageUrl())
                    .calculatorId(meta.getCalculatorId())
                    .importMethod(meta.getImportMethod())
                    .build();
            dto.setMetadata(metadata);
        }

        dto.setSharedFromUserId(entity.getSharedFromUserId());
        dto.setSharedFromUsername(entity.getSharedFromUsername());
        dto.setSharedOriginalRecipeId(entity.getSharedOriginalRecipeId());

        return dto;
    }

    @Override
    public List<RecipeDto> findAllAdmin() {
        return jpaRepository.findAll().stream()
                .map(this::entityToDto)
                .toList();
    }

    @Override
    public Optional<RecipeDto> findByIdAdmin(String id) {
        return jpaRepository.findById(id).map(this::entityToDto);
    }

    @Override
    public RecipeDto saveAdmin(RecipeDto recipe) {
        if (recipe.getId() == null || recipe.getId().isEmpty()) {
            recipe.setId(UUID.randomUUID().toString());
        }

        // Viktig: bevar owner_user_id hvis den allerede finnes i DB
        RecipeEntity entity = dtoToEntity(recipe);

        var existing = jpaRepository.findById(recipe.getId());
        existing.ifPresent(e -> entity.setOwnerUserId(e.getOwnerUserId()));

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
