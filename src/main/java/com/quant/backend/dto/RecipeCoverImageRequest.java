package com.quant.backend.dto;

import lombok.Data;

@Data
public class RecipeCoverImageRequest {
    private String coverImageId; // nullable allowed to clear cover
}