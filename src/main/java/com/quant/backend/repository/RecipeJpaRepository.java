package com.quant.backend.repository;

import com.quant.backend.entity.RecipeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipeJpaRepository extends JpaRepository<RecipeEntity, String> {
    List<RecipeEntity> findAllByOwnerUserId(String ownerUserId);
    Optional<RecipeEntity> findByIdAndOwnerUserId(String id, String ownerUserId);
    boolean existsByIdAndOwnerUserId(String id, String ownerUserId);
}