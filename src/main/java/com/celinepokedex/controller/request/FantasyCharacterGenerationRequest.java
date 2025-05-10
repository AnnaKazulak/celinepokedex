package com.celinepokedex.controller.request;

import com.celinepokedex.model.BaseAnimal;
import com.celinepokedex.model.CharacterTrait;
import com.celinepokedex.model.DominantColor;
import com.celinepokedex.model.ElementType;
import com.celinepokedex.model.StyleType;

import java.util.List;

public class FantasyCharacterGenerationRequest {
    private BaseAnimal baseAnimal;
    private ElementType elementType;
    private DominantColor dominantColor;
    private StyleType styleType;
    private List<CharacterTrait> traits;

    // Getters and setters
    public BaseAnimal getBaseAnimal() {
        return baseAnimal;
    }

    public void setBaseAnimal(BaseAnimal baseAnimal) {
        this.baseAnimal = baseAnimal;
    }

    public ElementType getElementType() {
        return elementType;
    }

    public void setElementType(ElementType elementType) {
        this.elementType = elementType;
    }

    public DominantColor getDominantColor() {
        return dominantColor;
    }

    public void setDominantColor(DominantColor dominantColor) {
        this.dominantColor = dominantColor;
    }

    public StyleType getStyleType() {
        return styleType;
    }

    public void setStyleType(StyleType styleType) {
        this.styleType = styleType;
    }

    public List<CharacterTrait> getTraits() {
        return traits;
    }

    public void setTraits(List<CharacterTrait> traits) {
        this.traits = traits;
    }
}