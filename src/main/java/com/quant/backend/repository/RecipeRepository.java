package com.quant.backend.repository;

import com.quant.backend.dto.RecipeDto;

import java.util.List;
import java.util.Optional;

public interface RecipeRepository {

    List<RecipeDto> findAll();

    Optional<RecipeDto> findById(String id);

    RecipeDto save(RecipeDto recipe);

    boolean deleteById(String id);

    boolean existsById(String id);

}

