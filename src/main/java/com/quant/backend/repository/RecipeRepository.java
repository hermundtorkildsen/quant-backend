package com.quant.backend.repository;

import com.quant.backend.dto.RecipeDto;

import java.util.List;
import java.util.Optional;

public interface RecipeRepository {

    List<RecipeDto> findAllForUser(String userId);

    Optional<RecipeDto> findByIdForUser(String userId, String id);

    RecipeDto saveForUser(String userId, RecipeDto recipe);

    boolean deleteByIdForUser(String userId, String id);

    boolean existsByIdForUser(String userId, String id);
}

