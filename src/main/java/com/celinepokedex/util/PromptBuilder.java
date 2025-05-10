package com.celinepokedex.util;

import com.celinepokedex.model.BaseAnimal;
import com.celinepokedex.model.CharacterTrait;
import com.celinepokedex.model.DominantColor;
import com.celinepokedex.model.ElementType;
import com.celinepokedex.model.StyleType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PromptBuilder {
    // Fantasy creature mapping for each base animal type
    private static final Map<BaseAnimal, String> FANTASY_CREATURE_MAP = new HashMap<>();
    
    static {
        FANTASY_CREATURE_MAP.put(BaseAnimal.CAT, "Bakeneko (mystical cat)");
        FANTASY_CREATURE_MAP.put(BaseAnimal.LIZARD, "Dragon");
        FANTASY_CREATURE_MAP.put(BaseAnimal.BIRD, "Phoenix");
        FANTASY_CREATURE_MAP.put(BaseAnimal.FROG, "Frog Prince");
        FANTASY_CREATURE_MAP.put(BaseAnimal.FOX, "Kitsune (nine-tailed fox)");
        FANTASY_CREATURE_MAP.put(BaseAnimal.SNAKE, "Naga (half-snake, half-human)");
        FANTASY_CREATURE_MAP.put(BaseAnimal.HORSE, "Pegasus (winged horse)");
        FANTASY_CREATURE_MAP.put(BaseAnimal.TURTLE, "Koopa (Mario-like turtle)");
        FANTASY_CREATURE_MAP.put(BaseAnimal.LION, "Manticore (lion with scorpion tail)");
        FANTASY_CREATURE_MAP.put(BaseAnimal.EAGLE, "Griffin (eagle-lion hybrid)");
        FANTASY_CREATURE_MAP.put(BaseAnimal.DEER, "Celestial Deer (glowing antlers)");
    }
    
    public static String buildPrompt(BaseAnimal animal, ElementType element, StyleType style, List<CharacterTrait> traits) {
        return buildPrompt(animal, element, null, style, traits);
    }
    
    public static String buildPrompt(BaseAnimal animal, ElementType element, DominantColor dominantColor, StyleType style, List<CharacterTrait> traits) {
        StringBuilder prompt = new StringBuilder();
        
        // Get the fantasy creature transformation for the base animal
        String fantasyCreature = FANTASY_CREATURE_MAP.getOrDefault(animal, animal.name().toLowerCase());
        
        prompt.append("A magical fantasy character that is a ")
              .append(fantasyCreature)
              .append(", with ")
              .append(element.name().toLowerCase())
              .append(" powers");

        if (dominantColor != null) {
            prompt.append(", predominantly ")
                  .append(dominantColor.name().toLowerCase())
                  .append(" in color");
        }

        if (traits != null && !traits.isEmpty()) {
            prompt.append(", ");
            prompt.append(traits.stream()
                                .map(t -> t.name().toLowerCase().replace("_", " "))
                                .collect(Collectors.joining(", ")));
        }

        prompt.append(". ");
        prompt.append("This is a fantasy character designed in the style of ")
              .append(style.name().replace("_", " ").toLowerCase())
              .append(". White background, centered, full body, high detail, colorful, no shadow, no text, no watermark.");
        
        return prompt.toString();
    }
}