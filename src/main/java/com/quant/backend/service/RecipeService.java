package com.quant.backend.service;

import com.quant.backend.auth.AdminAccess;
import com.quant.backend.auth.QuantPrincipal;
import com.quant.backend.auth.UserEntity;
import com.quant.backend.auth.UserJpaRepository;
import com.quant.backend.dto.ImportRecipeRequestDto;
import com.quant.backend.dto.RecipeDto;
import com.quant.backend.dto.RecipeMetadataDto;
import com.quant.backend.entity.RecipeShareEntity;
import com.quant.backend.repository.RecipeRepository;
import com.quant.backend.repository.RecipeShareJpaRepository;
import com.quant.backend.service.RecipeParserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final RecipeParserService recipeParserService;
    private final AdminAccess adminAccess;
    private final UserJpaRepository userRepo;
    private final RecipeShareJpaRepository recipeShareRepo;

    @Value("${quant.allow-admin-mutations:false}")
    private boolean allowAdminMutations; // (ikke brukt når admin er read-only)

    public RecipeService(RecipeRepository recipeRepository,
                         RecipeParserService recipeParserService,
                         AdminAccess adminAccess,
                         UserJpaRepository userRepo,
                         RecipeShareJpaRepository recipeShareRepo) {
        this.recipeRepository = recipeRepository;
        this.recipeParserService = recipeParserService;
        this.adminAccess = adminAccess;
        this.userRepo = userRepo;
        this.recipeShareRepo = recipeShareRepo;
    }

    // ------------------------
    // READ (admin kan lese alt)
    // ------------------------

    public List<RecipeDto> getAllRecipes() {
        if (isAdmin()) return recipeRepository.findAllAdmin();
        return recipeRepository.findAllForUser(currentUserId());
    }

    public Optional<RecipeDto> getRecipeById(String id) {
        if (isAdmin()) return recipeRepository.findByIdAdmin(id);
        return recipeRepository.findByIdForUser(currentUserId(), id);
    }

    // ------------------------------------
    // WRITE (admin er read-only, alltid 403)
    // ------------------------------------

    public RecipeDto saveRecipe(RecipeDto recipe) {
        ensureAdminReadOnly();

        LocalDateTime now = LocalDateTime.now();

        // createdAt settes kun første gang (for eksisterende rader kan den være null i starten)
        if (recipe.getCreatedAt() == null) {
            recipe.setCreatedAt(now);
        }

        // updatedAt settes alltid ved ekte "save/edit"
        recipe.setUpdatedAt(now);

        return recipeRepository.saveForUser(currentUserId(), recipe);
    }

    public boolean deleteRecipe(String id) {
        ensureAdminReadOnly();
        return recipeRepository.deleteByIdForUser(currentUserId(), id);
    }

    public void shareRecipe(String recipeId, String toUsername, String message) {
        ensureAdminReadOnly();

        final String fromUserId = currentUserId();

        // 1) finn original (må eies av avsender)
        final RecipeDto original = recipeRepository
                .findByIdForUser(fromUserId, recipeId)
                .orElseThrow(() -> new RuntimeException("Recipe not found"));

        // 2) finn mottaker
        final UserEntity receiver = userRepo.findByUsername(toUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (receiver.getId().equals(fromUserId)) {
            throw new RuntimeException("Cannot share recipe with yourself");
        }

        // 3) opprett share (PENDING)
        final RecipeShareEntity share = new RecipeShareEntity();
        share.setId(UUID.randomUUID().toString());
        share.setRecipeId(original.getId());
        share.setFromUserId(fromUserId);
        share.setToUserId(receiver.getId());
        share.setMessage(message == null || message.trim().isEmpty() ? null : message.trim());
        share.setStatus(RecipeShareEntity.Status.PENDING);
        share.setCreatedAt(LocalDateTime.now());
        share.setHandledAt(null);

        recipeShareRepo.save(share);
    }

    public RecipeDto markViewed(String recipeId) {
        ensureAdminReadOnly();

        final String userId = currentUserId();

        RecipeDto recipe = recipeRepository.findByIdForUser(userId, recipeId)
                .orElseThrow(() -> new RuntimeException("Recipe not found"));

        recipe.setLastViewedAt(LocalDateTime.now());
        Integer vc = recipe.getViewCount() != null ? recipe.getViewCount() : 0;
        recipe.setViewCount(vc + 1);

        return recipeRepository.saveForUser(userId, recipe);
    }

    public RecipeDto setFavorite(String recipeId, boolean favorite) {
        ensureAdminReadOnly();

        final String userId = currentUserId();

        RecipeDto recipe = recipeRepository.findByIdForUser(userId, recipeId)
                .orElseThrow(() -> new RuntimeException("Recipe not found"));

        recipe.setFavorite(favorite);
        recipe.setFavoritedAt(favorite ? LocalDateTime.now() : null);

        return recipeRepository.saveForUser(userId, recipe);
    }

    public RecipeDto setPinned(String recipeId, boolean pinned) {
        ensureAdminReadOnly();

        final String userId = currentUserId();

        RecipeDto recipe = recipeRepository.findByIdForUser(userId, recipeId)
                .orElseThrow(() -> new RuntimeException("Recipe not found"));

        recipe.setPinned(pinned);
        recipe.setPinnedAt(pinned ? LocalDateTime.now() : null);

        return recipeRepository.saveForUser(userId, recipe);
    }

    public RecipeDto setCoverImage(String recipeId, String coverImageId) {
        ensureAdminReadOnly();

        String normalized = (coverImageId != null && !coverImageId.trim().isEmpty())
                ? coverImageId.trim()
                : null;

        final String userId = currentUserId();

        RecipeDto recipe = recipeRepository.findByIdForUser(userId, recipeId)
                .orElseThrow(() -> new RuntimeException("Recipe not found"));

        recipe.setCoverImageId(normalized);
        return recipeRepository.saveForUser(userId, recipe);
    }

    // ------------------------
    // IMPORT (ingen lagring her)
    // ------------------------

    public RecipeDto importRecipeFromText(ImportRecipeRequestDto request) {

        RecipeDto aiRecipe = recipeParserService.parseToRecipe(
                request.getText(),
                request.getSourceUrl()
        );

        if (aiRecipe != null) {
            if (aiRecipe.getDescription() == null || aiRecipe.getDescription().isBlank()) {
                aiRecipe.setDescription(
                        "Beskrivelse ikke funnet i teksten. Rediger gjerne denne oppskriften."
                );
            }
            return aiRecipe;
        }

        System.err.println("RecipeService: AI parsing failed, using stub fallback");

        return RecipeDto.builder()
                .id(UUID.randomUUID().toString())
                .title("Imported: " +
                        request.getText().substring(0,
                                Math.min(50, request.getText().length())))
                .description(request.getText())
                .servings(4)
                .ingredients(new ArrayList<>())
                .steps(new ArrayList<>())
                .metadata(RecipeMetadataDto.builder()
                        .sourceUrl(request.getSourceUrl())
                        .importMethod("stub")
                        .categories(new ArrayList<>())
                        .build())
                .build();
    }

    // ------------------------
    // Helpers
    // ------------------------

    private void ensureAdminReadOnly() {
        if (isAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin is read-only");
        }
    }

    private String currentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new IllegalArgumentException("Not authenticated");
        }
        Object p = auth.getPrincipal();
        if (p instanceof QuantPrincipal qp) {
            return qp.userId();
        }
        throw new IllegalArgumentException("Not authenticated");
    }

    private boolean isAdmin() {
        return adminAccess.isAdminEmail(currentUserEmail());
    }

    private String currentUserEmail() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) return null;
        Object p = auth.getPrincipal();
        if (p instanceof QuantPrincipal qp) return qp.email();
        return null;
    }
}