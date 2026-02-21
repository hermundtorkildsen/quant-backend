package com.quant.backend.service;

import com.quant.backend.auth.UserJpaRepository;
import com.quant.backend.controller.RecipeShareController;
import com.quant.backend.dto.*;
import com.quant.backend.entity.RecipeShareEntity;
import com.quant.backend.repository.RecipeRepository;
import com.quant.backend.repository.RecipeShareJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

@Service
public class RecipeShareService {

    private final RecipeShareJpaRepository shareRepo;
    private final UserJpaRepository userRepo;
    private final RecipeRepository recipeRepository;

    public RecipeShareService(
            RecipeShareJpaRepository shareRepo,
            UserJpaRepository userRepo,
            RecipeRepository recipeRepository
    ) {
        this.shareRepo = shareRepo;
        this.userRepo = userRepo;
        this.recipeRepository = recipeRepository;
    }

    @Transactional
    public RecipeDto accept(String shareId, String userId) {

        // LOCK share-rowen for å unngå dobbel-accept (dobbelklikk / retries)
        var share = shareRepo.findForUpdate(shareId, userId)
                .orElseThrow(() -> new RuntimeException("Share not found"));

        // Idempotency: hvis allerede ACCEPTED -> returner samme importerte oppskrift
        if (share.getStatus() == RecipeShareEntity.Status.ACCEPTED) {
            if (share.getImportedRecipeId() == null) {
                throw new RuntimeException("Share already accepted, but importedRecipeId is missing");
            }
            return recipeRepository.findByIdForUser(userId, share.getImportedRecipeId())
                    .orElseThrow(() -> new RuntimeException("Imported recipe not found"));
        }

        // Hvis ikke pending, så er den håndtert (DECLINED) -> gi tydelig feil
        if (share.getStatus() != RecipeShareEntity.Status.PENDING) {
            throw new RuntimeException("Share is not pending");
        }

        // hent original oppskrift (fra avsender)
        var original = recipeRepository.findByIdForUser(share.getFromUserId(), share.getRecipeId())
                .orElseThrow(() -> new RuntimeException("Original recipe not found"));

        // lag kopi til mottaker (ny id)
        var copy = deepCopyRecipe(original);
        copy.setId(UUID.randomUUID().toString());

        // behold hvem den kom fra (for "Delt av ...")
        copy.setSharedFromUserId(share.getFromUserId());
        copy.setSharedFromUsername(
                userRepo.findById(share.getFromUserId()).map(u -> u.getUsername()).orElse(null)
        );
        copy.setSharedOriginalRecipeId(share.getRecipeId());

        var saved = recipeRepository.saveForUser(userId, copy);

        // marker share som accepted + lagre importedRecipeId
        share.setStatus(RecipeShareEntity.Status.ACCEPTED);
        share.setHandledAt(LocalDateTime.now());
        share.setImportedRecipeId(saved.getId());
        shareRepo.save(share);

        return saved;
    }

    @Transactional
    public void decline(String shareId, String userId) {

        var share = shareRepo.findForUpdate(shareId, userId)
                .orElseThrow(() -> new RuntimeException("Share not found"));

        // Idempotency: hvis allerede DECLINED -> ok
        if (share.getStatus() == RecipeShareEntity.Status.DECLINED) {
            return;
        }

        // Hvis allerede ACCEPTED, ikke la den "declines" etterpå
        if (share.getStatus() == RecipeShareEntity.Status.ACCEPTED) {
            throw new RuntimeException("Share already accepted");
        }

        if (share.getStatus() != RecipeShareEntity.Status.PENDING) {
            throw new RuntimeException("Share is not pending");
        }

        share.setStatus(RecipeShareEntity.Status.DECLINED);
        share.setHandledAt(LocalDateTime.now());
        shareRepo.save(share);
    }

    private RecipeDto deepCopyRecipe(RecipeDto r) {
        var copy = new RecipeDto();
        copy.setTitle(r.getTitle());
        copy.setDescription(r.getDescription());
        copy.setServings(r.getServings());

        if (r.getIngredients() != null) {
            copy.setIngredients(r.getIngredients().stream()
                    .map(i -> new IngredientDto(
                            i.getAmount(), i.getUnit(), i.getItem(), i.getNotes(), i.getSection()
                    )).toList());
        }

        if (r.getSteps() != null) {
            copy.setSteps(r.getSteps().stream()
                    .map(s -> new RecipeStepDto(
                            s.getStep(), s.getInstruction(), s.getNotes()
                    )).toList());
        }

        if (r.getMetadata() != null) {
            var m = r.getMetadata();
            copy.setMetadata(RecipeMetadataDto.builder()
                    .sourceUrl(m.getSourceUrl())
                    .author(m.getAuthor())
                    .language(m.getLanguage())
                    .categories(m.getCategories() != null ? new ArrayList<>(m.getCategories()) : new ArrayList<>())
                    .imageUrl(m.getImageUrl())
                    .calculatorId(m.getCalculatorId())
                    .importMethod(m.getImportMethod())
                    .build());
        }

        return copy;
    }

    public long inboxCount(String userId) {
        return shareRepo.countByToUserIdAndStatus(userId, RecipeShareEntity.Status.PENDING);
    }

    public java.util.List<RecipeShareController.InboxShareItem> inbox(String userId) {
        var shares = shareRepo.findByToUserIdAndStatusOrderByCreatedAtDesc(
                userId, RecipeShareEntity.Status.PENDING
        );

        return shares.stream().map(s -> {
            String fromUsername = userRepo.findById(s.getFromUserId())
                    .map(u -> u.getUsername())
                    .orElse("ukjent");

            return new RecipeShareController.InboxShareItem(
                    s.getId(),
                    s.getRecipeId(),
                    fromUsername,
                    s.getMessage(),
                    s.getCreatedAt()
            );
        }).toList();
    }

}
