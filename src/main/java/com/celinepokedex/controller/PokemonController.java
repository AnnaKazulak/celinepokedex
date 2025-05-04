package com.celinepokedex.controller;

import com.celinepokedex.exception.PokemonNotFoundException;
import com.celinepokedex.model.EvolutionChain;
import com.celinepokedex.model.Pokemon;
import com.celinepokedex.model.Type;
import com.celinepokedex.repository.PokemonRepository;
import com.celinepokedex.service.PokemonEvolutionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pokemons")
public class PokemonController {

    @Autowired
    private PokemonRepository pokemonRepository;
    
    @Autowired
    private PokemonEvolutionService evolutionService;

    // GET: Alle Pokémon abrufen, mit optionaler Typ-Filterung oder Evolution-Chain-Filterung
    @GetMapping
    public List<Pokemon> getAllPokemons(
            @RequestParam(required = false) List<Type> types,
            @RequestParam(required = false) Integer evolutionChainId) {
        
        List<Pokemon> pokemons;
        
        // Wenn eine evolutionChainId angegeben wurde, filtere nach Evolution-Chain-ID
        if (evolutionChainId != null) {
            return pokemonRepository.findByEvolutionChainId(evolutionChainId);
        }
        
        // Sonst hole alle Pokémon
        pokemons = pokemonRepository.findAll();
        
        // Wenn Typen angegeben wurden, filtere die Liste nach diesen Typen
        if (types != null && !types.isEmpty()) {
            pokemons = pokemons.stream()
                    .filter(pokemon -> 
                        // Ein Pokémon wird angezeigt, wenn es mindestens einen der gesuchten Typen hat
                        types.stream().anyMatch(type -> 
                            type.equals(pokemon.getType1()) || type.equals(pokemon.getType2())
                        )
                    )
                    .collect(Collectors.toList());
        }
        
        // Sortiere nach Pokédex-Nummer
        return pokemons.stream()
                .sorted((p1, p2) -> p1.getPokedexNumber().compareTo(p2.getPokedexNumber()))
                .toList();
    }
    
    // GET: Alle verfügbaren Pokémon-Typen abrufen
    @GetMapping("/types")
    public Type[] getAllTypes() {
        return Type.values();
    }
    
    // GET: Ein einzelnes Pokémon nach Pokédex-Nummer abrufen
    @GetMapping("/{pokedexNumber}")
    public Pokemon getPokemonByPokedexNumber(@PathVariable String pokedexNumber) {
        return pokemonRepository.findById(pokedexNumber)
                .orElseThrow(() -> new PokemonNotFoundException(pokedexNumber));
    }

    // GET: Pokémon nach Name, Pokédexnummer oder Typen suchen
    @GetMapping("/search")
    public List<Pokemon> searchPokemons(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String pokedexNumber,
            @RequestParam(required = false) List<Type> types) {
        
        // Wenn Typen angegeben wurden, hat das Priorität
        if (types != null && !types.isEmpty()) {
            return pokemonRepository.findAll().stream()
                    .filter(pokemon -> 
                        types.stream().anyMatch(type -> 
                            type.equals(pokemon.getType1()) || type.equals(pokemon.getType2())
                        )
                    )
                    .collect(Collectors.toList());
        }
        // Sonst normale Suche nach Name oder Pokédexnummer
        else if (name != null) {
            return pokemonRepository.findByNameContainingIgnoreCase(name);
        } else if (pokedexNumber != null) {
            return pokemonRepository.findById(pokedexNumber)
                    .map(List::of)
                    .orElse(List.of());
        } else {
            return List.of(); // Falls keine Suchparameter angegeben wurden
        }
    }
    
    // POST: Neues Pokémon erstellen
    @PostMapping
    public ResponseEntity<Pokemon> createPokemon(@Valid @RequestBody Pokemon pokemon) {
        if (pokemon.getDescription() != null && pokemon.getDescription().length() > 150) {
            pokemon.setDescription(pokemon.getDescription().substring(0, 150));
        }
        return ResponseEntity.ok(pokemonRepository.save(pokemon));
    }

    // PUT: Ein Pokémon aktualisieren
    @PutMapping("/{pokedexNumber}")
    public ResponseEntity<Pokemon> updatePokemon(@PathVariable String pokedexNumber, @Valid @RequestBody Pokemon updatedPokemon) {
        return pokemonRepository.findById(pokedexNumber).map(pokemon -> {
            pokemon.setName(updatedPokemon.getName());
            
            // Beschreibung auf 150 Zeichen begrenzen
            String description = updatedPokemon.getDescription();
            if (description != null && description.length() > 150) {
                description = description.substring(0, 150);
            }
            pokemon.setDescription(description);
            
            pokemon.setImageUrl(updatedPokemon.getImageUrl());
            pokemon.setHeight(updatedPokemon.getHeight());
            pokemon.setWeight(updatedPokemon.getWeight());
            pokemon.setType1(updatedPokemon.getType1());
            pokemon.setType2(updatedPokemon.getType2());
            pokemon.setCategory(updatedPokemon.getCategory());
            pokemon.setAbility(updatedPokemon.getAbility());
            pokemon.setPokedexNumber(updatedPokemon.getPokedexNumber());
            
            // Evolution fields
            pokemon.setEvolutionChainId(updatedPokemon.getEvolutionChainId());
            pokemon.setEvolvesFromId(updatedPokemon.getEvolvesFromId());
            pokemon.setEvolutionTrigger(updatedPokemon.getEvolutionTrigger());
            pokemon.setEvolutionCondition(updatedPokemon.getEvolutionCondition());
            
            return ResponseEntity.ok(pokemonRepository.save(pokemon));
        }).orElseThrow(() -> new PokemonNotFoundException(pokedexNumber));
    }

    // DELETE: Ein Pokémon löschen
    @DeleteMapping("/{pokedexNumber}")
    public void deletePokemon(@PathVariable String pokedexNumber) {
        if (!pokemonRepository.existsById(pokedexNumber)) {
            throw new PokemonNotFoundException(pokedexNumber);
        }
        pokemonRepository.deleteById(pokedexNumber);
    }
    
    // GET: Evolution chain for a Pokemon
    @GetMapping("/{pokedexNumber}/evolution-chain")
    public ResponseEntity<EvolutionChain> getEvolutionChain(@PathVariable String pokedexNumber) {
        Pokemon pokemon = pokemonRepository.findById(pokedexNumber)
                .orElseThrow(() -> new PokemonNotFoundException(pokedexNumber));
                
        return evolutionService.getEvolutionChainForPokemon(pokemon)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
