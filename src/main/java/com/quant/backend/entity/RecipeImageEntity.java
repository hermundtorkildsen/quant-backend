package com.quant.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Stores images belonging to a recipe.
 *
 * Future-proof:
 * - multiple images per recipe
 * - cover image pointer stored on RecipeEntity (cover_image_id)
 * - supports user uploads and imported URLs
 *
 * NOTE: We store a storage_key rather than a public URL, so we can switch
 * storage providers and/or use signed URLs later.
 */
@Entity
@Table(name = "recipe_images")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeImageEntity {

    public enum SourceType {
        USER_UPLOAD,
        IMPORTED_URL,
        AI_GENERATED
    }

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "recipe_id", nullable = false)
    private String recipeId;

    @Column(name = "owner_user_id", nullable = false)
    private String ownerUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    private SourceType sourceType;

    @Column(name = "storage_key", nullable = false, length = 800)
    private String storageKey;

    @Column(name = "original_url", length = 2000)
    private String originalUrl;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}