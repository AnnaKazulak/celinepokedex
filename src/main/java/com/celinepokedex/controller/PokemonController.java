package com.celinepokedex.controller;

import com.celinepokedex.exception.PokemonNotFoundException;
import com.celinepokedex.model.Pokemon;
import com.celinepokedex.repository.PokemonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pokemons")
public class PokemonController {

    @Autowired
    private PokemonRepository pokemonRepository;

    // GET: Alle Pokémon abrufen, sortiert nach pokedexNumber
    @GetMapping
    public List<Pokemon> getAllPokemons() {
        return pokemonRepository.findAll().stream()
                .sorted((p1, p2) -> p1.getPokedexNumber().compareTo(p2.getPokedexNumber()))
                .toList();
    }

    // GET: Pokémon nach Name oder Pokédexnummer suchen
    @GetMapping("/search")
    public List<Pokemon> searchPokemons(@RequestParam(required = false) String name,
                                        @RequestParam(required = false) String pokedexNumber) {
        if (name != null) {
            return pokemonRepository.findByNameContainingIgnoreCase(name);
        } else if (pokedexNumber != null) {
            return pokemonRepository.findByPokedexNumber(pokedexNumber)
                    .map(List::of)
                    .orElse(List.of());
        } else {
            return List.of(); // Falls keine Suchparameter angegeben wurden
        }
    }

    // POST: Neues Pokémon erstellen
    @PostMapping
    public Pokemon createPokemon(@RequestBody Pokemon pokemon) {
        return pokemonRepository.save(pokemon);
    }

    // PUT: Ein Pokémon aktualisieren
    @PutMapping("/{id}")
    public Pokemon updatePokemon(@PathVariable Long id, @RequestBody Pokemon updatedPokemon) {
        return pokemonRepository.findById(id).map(pokemon -> {
            pokemon.setName(updatedPokemon.getName());
            pokemon.setDescription(updatedPokemon.getDescription());
            pokemon.setImageUrl(updatedPokemon.getImageUrl());
            pokemon.setHeight(updatedPokemon.getHeight());
            pokemon.setWeight(updatedPokemon.getWeight());
            pokemon.setType1(updatedPokemon.getType1());
            pokemon.setType2(updatedPokemon.getType2());
            pokemon.setCategory(updatedPokemon.getCategory());
            pokemon.setAbility(updatedPokemon.getAbility());
            pokemon.setPokedexNumber(updatedPokemon.getPokedexNumber());
            return pokemonRepository.save(pokemon);
        }).orElseThrow(() -> new PokemonNotFoundException(id));
    }

    // DELETE: Ein Pokémon löschen
    @DeleteMapping("/{id}")
    public void deletePokemon(@PathVariable Long id) {
        if (!pokemonRepository.existsById(id)) {
            throw new PokemonNotFoundException(id);
        }
        pokemonRepository.deleteById(id);
    }
}
