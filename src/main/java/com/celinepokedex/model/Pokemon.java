package com.celinepokedex.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Column;
import jakarta.persistence.Transient;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Date;

@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class Pokemon {

    @Id
    @NotNull
    private String pokedexNumber; // Pokedex-Nummer als primärer Schlüssel

    private String name;
    
    @Column(length = 150)
    @Size(max = 150, message = "Die Beschreibung darf maximal 150 Zeichen lang sein")
    private String description;
    
    private String imageUrl;

    private Double height; // Größe
    private Double weight; // Gewicht
    
    @Enumerated(EnumType.STRING)
    private Type type1; // Haupttyp
    
    @Enumerated(EnumType.STRING)
    private Type type2; // Sekundärtyp (optional)
    
    private String category; // Kategorie
    private String ability; // Fähigkeit
    
    // Evolution fields
    private Integer evolutionChainId; // ID der Evolutionskette in der PokéAPI
    private String evolvesFromId; // Pokédex-Nummer des Pokemon, aus dem es sich entwickelt hat
    private String evolutionTrigger; // Wie es sich entwickelt (z.B. "level-up", "trade", "use-item")
    private String evolutionCondition; // Bedingungen für die Entwicklung (z.B. Level 16, mit einem Feuerstein)

    // Flag für die Anzeige in der Evolutionskettenansicht (wird nicht in DB gespeichert)
    @Transient
    private boolean inUserCollection = true;
    
    // Datum, wann das Pokemon hinzugefügt wurde
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    public Pokemon() {
        // Setze das Erstellungsdatum automatisch bei der Erstellung eines neuen Pokemons
        this.createdAt = new Date();
    }

    public Pokemon(String pokedexNumber, String name, String description, String imageUrl, Double height, Double weight, Type type1, Type type2, String category, String ability) {
        this.pokedexNumber = pokedexNumber;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.height = height;
        this.weight = weight;
        this.type1 = type1;
        this.type2 = type2;
        this.category = category;
        this.ability = ability;
        this.createdAt = new Date();
    }

    // Getter und Setter
    public String getPokedexNumber() {
        return pokedexNumber;
    }

    public void setPokedexNumber(String pokedexNumber) {
        this.pokedexNumber = pokedexNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Type getType1() {
        return type1;
    }

    public void setType1(Type type1) {
        this.type1 = type1;
    }

    public Type getType2() {
        return type2;
    }

    public void setType2(Type type2) {
        this.type2 = type2;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAbility() {
        return ability;
    }

    public void setAbility(String ability) {
        this.ability = ability;
    }

    // Evolution getters and setters
    public Integer getEvolutionChainId() {
        return evolutionChainId;
    }

    public void setEvolutionChainId(Integer evolutionChainId) {
        this.evolutionChainId = evolutionChainId;
    }

    public String getEvolvesFromId() {
        return evolvesFromId;
    }

    public void setEvolvesFromId(String evolvesFromId) {
        this.evolvesFromId = evolvesFromId;
    }

    public String getEvolutionTrigger() {
        return evolutionTrigger;
    }

    public void setEvolutionTrigger(String evolutionTrigger) {
        this.evolutionTrigger = evolutionTrigger;
    }

    public String getEvolutionCondition() {
        return evolutionCondition;
    }

    public void setEvolutionCondition(String evolutionCondition) {
        this.evolutionCondition = evolutionCondition;
    }

    public boolean isInUserCollection() {
        return inUserCollection;
    }

    public void setInUserCollection(boolean inUserCollection) {
        this.inUserCollection = inUserCollection;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
