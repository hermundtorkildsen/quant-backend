package com.quant.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeMetadataDto {
    @JsonProperty("source_url")
    private String sourceUrl;
    
    private String author;
    
    private String language;
    
    @Builder.Default
    private List<String> categories = new ArrayList<>();
    
    @JsonProperty("image_url")
    private String imageUrl;
    
    @JsonProperty("calculator_id")
    private String calculatorId;
    
    @JsonProperty("import_method")
    private String importMethod;
}

