package com.quant.backend.repository;

import com.quant.backend.dto.*;
import com.quant.backend.entity.*;
import org.springframework.context.annotation.Profile;
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
    public List<RecipeDto> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::entityToDto)
                .toList();
    }

    @Override
    public Optional<RecipeDto> findById(String id) {
        return jpaRepository.findById(id)
                .map(this::entityToDto);
    }

    @Override
    public RecipeDto save(RecipeDto recipe) {
        if (recipe.getId() == null || recipe.getId().isEmpty()) {
            recipe.setId(UUID.randomUUID().toString());
        }
        
        RecipeEntity entity = dtoToEntity(recipe);
        RecipeEntity saved = jpaRepository.save(entity);
        return entityToDto(saved);
    }

    @Override
    public boolean deleteById(String id) {
        if (!jpaRepository.existsById(id)) {
            return false;
        }
        jpaRepository.deleteById(id);
        return true;
    }

    @Override
    public boolean existsById(String id) {
        return jpaRepository.existsById(id);
    }

    private RecipeEntity dtoToEntity(RecipeDto dto) {
        RecipeEntity entity = new RecipeEntity();
        entity.setId(dto.getId());
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setServings(dto.getServings());
        
        // Convert ingredients
        if (dto.getIngredients() != null) {
            entity.setIngredients(dto.getIngredients().stream()
                    .map(ing -> new IngredientEmbeddable(
                            ing.getAmount(),
                            ing.getUnit(),
                            ing.getItem(),
                            ing.getNotes()
                    ))
                    .toList());
        }
        
        // Convert steps
        if (dto.getSteps() != null) {
            entity.setSteps(dto.getSteps().stream()
                    .map(step -> new RecipeStepEmbeddable(
                            step.getStep(),
                            step.getInstruction(),
                            step.getNotes()
                    ))
                    .toList());
        }
        
        // Convert categories
        if (dto.getMetadata() != null && dto.getMetadata().getCategories() != null) {
            entity.setCategories(new ArrayList<>(dto.getMetadata().getCategories()));
        }
        
        // Convert metadata
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
        
        // Convert ingredients
        if (entity.getIngredients() != null) {
            dto.setIngredients(entity.getIngredients().stream()
                    .map(ing -> new IngredientDto(
                            ing.getAmount(),
                            ing.getUnit(),
                            ing.getItem(),
                            ing.getNotes()
                    ))
                    .toList());
        }
        
        // Convert steps
        if (entity.getSteps() != null) {
            dto.setSteps(entity.getSteps().stream()
                    .map(step -> new RecipeStepDto(
                            step.getStep(),
                            step.getInstruction(),
                            step.getNotes()
                    ))
                    .toList());
        }
        
        // Convert metadata
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
        
        return dto;
    }
}

