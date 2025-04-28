package com.celinepokedex.exception;

public class PokemonNotFoundException extends RuntimeException {
    private final Long id;

    public PokemonNotFoundException(Long id) {
        super("Pokemon not found with id " + id);
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
