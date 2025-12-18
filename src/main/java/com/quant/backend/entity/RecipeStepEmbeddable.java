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
public class RecipeStepEmbeddable {
    @Column(name = "step_number")
    private Integer step;
    
    @Column(name = "step_instruction", length = 2000)
    private String instruction;
    
    @Column(name = "step_notes", length = 1000)
    private String notes;
}

