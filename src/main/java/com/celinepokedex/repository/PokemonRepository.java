package com.celinepokedex.repository;

import com.celinepokedex.model.Pokemon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PokemonRepository extends JpaRepository<Pokemon, String> {
    
    List<Pokemon> findByNameContainingIgnoreCase(String name);
    
    // Evolution related queries
    List<Pokemon> findByEvolutionChainId(Integer evolutionChainId);
    
    List<Pokemon> findByEvolvesFromId(String evolvesFromId);
}
