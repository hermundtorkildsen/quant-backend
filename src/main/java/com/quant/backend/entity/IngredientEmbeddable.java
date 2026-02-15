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
public class IngredientEmbeddable {
    @Column(name = "ingredient_amount")
    private Double amount;
    
    @Column(name = "ingredient_unit")
    private String unit;
    
    @Column(name = "ingredient_item")
    private String item;
    
    @Column(name = "ingredient_notes")
    private String notes;

    @Column(name = "ingredient_section")
    private String section;
}

