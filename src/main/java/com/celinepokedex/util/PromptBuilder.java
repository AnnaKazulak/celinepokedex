package com.celinepokedex.util;

import com.celinepokedex.model.BaseAnimal;
import com.celinepokedex.model.CharacterTrait;
import com.celinepokedex.model.ElementType;
import com.celinepokedex.model.StyleType;

import java.util.List;
import java.util.stream.Collectors;

public class PromptBuilder {
    public static String buildPrompt(BaseAnimal animal, ElementType element, StyleType style, List<CharacterTrait> traits) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("A fantasy creature that looks like a ")
              .append(animal.name().toLowerCase())
              .append(", with ")
              .append(element.name().toLowerCase())
              .append(" powers");

        if (traits != null && !traits.isEmpty()) {
            prompt.append(", ");
            prompt.append(traits.stream()
                                .map(t -> t.name().toLowerCase().replace("_", " "))
                                .collect(Collectors.joining(", ")));
        }

        prompt.append(". ");
        prompt.append("Designed in the style of ")
              .append(style.name().replace("_", " ").toLowerCase())
              .append(". White background, centered, full body, high detail, colorful, no shadow, no text, no watermark.");
        
        return prompt.toString();
    }
}