package com.quant.backend.repository;

import com.quant.backend.dto.RecipeDto;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryRecipeRepository implements RecipeRepository {

    private final Map<String, RecipeDto> store = new ConcurrentHashMap<>();

    @Override
    public List<RecipeDto> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public Optional<RecipeDto> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public RecipeDto save(RecipeDto recipe) {
        if (recipe.getId() == null || recipe.getId().isEmpty()) {
            recipe.setId(UUID.randomUUID().toString());
        }
        store.put(recipe.getId(), recipe);
        return recipe;
    }

    @Override
    public boolean deleteById(String id) {
        return store.remove(id) != null;
    }

    @Override
    public boolean existsById(String id) {
        return store.containsKey(id);
    }

}

