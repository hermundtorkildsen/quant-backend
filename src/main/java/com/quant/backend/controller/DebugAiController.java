package com.quant.backend.controller;

import com.quant.backend.service.RecipeParserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/debug")
public class DebugAiController {

    private final RecipeParserService recipeParserService;

    public DebugAiController(RecipeParserService recipeParserService) {
        this.recipeParserService = recipeParserService;
    }

    @PostMapping("/parse-test")
    public String testParse(@RequestBody String text) {
        try {
            return recipeParserService.parseRaw(text);
        } catch (Exception e) {
            // For debug only: never throw
            return null;
        }
    }

}

