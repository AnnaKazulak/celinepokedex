package com.celinepokedex.controller;

import com.celinepokedex.model.FantasyCharacter;
import com.celinepokedex.service.FantasyCharacterService;
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

    @Autowired
    public FantasyCharacterController(FantasyCharacterService fantasyCharacterService) {
        this.fantasyCharacterService = fantasyCharacterService;
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
}