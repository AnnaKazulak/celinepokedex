package com.celinepokedex.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Pokemon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private String imageUrl;

    private Double height; // Größe
    private Double weight; // Gewicht
    private String type1; // Haupttyp
    private String type2; // Sekundärtyp (optional)
    private String category; // Kategorie
    private String ability; // Fähigkeit
    private String pokedexNumber; // Pokedex-Nummer (als String)

    public Pokemon() {}

    public Pokemon(String name, String description, String imageUrl, Double height, Double weight, String type1, String type2, String category, String ability, String pokedexNumber) {
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.height = height;
        this.weight = weight;
        this.type1 = type1;
        this.type2 = type2;
        this.category = category;
        this.ability = ability;
        this.pokedexNumber = pokedexNumber;
    }

    // Getter und Setter
    public Long getId() {
        return id;
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

    public String getType1() {
        return type1;
    }

    public void setType1(String type1) {
        this.type1 = type1;
    }

    public String getType2() {
        return type2;
    }

    public void setType2(String type2) {
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

    public String getPokedexNumber() {
        return pokedexNumber;
    }

    public void setPokedexNumber(String pokedexNumber) {
        this.pokedexNumber = pokedexNumber;
    }
}
