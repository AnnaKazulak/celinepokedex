package com.celinepokedex.model;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a Pokémon evolution chain structure with support for branched evolutions
 */
public class EvolutionChain {
    
    private Integer id;
    private List<EvolutionStage> stages;
    // Neue Map für Verzweigungsentwicklungen, Schlüssel ist pokedexNumber des Basis-Pokémon
    private Map<String, List<EvolutionStage>> branches;
    
    public EvolutionChain() {
        this.stages = new ArrayList<>();
        this.branches = new HashMap<>();
    }
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public List<EvolutionStage> getStages() {
        return stages;
    }
    
    public void setStages(List<EvolutionStage> stages) {
        this.stages = stages;
    }
    
    public void addStage(EvolutionStage stage) {
        if (this.stages == null) {
            this.stages = new ArrayList<>();
        }
        this.stages.add(stage);
    }
    
    // Neue Methoden für Verzweigungsentwicklungen
    public Map<String, List<EvolutionStage>> getBranches() {
        return branches;
    }
    
    public void setBranches(Map<String, List<EvolutionStage>> branches) {
        this.branches = branches;
    }
    
    /**
     * Fügt eine Entwicklung als Verzweigung hinzu
     * @param basePokedexNumber ID des Basis-Pokémon (von dem die Entwicklung ausgeht)
     * @param stage Die Entwicklungsstufe
     */
    public void addBranch(String basePokedexNumber, EvolutionStage stage) {
        if (this.branches == null) {
            this.branches = new HashMap<>();
        }
        
        if (!this.branches.containsKey(basePokedexNumber)) {
            this.branches.put(basePokedexNumber, new ArrayList<>());
        }
        
        this.branches.get(basePokedexNumber).add(stage);
    }
    
    /**
     * Überprüft, ob ein Pokémon Verzweigungsentwicklungen hat
     * @param pokedexNumber ID des zu prüfenden Pokémon
     * @return true wenn Verzweigungen existieren
     */
    public boolean hasBranches(String pokedexNumber) {
        return branches != null && branches.containsKey(pokedexNumber) && !branches.get(pokedexNumber).isEmpty();
    }
    
    /**
     * Inner class representing a stage in the evolution chain
     */
    public static class EvolutionStage {
        private Pokemon pokemon;
        private String trigger;
        private String condition;
        
        public EvolutionStage(Pokemon pokemon, String trigger, String condition) {
            this.pokemon = pokemon;
            this.trigger = trigger;
            this.condition = condition;
        }
        
        public Pokemon getPokemon() {
            return pokemon;
        }
        
        public void setPokemon(Pokemon pokemon) {
            this.pokemon = pokemon;
        }
        
        public String getTrigger() {
            return trigger;
        }
        
        public void setTrigger(String trigger) {
            this.trigger = trigger;
        }
        
        public String getCondition() {
            return condition;
        }
        
        public void setCondition(String condition) {
            this.condition = condition;
        }
    }
}