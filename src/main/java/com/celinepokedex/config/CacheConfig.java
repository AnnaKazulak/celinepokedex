package com.celinepokedex.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String POKEAPI_POKEMON_CACHE = "pokeapiPokemonCache";
    public static final String POKEAPI_SPECIES_CACHE = "pokeapiSpeciesCache";
    public static final String POKEAPI_EVOLUTION_CHAIN_CACHE = "pokeapiEvolutionChainCache";

    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        cacheManager.setCacheNames(Arrays.asList(
            POKEAPI_POKEMON_CACHE, 
            POKEAPI_SPECIES_CACHE,
            POKEAPI_EVOLUTION_CHAIN_CACHE
        ));
        return cacheManager;
    }
}