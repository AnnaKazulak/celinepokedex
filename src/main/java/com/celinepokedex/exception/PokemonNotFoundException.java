package com.celinepokedex.exception;

public class PokemonNotFoundException extends RuntimeException {
    private final String pokedexNumber;

    public PokemonNotFoundException(String pokedexNumber) {
        super("Pokemon not found with Pokédex number " + pokedexNumber);
        this.pokedexNumber = pokedexNumber;
    }

    public String getPokedexNumber() {
        return pokedexNumber;
    }
}
