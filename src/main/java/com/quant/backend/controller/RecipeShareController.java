package com.quant.backend.controller;

import com.quant.backend.auth.QuantPrincipal;
import com.quant.backend.auth.UserJpaRepository;
import com.quant.backend.dto.IngredientDto;
import com.quant.backend.dto.RecipeDto;
import com.quant.backend.dto.RecipeMetadataDto;
import com.quant.backend.dto.RecipeStepDto;
import com.quant.backend.entity.RecipeShareEntity;
import com.quant.backend.repository.RecipeRepository;
import com.quant.backend.repository.RecipeShareJpaRepository;
import com.quant.backend.service.RecipeShareService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/shares")
public class RecipeShareController {

    private final RecipeShareJpaRepository shareRepo;
    private final RecipeShareService shareService;

    public RecipeShareController(RecipeShareJpaRepository shareRepo, RecipeShareService shareService) {
        this.shareRepo = shareRepo;
        this.shareService = shareService;

    }

    public record InboxCountResponse(long count) {}

    @GetMapping("/inbox/count")
    public InboxCountResponse inboxCount() {
        String userId = currentUserId();
        return new InboxCountResponse(shareService.inboxCount(userId));
    }

    public record InboxShareItem(
            String id,
            String recipeId,
            String fromUsername,
            String message,
            java.time.LocalDateTime createdAt
    ) {}


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

    @GetMapping("/inbox")
    public java.util.List<InboxShareItem> inbox() {
        String userId = currentUserId();
        return shareService.inbox(userId);
    }

    @PostMapping("/{shareId}/accept")
    public RecipeDto accept(@PathVariable String shareId) {
        String userId = currentUserId();
        return shareService.accept(shareId, userId);
    }

    @PostMapping("/{shareId}/decline")
    public void decline(@PathVariable String shareId) {
        String userId = currentUserId();
        shareService.decline(shareId, userId);
    }
}
