package com.celinepokedex.service;

import com.celinepokedex.config.CacheConfig;
import com.celinepokedex.model.EvolutionChain;
import com.celinepokedex.model.Pokemon;
import com.celinepokedex.repository.PokemonRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PokemonEvolutionService {

    private static final String POKEAPI_EVOLUTION_URL = "https://pokeapi.co/api/v2/evolution-chain/";
    private static final String POKEAPI_POKEMON_URL = "https://pokeapi.co/api/v2/pokemon/";
    private static final String POKEAPI_SPECIES_URL = "https://pokeapi.co/api/v2/pokemon-species/";
    
    @Autowired
    private PokemonRepository pokemonRepository;
    
    @Autowired
    private RestTemplate restTemplate;
    
    /**
     * Fetches evolution chain data for a specific Pokemon
     */
    public Optional<EvolutionChain> getEvolutionChainForPokemon(Pokemon pokemon) {
        if (pokemon.getEvolutionChainId() == null) {
            return Optional.empty();
        }
        
        EvolutionChain evolutionChain = new EvolutionChain();
        evolutionChain.setId(pokemon.getEvolutionChainId());
        
        // Try to build the chain from our local database
        List<Pokemon> evolutions = pokemonRepository.findByEvolutionChainId(pokemon.getEvolutionChainId());
        
        if (!evolutions.isEmpty()) {
            // Wir haben lokale Daten - baue die Kette daraus auf
            Map<String, Boolean> userCollectionMap = new HashMap<>();
            
            // Erstelle eine Map von Pokédex-Nummern, die in der Benutzerdatenbank vorhanden sind
            for (Pokemon evo : evolutions) {
                userCollectionMap.put(evo.getPokedexNumber(), true);
            }
            
            // Versuche, zusätzliche Pokémon aus der API zu holen, falls die Kette unvollständig ist
            try {
                Optional<EvolutionChain> apiChain = fetchEvolutionChainFromApi(pokemon.getEvolutionChainId());
                if (apiChain.isPresent() && apiChain.get().getStages() != null) {
                    // Füge PokeAPI-Daten mit inUserCollection=false hinzu, wenn sie nicht in unserer DB sind
                    for (EvolutionChain.EvolutionStage stage : apiChain.get().getStages()) {
                        String pokedexNum = stage.getPokemon().getPokedexNumber();
                        if (!userCollectionMap.containsKey(pokedexNum)) {
                            // Dieses Pokémon ist nicht in der Benutzerdatenbank
                            stage.getPokemon().setInUserCollection(false);
                            evolutionChain.addStage(stage);
                        }
                    }
                    
                    // Verzweigungen aus der API hinzufügen
                    if (apiChain.get().getBranches() != null && !apiChain.get().getBranches().isEmpty()) {
                        for (Map.Entry<String, List<EvolutionChain.EvolutionStage>> entry : 
                             apiChain.get().getBranches().entrySet()) {
                            String basePokedexNum = entry.getKey();
                            for (EvolutionChain.EvolutionStage branchStage : entry.getValue()) {
                                String branchPokedexNum = branchStage.getPokemon().getPokedexNumber();
                                if (!userCollectionMap.containsKey(branchPokedexNum)) {
                                    branchStage.getPokemon().setInUserCollection(false);
                                    evolutionChain.addBranch(basePokedexNum, branchStage);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // API-Aufruf fehlgeschlagen - ignorieren und nur mit lokalen Daten fortfahren
                System.err.println("Fehler beim Abrufen der API-Daten: " + e.getMessage());
            }
            
            // Füge lokale Pokémon mit inUserCollection=true hinzu
            for (Pokemon evo : evolutions) {
                String trigger = evo.getEvolutionTrigger();
                String condition = evo.getEvolutionCondition();
                evo.setInUserCollection(true); // Dieses Pokémon ist definitiv in der Sammlung
                
                // Prüfen, ob es eine Standardentwicklung oder eine Verzweigungsentwicklung ist
                if (evo.getEvolvesFromId() != null) {
                    // Prüfen ob mehrere Pokémon vom selben Pokémon evolvieren (Verzweigung)
                    List<Pokemon> siblingEvolutions = pokemonRepository.findByEvolvesFromId(evo.getEvolvesFromId());
                    if (siblingEvolutions.size() > 1) {
                        // Es ist eine Verzweigungsentwicklung
                        evolutionChain.addBranch(evo.getEvolvesFromId(), 
                                new EvolutionChain.EvolutionStage(evo, trigger, condition));
                    } else {
                        // Es ist eine normale Entwicklung
                        evolutionChain.addStage(
                                new EvolutionChain.EvolutionStage(evo, trigger, condition));
                    }
                } else {
                    // Basis-Pokémon ohne evolvesFromId
                    evolutionChain.addStage(
                            new EvolutionChain.EvolutionStage(evo, trigger, condition));
                }
            }
            
            return Optional.of(evolutionChain);
        } else {
            // Keine lokalen Daten - versuche, von der PokéAPI zu holen
            try {
                Optional<EvolutionChain> apiChain = fetchEvolutionChainFromApi(pokemon.getEvolutionChainId());
                if (apiChain.isPresent()) {
                    // Markiere alle Pokémon aus der API als nicht in der Benutzersammlung
                    for (EvolutionChain.EvolutionStage stage : apiChain.get().getStages()) {
                        stage.getPokemon().setInUserCollection(false);
                    }
                    
                    // Markiere alle Verzweigungsentwicklungen aus der API als nicht in der Benutzersammlung
                    if (apiChain.get().getBranches() != null) {
                        for (List<EvolutionChain.EvolutionStage> branchStages : apiChain.get().getBranches().values()) {
                            for (EvolutionChain.EvolutionStage branchStage : branchStages) {
                                branchStage.getPokemon().setInUserCollection(false);
                            }
                        }
                    }
                }
                return apiChain;
            } catch (Exception e) {
                return Optional.empty();
            }
        }
    }
    
    /**
     * Fetches an evolution chain from the PokéAPI
     */
    @Cacheable(value = CacheConfig.POKEAPI_EVOLUTION_CHAIN_CACHE, key = "#chainId")
    private Optional<EvolutionChain> fetchEvolutionChainFromApi(Integer chainId) {
        try {
            String url = POKEAPI_EVOLUTION_URL + chainId;
            String jsonResponse = restTemplate.getForObject(url, String.class);
            
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(jsonResponse);
            
            EvolutionChain chain = new EvolutionChain();
            chain.setId(chainId);
            
            // Parse the chain starting from the base form
            JsonNode chainNode = rootNode.path("chain");
            processEvolutionChain(chainNode, chain, null, null);
            
            return Optional.of(chain);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    /**
     * Recursively processes the evolution chain JSON from PokéAPI
     */
    private void processEvolutionChain(JsonNode chainNode, EvolutionChain chain, String trigger, String condition) {
        // Extract the species data
        JsonNode speciesNode = chainNode.path("species");
        String speciesUrl = speciesNode.path("url").asText();
        String pokedexNumber = extractPokedexNumber(speciesUrl);
        
        // Try to find this Pokemon in our database
        Optional<Pokemon> pokemonOpt = pokemonRepository.findById(pokedexNumber);
        
        Pokemon pokemon;
        boolean inUserCollection = false;
        
        if (pokemonOpt.isPresent()) {
            // Benutzer hat dieses Pokémon bereits in seiner Sammlung
            pokemon = pokemonOpt.get();
            inUserCollection = true;
            
            // Update evolution data in our database if needed
            pokemon.setEvolutionChainId(chain.getId());
            pokemonRepository.save(pokemon);
        } else {
            // Benutzer hat dieses Pokémon noch nicht - erstelle eine temporäre Version aus der API
            pokemon = createTemporaryPokemonFromApi(pokedexNumber);
            inUserCollection = false;
        }
        
        // Setze explizit das inUserCollection-Flag
        pokemon.setInUserCollection(inUserCollection);
        
        // Füge das Pokémon zur Evolutionskette hinzu
        chain.addStage(new EvolutionChain.EvolutionStage(pokemon, trigger, condition));
        
        // Process evolution details for the next stage
        JsonNode evolvesToNodes = chainNode.path("evolves_to");
        
        // Prüfe auf Verzweigungsentwicklungen (mehrere evolves_to Einträge)
        if (evolvesToNodes.size() > 1) {
            // Dies ist ein Pokémon mit Verzweigungsentwicklungen (wie Evoli)
            for (JsonNode evolvesTo : evolvesToNodes) {
                String nextTrigger = null;
                String nextCondition = null;
                
                // Extract evolution trigger and condition
                JsonNode evolutionDetailsArray = evolvesTo.path("evolution_details");
                if (evolutionDetailsArray.isArray() && evolutionDetailsArray.size() > 0) {
                    JsonNode details = evolutionDetailsArray.get(0);
                    nextTrigger = details.path("trigger").path("name").asText();
                    
                    // Find the most specific evolution condition
                    if (details.has("min_level") && !details.path("min_level").isNull()) {
                        nextCondition = "Level " + details.path("min_level").asInt();
                    } else if (details.has("item") && !details.path("item").isNull()) {
                        nextCondition = "Use " + details.path("item").path("name").asText();
                    } else if (details.has("held_item") && !details.path("held_item").isNull()) {
                        nextCondition = "Hold " + details.path("held_item").path("name").asText();
                    } else if (details.has("min_happiness") && !details.path("min_happiness").isNull()) {
                        nextCondition = "Happiness " + details.path("min_happiness").asInt();
                    } else if (details.has("time_of_day") && !details.path("time_of_day").asText().isEmpty()) {
                        nextCondition = "During " + details.path("time_of_day").asText();
                    } else {
                        nextCondition = nextTrigger;
                    }
                }
                
                // Extrahiere das nächste Pokémon in dieser Verzweigung
                JsonNode branchSpeciesNode = evolvesTo.path("species");
                String branchSpeciesUrl = branchSpeciesNode.path("url").asText();
                String branchPokedexNumber = extractPokedexNumber(branchSpeciesUrl);
                
                // Versuche, dieses Pokémon in unserer Datenbank zu finden
                Optional<Pokemon> branchPokemonOpt = pokemonRepository.findById(branchPokedexNumber);
                
                Pokemon branchPokemon;
                boolean branchInUserCollection = false;
                
                if (branchPokemonOpt.isPresent()) {
                    branchPokemon = branchPokemonOpt.get();
                    branchInUserCollection = true;
                    
                    // Update evolution data in our database
                    branchPokemon.setEvolutionChainId(chain.getId());
                    branchPokemon.setEvolvesFromId(pokedexNumber);  // Set the evolution source
                    pokemonRepository.save(branchPokemon);
                } else {
                    branchPokemon = createTemporaryPokemonFromApi(branchPokedexNumber);
                    branchInUserCollection = false;
                }
                
                branchPokemon.setInUserCollection(branchInUserCollection);
                
                // Füge dieses Pokémon als Verzweigungsentwicklung hinzu
                chain.addBranch(pokedexNumber, 
                        new EvolutionChain.EvolutionStage(branchPokemon, nextTrigger, nextCondition));
                
                // Rekursiv weiterverarbeiten für weitere Entwicklungen dieser Verzweigung
                if (evolvesTo.path("evolves_to").size() > 0) {
                    processEvolutionChain(evolvesTo, chain, nextTrigger, nextCondition);
                }
            }
        } else {
            // Normale Evolutionskette ohne Verzweigung
            for (JsonNode evolvesTo : evolvesToNodes) {
                String nextTrigger = null;
                String nextCondition = null;
                
                JsonNode evolutionDetailsArray = evolvesTo.path("evolution_details");
                if (evolutionDetailsArray.isArray() && evolutionDetailsArray.size() > 0) {
                    JsonNode details = evolutionDetailsArray.get(0);
                    nextTrigger = details.path("trigger").path("name").asText();
                    
                    // Find the most specific evolution condition
                    if (details.has("min_level") && !details.path("min_level").isNull()) {
                        nextCondition = "Level " + details.path("min_level").asInt();
                    } else if (details.has("item") && !details.path("item").isNull()) {
                        nextCondition = "Use " + details.path("item").path("name").asText();
                    } else if (details.has("held_item") && !details.path("held_item").isNull()) {
                        nextCondition = "Hold " + details.path("held_item").path("name").asText();
                    } else if (details.has("min_happiness") && !details.path("min_happiness").isNull()) {
                        nextCondition = "Happiness " + details.path("min_happiness").asInt();
                    } else if (details.has("time_of_day") && !details.path("time_of_day").asText().isEmpty()) {
                        nextCondition = "During " + details.path("time_of_day").asText();
                    } else {
                        nextCondition = nextTrigger;
                    }
                }
                
                // Get the Pokédex number of current species for evolvesFrom reference
                String currentPokedexNumber = extractPokedexNumber(speciesUrl);
                
                // Recursive call for next evolution
                processEvolutionChain(evolvesTo, chain, nextTrigger, nextCondition);
                
                // Update evolvesFromId in the database for the next evolution
                JsonNode nextSpeciesNode = evolvesTo.path("species");
                String nextSpeciesUrl = nextSpeciesNode.path("url").asText();
                String nextPokedexNumber = extractPokedexNumber(nextSpeciesUrl);
                
                pokemonRepository.findById(nextPokedexNumber).ifPresent(p -> {
                    p.setEvolvesFromId(currentPokedexNumber);
                    pokemonRepository.save(p);
                });
            }
        }
    }
    
    /**
     * Erstellt ein temporäres Pokémon-Objekt aus der API für die Evolutionskette
     */
    private Pokemon createTemporaryPokemonFromApi(String pokedexNumber) {
        try {
            // Versuche zuerst mit dem species-Endpunkt, um den Namen zu erhalten
            JsonNode speciesNode = fetchPokemonSpeciesData(pokedexNumber);
            
            // Extrahiere die wichtigsten Informationen
            String name = speciesNode.path("name").asText();
            name = name.substring(0, 1).toUpperCase() + name.substring(1); // Ersten Buchstaben groß schreiben
            
            // Prüfe auf deutschen Namen
            JsonNode namesArray = speciesNode.path("names");
            if (namesArray.isArray()) {
                for (JsonNode nameNode : namesArray) {
                    if (nameNode.path("language").path("name").asText().equals("de")) {
                        name = nameNode.path("name").asText();
                        break;
                    }
                }
            }
            
            // Bild-URL aus dem regulären Pokémon-Endpunkt holen
            JsonNode pokemonNode = fetchPokemonData(pokedexNumber);
            
            // Offizielle Artwork-URL extrahieren
            String imageUrl = pokemonNode.path("sprites")
                                       .path("other")
                                       .path("official-artwork")
                                       .path("front_default").asText();
            
            // Fallback auf Standard-Sprite, wenn kein Artwork vorhanden ist
            if (imageUrl == null || imageUrl.isEmpty()) {
                imageUrl = pokemonNode.path("sprites").path("front_default").asText();
            }
            
            // Pokemon-Objekt erstellen (nur mit grundlegenden Daten)
            Pokemon pokemon = new Pokemon();
            pokemon.setPokedexNumber(String.format("%03d", Integer.parseInt(pokedexNumber)));
            pokemon.setName(name);
            pokemon.setImageUrl(imageUrl);
            
            return pokemon;
        } catch (Exception e) {
            // Wenn API-Aufruf fehlschlägt, erstelle ein Dummy-Pokémon
            Pokemon pokemon = new Pokemon();
            pokemon.setPokedexNumber(String.format("%03d", Integer.parseInt(pokedexNumber)));
            pokemon.setName("Pokemon #" + pokedexNumber);
            pokemon.setImageUrl("/images/placeholder.png"); // Platzhalter-Bild
            return pokemon;
        }
    }
    
    /**
     * Ruft Pokemon-Daten von der PokeAPI ab mit Caching
     */
    @Cacheable(value = CacheConfig.POKEAPI_POKEMON_CACHE, key = "#pokedexNumber")
    public JsonNode fetchPokemonData(String pokedexNumber) throws Exception {
        String pokemonUrl = POKEAPI_POKEMON_URL + pokedexNumber;
        String pokemonResponse = restTemplate.getForObject(pokemonUrl, String.class);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(pokemonResponse);
    }
    
    /**
     * Ruft Pokemon-Species-Daten von der PokeAPI ab mit Caching
     */
    @Cacheable(value = CacheConfig.POKEAPI_SPECIES_CACHE, key = "#pokedexNumber")
    public JsonNode fetchPokemonSpeciesData(String pokedexNumber) throws Exception {
        String speciesUrl = POKEAPI_SPECIES_URL + pokedexNumber;
        String speciesResponse = restTemplate.getForObject(speciesUrl, String.class);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(speciesResponse);
    }
    
    /**
     * Extracts the Pokedex number from a PokéAPI species URL
     */
    private String extractPokedexNumber(String url) {
        // URL format: https://pokeapi.co/api/v2/pokemon-species/25/
        String[] parts = url.split("/");
        if (parts.length >= 2) {
            String number = parts[parts.length - 1];
            if (number.isEmpty()) {
                number = parts[parts.length - 2];
            }
            try {
                int num = Integer.parseInt(number);
                return String.format("%03d", num); // Format as 3-digit string (e.g., "025")
            } catch (NumberFormatException e) {
                return number;
            }
        }
        return "";
    }
}