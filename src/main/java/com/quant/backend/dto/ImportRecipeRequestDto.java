package com.quant.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportRecipeRequestDto {
    private String text;
    
    @JsonProperty("source_url")
    private String sourceUrl;
}

