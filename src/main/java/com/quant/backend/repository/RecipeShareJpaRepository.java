package com.quant.backend.repository;

import com.quant.backend.entity.RecipeShareEntity;
import com.quant.backend.entity.RecipeShareEntity.Status;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;
import java.util.Optional;

public interface RecipeShareJpaRepository extends JpaRepository<RecipeShareEntity, String> {

    List<RecipeShareEntity> findByToUserIdAndStatusOrderByCreatedAtDesc(String toUserId, Status status);

    long countByToUserIdAndStatus(String toUserId, Status status);

    Optional<RecipeShareEntity> findByIdAndToUserId(String id, String toUserId);

    Optional<RecipeShareEntity> findByIdAndToUserIdAndStatus(String id, String toUserId, Status status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from RecipeShareEntity s where s.id = :id and s.toUserId = :toUserId")
    Optional<RecipeShareEntity> findForUpdate(@Param("id") String id, @Param("toUserId") String toUserId);
    @Modifying
    @Query("delete from RecipeShareEntity s where s.status in :statuses and s.handledAt < :cutoff")
    int deleteHandledOlderThan(@Param("statuses") List<Status> statuses, @Param("cutoff") java.time.LocalDateTime cutoff);


}
