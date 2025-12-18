package com.quant.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeMetadataEmbeddable {
    @Column(name = "metadata_source_url")
    private String sourceUrl;
    
    @Column(name = "metadata_author")
    private String author;
    
    @Column(name = "metadata_language")
    private String language;
    
    @Column(name = "metadata_image_url")
    private String imageUrl;
    
    @Column(name = "metadata_calculator_id")
    private String calculatorId;
    
    @Column(name = "metadata_import_method")
    private String importMethod;
}

