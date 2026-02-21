package com.quant.backend.service;

import com.quant.backend.entity.RecipeShareEntity.Status;
import com.quant.backend.repository.RecipeShareJpaRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class RecipeShareCleanupJob {

    private final RecipeShareJpaRepository shareRepo;

    public RecipeShareCleanupJob(RecipeShareJpaRepository shareRepo) {
        this.shareRepo = shareRepo;
    }

    // Hver natt kl 03:15
    @Scheduled(cron = "0 15 3 * * *")
    @Transactional
    public void cleanupOldShares() {

        // 30 dager: fjern ACCEPTED/DECLINED
        LocalDateTime handledCutoff = LocalDateTime.now().minusDays(30);
        shareRepo.deleteHandledOlderThan(List.of(Status.ACCEPTED, Status.DECLINED), handledCutoff);

        // (Valgfritt) 90 dager: fjern PENDING som aldri ble håndtert
        // Hvis du vil være helt trygg på "ikke søppel":
        // LocalDateTime pendingCutoff = LocalDateTime.now().minusDays(90);
        // shareRepo.deletePendingOlderThan(Status.PENDING, pendingCutoff); // krever en ekstra repo-metode
    }
}