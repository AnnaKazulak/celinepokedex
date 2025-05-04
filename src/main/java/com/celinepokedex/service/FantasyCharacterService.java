package com.celinepokedex.service;

import com.celinepokedex.model.FantasyCharacter;
import com.celinepokedex.repository.FantasyCharacterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FantasyCharacterService {

    private final FantasyCharacterRepository fantasyCharacterRepository;

    @Autowired
    public FantasyCharacterService(FantasyCharacterRepository fantasyCharacterRepository) {
        this.fantasyCharacterRepository = fantasyCharacterRepository;
    }

    /**
     * Get all fantasy characters
     */
    public List<FantasyCharacter> getAllFantasyCharacters() {
        return fantasyCharacterRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Get a fantasy character by ID
     */
    public Optional<FantasyCharacter> getFantasyCharacterById(Long id) {
        return fantasyCharacterRepository.findById(id);
    }

    /**
     * Save a new fantasy character
     */
    public FantasyCharacter saveFantasyCharacter(FantasyCharacter fantasyCharacter) {
        return fantasyCharacterRepository.save(fantasyCharacter);
    }

    /**
     * Search fantasy characters by prompt
     */
    public List<FantasyCharacter> searchFantasyCharacters(String query) {
        return fantasyCharacterRepository.findByPromptContaining(query);
    }
}