package com.quant.backend.repository;

import com.quant.backend.entity.RecipeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeJpaRepository extends JpaRepository<RecipeEntity, String> {
}

