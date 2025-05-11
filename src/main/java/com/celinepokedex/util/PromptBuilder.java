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

    /**
     * Generate a creative fantasy character description based on the base animal and element type
     * 
     * @param baseAnimal The base animal as a string
     * @param elementType The element type as a string
     * @param fantasyCreature The fantasy creature transformation (optional)
     * @return A creative description for the character
     */
    public static String generateFantasyDescription(String baseAnimal, String elementType, String fantasyCreature) {
        // If fantasyCreature is not provided, try to get it from the map
        if (fantasyCreature == null || fantasyCreature.isEmpty()) {
            try {
                BaseAnimal animal = BaseAnimal.valueOf(baseAnimal);
                fantasyCreature = FANTASY_CREATURE_MAP.getOrDefault(animal, baseAnimal.toLowerCase());
            } catch (IllegalArgumentException e) {
                fantasyCreature = baseAnimal.toLowerCase();
            }
        }
        
        // A collection of creative descriptions for different element types
        Map<String, String[]> elementDescriptions = new HashMap<>();
        elementDescriptions.put("FIRE", new String[] {
            "with flames dancing around its body",
            "radiating intense heat and passion",
            "with eyes that burn like embers"
        });
        elementDescriptions.put("WATER", new String[] {
            "with flowing aquatic movements",
            "that can manipulate water and create beautiful liquid sculptures",
            "with a calming presence like a still pond"
        });
        elementDescriptions.put("EARTH", new String[] {
            "with rocky armor protecting its body",
            "connected deeply to the natural world",
            "that can cause the ground to tremble with each step"
        });
        elementDescriptions.put("WIND", new String[] {
            "that moves with incredible speed and grace",
            "surrounded by swirling air currents",
            "with the ability to fly effortlessly through the sky"
        });
        elementDescriptions.put("ELECTRIC", new String[] {
            "crackling with electric energy",
            "that can summon lightning at will",
            "with sparks flying from its body when excited"
        });
        elementDescriptions.put("ICE", new String[] {
            "with a body as cold as winter frost",
            "that can freeze anything it touches",
            "leaving a trail of snowflakes wherever it goes"
        });
        elementDescriptions.put("NATURE", new String[] {
            "with flowers and plants growing from its body",
            "that brings life to barren landscapes",
            "deeply connected to the forests and wilds"
        });
        elementDescriptions.put("SHADOW", new String[] {
            "that can melt into darkness at will",
            "with mysterious shadows swirling around it",
            "that appears and disappears like a phantom"
        });
        elementDescriptions.put("LIGHT", new String[] {
            "radiating a warm, comforting glow",
            "that shines with inner brilliance",
            "that can create dazzling light displays"
        });
        elementDescriptions.put("POISON", new String[] {
            "with deadly but beautiful toxic patterns",
            "that can create potent elixirs both harmful and healing",
            "surrounded by a mesmerizing toxic mist"
        });
        
        // Select a random description for the given element type
        String[] descriptions = elementDescriptions.getOrDefault(elementType, new String[] {
            "with magical abilities",
            "with mysterious powers",
            "with extraordinary talents"
        });
        
        int randomIndex = (int) (Math.random() * descriptions.length);
        String elementDescription = descriptions[randomIndex];
        
        // Create personality traits
        String[] personalities = {
            "playful and curious", 
            "wise and ancient", 
            "brave and protective",
            "shy but powerful", 
            "mysterious and elusive", 
            "friendly and helpful",
            "mischievous but kind-hearted", 
            "noble and dignified", 
            "energetic and adventurous"
        };
        
        randomIndex = (int) (Math.random() * personalities.length);
        String personality = personalities[randomIndex];
        
        // Create habitat descriptions
        String[] habitats = {
            "found in enchanted forests",
            "dwelling in ancient ruins",
            "living high in the mountains",
            "making its home near magical lakes",
            "wandering through mystical plains",
            "hiding in shadowy caves",
            "soaring through the skies",
            "living in crystal caverns",
            "thriving in magical sanctuaries"
        };
        
        randomIndex = (int) (Math.random() * habitats.length);
        String habitat = habitats[randomIndex];
        
        // Build the final description
        return String.format("A magical %s %s. It is %s and usually %s. This creature is rare and highly valued for its unique abilities.",
            fantasyCreature.toLowerCase(),
            elementDescription,
            personality,
            habitat
        );
    }

    /**
     * Generate a creative fantasy character name based on the base animal and element type
     * 
     * @param baseAnimal The base animal as a string
     * @param elementType The element type as a string
     * @param fantasyCreature The fantasy creature transformation (optional)
     * @return A creative name for the character
     */
    public static String generateFantasyName(String baseAnimal, String elementType, String fantasyCreature) {
        // If fantasyCreature is not provided, try to get it from the map
        if (fantasyCreature == null || fantasyCreature.isEmpty()) {
            try {
                BaseAnimal animal = BaseAnimal.valueOf(baseAnimal);
                fantasyCreature = FANTASY_CREATURE_MAP.getOrDefault(animal, baseAnimal.toLowerCase());
            } catch (IllegalArgumentException e) {
                fantasyCreature = baseAnimal.toLowerCase();
            }
        }
        
        // Prefixes based on element type
        Map<String, String[]> elementPrefixes = new HashMap<>();
        elementPrefixes.put("FIRE", new String[] {
            "Blaze", "Ember", "Inferno", "Pyro", "Scorch", "Ash", "Flame", "Cinder", "Phoenix", "Burn"
        });
        elementPrefixes.put("WATER", new String[] {
            "Aqua", "Wave", "Splash", "Tide", "Torrent", "Ripple", "Hydro", "Dew", "Mist", "Deluge"
        });
        elementPrefixes.put("EARTH", new String[] {
            "Terra", "Stone", "Pebble", "Crystal", "Clay", "Boulder", "Quake", "Rock", "Geo", "Onyx"
        });
        elementPrefixes.put("WIND", new String[] {
            "Gale", "Breeze", "Whisper", "Zephyr", "Storm", "Tempest", "Gust", "Sky", "Whirl", "Swift"
        });
        elementPrefixes.put("ELECTRIC", new String[] {
            "Volt", "Spark", "Jolt", "Shock", "Thunder", "Lightning", "Arc", "Static", "Bolt", "Flash"
        });
        elementPrefixes.put("ICE", new String[] {
            "Frost", "Glacier", "Snow", "Crystal", "Chill", "Freeze", "Winter", "Arctic", "Sleet", "Blizzard"
        });
        elementPrefixes.put("NATURE", new String[] {
            "Leaf", "Bloom", "Flora", "Fern", "Sprout", "Root", "Thorn", "Ivy", "Sage", "Grove"
        });
        elementPrefixes.put("SHADOW", new String[] {
            "Shade", "Dusk", "Umbra", "Gloom", "Phantom", "Raven", "Mist", "Eclipse", "Void", "Wraith"
        });
        elementPrefixes.put("LIGHT", new String[] {
            "Sol", "Lux", "Ray", "Dawn", "Glow", "Shine", "Bright", "Gleam", "Beacon", "Radiance"
        });
        elementPrefixes.put("POISON", new String[] {
            "Venom", "Toxin", "Hemlock", "Fang", "Nightshade", "Viper", "Thorn", "Sting", "Blight", "Toxic"
        });
        
        // Suffixes based on animal/creature type
        Map<String, String[]> animalSuffixes = new HashMap<>();
        animalSuffixes.put("CAT", new String[] {
            "paw", "whisker", "claw", "fur", "tail", "fang", "purr", "shadow", "eye", "hunter"
        });
        animalSuffixes.put("LIZARD", new String[] {
            "scale", "spike", "claw", "tail", "fang", "drake", "wing", "reptile", "slither", "tongue"
        });
        animalSuffixes.put("BIRD", new String[] {
            "wing", "feather", "talon", "beak", "song", "soar", "glide", "nest", "flight", "sky"
        });
        animalSuffixes.put("FROG", new String[] {
            "hop", "croak", "pond", "tongue", "leap", "lily", "webfoot", "marsh", "spring", "ribbit"
        });
        animalSuffixes.put("FOX", new String[] {
            "tail", "trick", "swift", "cunning", "brush", "paw", "clever", "spirit", "whisker", "shadow"
        });
        animalSuffixes.put("SNAKE", new String[] {
            "coil", "slither", "fang", "viper", "scale", "hiss", "serpent", "strike", "venom", "twist"
        });
        animalSuffixes.put("HORSE", new String[] {
            "mane", "gallop", "hoof", "neigh", "steed", "stallion", "swift", "rider", "canter", "charger"
        });
        animalSuffixes.put("TURTLE", new String[] {
            "shell", "shield", "ancient", "slow", "wisdom", "tide", "armor", "sage", "patient", "steady"
        });
        animalSuffixes.put("LION", new String[] {
            "mane", "roar", "pride", "claw", "regal", "king", "fang", "golden", "savage", "hunter"
        });
        animalSuffixes.put("EAGLE", new String[] {
            "talon", "soar", "wing", "eye", "beak", "feather", "high", "noble", "swift", "hunter"
        });
        animalSuffixes.put("DEER", new String[] {
            "antler", "grace", "leap", "swift", "forest", "hoof", "hart", "silent", "gentle", "stag"
        });
        
        // Select random prefix and suffix
        String[] prefixes = elementPrefixes.getOrDefault(elementType, new String[] {
            "Magic", "Mystic", "Arcane", "Wonder", "Enchanted", "Charmed", "Fable", "Legend", "Myth", "Fantasy"
        });
        
        String[] suffixes = animalSuffixes.getOrDefault(baseAnimal, new String[] {
            "spirit", "heart", "soul", "essence", "being", "guardian", "keeper", "master", "lord", "friend"
        });
        
        int randomPrefixIndex = (int) (Math.random() * prefixes.length);
        int randomSuffixIndex = (int) (Math.random() * suffixes.length);
        String prefix = prefixes[randomPrefixIndex];
        String suffix = suffixes[randomSuffixIndex];
        
        // 50% chance to add a second name component
        boolean addSecondName = Math.random() > 0.5;
        
        if (addSecondName) {
            // Secondary name component options
            String[] secondComponents = {
                "the Brave", "the Wise", "the Swift", "the Mighty", "the Bold",
                "the Mystic", "the Enchanter", "the Protector", "the Magnificent",
                "the Mysterious", "the Ancient", "the Guardian", "the Wanderer",
                "the Dreamer", "the Seeker", "the Noble", "the Wild"
            };
            
            int randomComponentIndex = (int) (Math.random() * secondComponents.length);
            String secondComponent = secondComponents[randomComponentIndex];
            
            return prefix + suffix + " " + secondComponent;
        } else {
            return prefix + suffix;
        }
    }
}