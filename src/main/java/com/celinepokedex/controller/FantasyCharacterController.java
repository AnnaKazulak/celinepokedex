package com.celinepokedex.controller;

import com.celinepokedex.controller.request.FantasyCharacterGenerationRequest;
import com.celinepokedex.model.FantasyCharacter;
import com.celinepokedex.service.FantasyCharacterService;
import com.celinepokedex.service.ImageGenerationService;
import com.celinepokedex.util.PromptBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/characters")
public class FantasyCharacterController {

    private final FantasyCharacterService fantasyCharacterService;
    private final ImageGenerationService imageGenerationService;

    @Autowired
    public FantasyCharacterController(FantasyCharacterService fantasyCharacterService, 
                                      ImageGenerationService imageGenerationService) {
        this.fantasyCharacterService = fantasyCharacterService;
        this.imageGenerationService = imageGenerationService;
    }

    /**
     * Get all fantasy characters
     * GET /api/characters
     */
    @GetMapping
    public ResponseEntity<List<FantasyCharacter>> getAllCharacters() {
        List<FantasyCharacter> characters = fantasyCharacterService.getAllFantasyCharacters();
        return ResponseEntity.ok(characters);
    }

    /**
     * Get a fantasy character by ID
     * GET /api/characters/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<FantasyCharacter> getCharacterById(@PathVariable Long id) {
        Optional<FantasyCharacter> character = fantasyCharacterService.getFantasyCharacterById(id);
        return character.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Save a fantasy character
     * POST /api/characters/save
     */
    @PostMapping("/save")
    public ResponseEntity<FantasyCharacter> saveCharacter(@RequestBody Map<String, String> payload) {
        String prompt = payload.get("prompt");
        String imageUrl = payload.get("imageUrl");
        
        if (prompt == null || imageUrl == null) {
            return ResponseEntity.badRequest().build();
        }
        
        FantasyCharacter character = new FantasyCharacter(prompt, imageUrl);
        FantasyCharacter savedCharacter = fantasyCharacterService.saveFantasyCharacter(character);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCharacter);
    }

    /**
     * Search fantasy characters by prompt
     * GET /api/characters/search?query=...
     */
    @GetMapping("/search")
    public ResponseEntity<List<FantasyCharacter>> searchCharacters(@RequestParam String query) {
        List<FantasyCharacter> characters = fantasyCharacterService.searchFantasyCharacters(query);
        return ResponseEntity.ok(characters);
    }

    /**
     * Generate a fantasy character image using enum options instead of raw prompts
     * POST /api/characters/generate
     */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateCharacterFromAttributes(
            @RequestBody FantasyCharacterGenerationRequest request) {
        
        // Validate request
        if (request.getBaseAnimal() == null || 
            request.getElementType() == null || 
            request.getDominantColor() == null ||
            request.getStyleType() == null) {
            return ResponseEntity.badRequest().build();
        }
        
        // Generate the prompt based on selected attributes
        String prompt = PromptBuilder.buildPrompt(
            request.getBaseAnimal(),
            request.getElementType(),
            request.getDominantColor(),
            request.getStyleType(),
            request.getTraits()
        );
        
        // Generate the image
        String imageData = imageGenerationService.generateFantasyCharacterImage(
            request.getBaseAnimal(),
            request.getElementType(),
            request.getDominantColor(),
            request.getStyleType(),
            request.getTraits()
        );
        
        // Return both the generated prompt and image data
        Map<String, Object> response = Map.of(
            "prompt", prompt,
            "imageData", imageData
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Save a fantasy character that was generated using enum options
     * POST /api/characters/save-generated
     */
    @PostMapping("/save-generated")
    public ResponseEntity<FantasyCharacter> saveGeneratedCharacter(
            @RequestBody FantasyCharacterGenerationRequest request) {
        
        // Validate request
        if (request.getBaseAnimal() == null || 
            request.getElementType() == null || 
            request.getDominantColor() == null ||
            request.getStyleType() == null) {
            return ResponseEntity.badRequest().build();
        }
        
        // Generate image and prompt
        String prompt = PromptBuilder.buildPrompt(
            request.getBaseAnimal(),
            request.getElementType(),
            request.getDominantColor(),
            request.getStyleType(),
            request.getTraits()
        );
        
        String imageData = imageGenerationService.generateFantasyCharacterImage(
            request.getBaseAnimal(),
            request.getElementType(),
            request.getDominantColor(),
            request.getStyleType(),
            request.getTraits()
        );
        
        // Create and save character
        FantasyCharacter character = new FantasyCharacter(prompt, imageData);
        FantasyCharacter savedCharacter = fantasyCharacterService.saveFantasyCharacter(character);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCharacter);
    }

    /**
     * Delete a fantasy character by ID
     * DELETE /api/characters/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCharacter(@PathVariable Long id) {
        Optional<FantasyCharacter> character = fantasyCharacterService.getFantasyCharacterById(id);
        if (character.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        fantasyCharacterService.deleteFantasyCharacter(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Update a fantasy character by ID
     * PUT /api/characters/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<FantasyCharacter> updateCharacter(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        
        Optional<FantasyCharacter> existingCharacter = fantasyCharacterService.getFantasyCharacterById(id);
        if (existingCharacter.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        String prompt = payload.get("prompt");
        String imageUrl = payload.get("imageUrl");
        
        if (prompt == null || imageUrl == null) {
            return ResponseEntity.badRequest().build();
        }
        
        FantasyCharacter updatedCharacter = existingCharacter.get();
        updatedCharacter.setPrompt(prompt);
        updatedCharacter.setImageUrl(imageUrl);
        
        FantasyCharacter savedCharacter = fantasyCharacterService.saveFantasyCharacter(updatedCharacter);
        return ResponseEntity.ok(savedCharacter);
    }
}