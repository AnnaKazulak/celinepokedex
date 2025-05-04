/**
 * Globale Konstanten und Konfigurationsvariablen für die Frontend-Anwendung
 */

// API Basis-URL
export const API_BASE_URL = 'http://localhost:8080/api';

// Pokémon API Endpunkte
export const API_ENDPOINTS = {
  POKEMONS: `${API_BASE_URL}/pokemons`,
  POKEMON_BY_ID: (id: number | string) => `${API_BASE_URL}/pokemons/${id}`,
  SEARCH_POKEMONS: (query: string) => `${API_BASE_URL}/pokemons/search?q=${query}`
};

// Externe Pokémon API (PokeAPI)
export const EXTERNAL_API = {
  BASE_URL: 'https://pokeapi.co/api/v2',
  POKEMON: (id: number | string) => `https://pokeapi.co/api/v2/pokemon/${id}`,
  POKEMON_SPECIES: (id: number | string) => `https://pokeapi.co/api/v2/pokemon-species/${id}`,
  SPRITE_URL: (id: number | string) => `https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/${id}.png`
};

// Anwendungskonfigurationen
export const APP_CONFIG = {
  DEFAULT_PAGE_SIZE: 12,
  MAX_DESCRIPTION_LENGTH: 150,
  DEFAULT_IMAGE_URL: 'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/25.png',
  DEFAULT_COLOR: '#b20072',
  MAX_POKEDEX_NUMBER: 898 // Maximale Pokédex-Nummer (Gen 8) - Für die Zufallsgenerierung
};

// Timeouts und Verzögerungen (in Millisekunden)
export const TIMEOUTS = {
  DEBOUNCE_SEARCH: 300,
  API_REQUEST_TIMEOUT: 10000,
  COLOR_EXTRACTION_DELAY: 100,
  AUTO_COMPLETE_DELAY: 800
};

// Bildgrößen für verschiedene Ansichten
export const IMAGE_SIZES = {
  CARD: {
    WIDTH: 150,
    HEIGHT: 150
  },
  DETAIL: {
    WIDTH: 320,
    HEIGHT: 320
  },
  THUMBNAIL: {
    WIDTH: 60,
    HEIGHT: 60
  }
};

// Validierungsregeln
export const VALIDATION_RULES = {
  REQUIRED: (v: any) => !!v || 'Dieses Feld ist erforderlich',
  NUMBER_ONLY: (v: string) => /^\d+$/.test(v) || 'Nur Zahlen erlaubt',
  MAX_LENGTH: (max: number) => (v: string) => (v && v.length <= max) || `Maximal ${max} Zeichen erlaubt`
};

// Pokémon-Farben
export const POKEMON_COLORS = {
  PIKACHU_YELLOW: '#F6C747',   // Pikachu-Gelb
  LEAF_GREEN: '#78C850',       // Blattgrün (Bulbasaur)
  FLAME_ORANGE: '#F08030',     // Flammenorange (Charmander)
  WATER_BLUE: '#6890F0',       // Wasserblau (Squirtle)
  PASTEL_PINK: '#F8B8D0',      // Pastellrosa (Jigglypuff)
  GHOST_PURPLE: '#705898',     // Geisterlila (Gengar)
  DEEP_SEA_GREEN: '#2E5F73',   // Tiefseegrün (Snorlax)
  PSYCHIC_VIOLET: '#A27DFA',   // Psycho-Violett (Mewtwo)
  STEEL_BLUE: '#3C5AA6',       // Stahlblau (Lucario)
  BLAZE_ORANGE: '#ED8A0E',     // Glutorange (Charizard)
};