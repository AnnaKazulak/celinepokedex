/**
 * Globale Konstanten und Konfigurationsvariablen für die Frontend-Anwendung
 */

// API Basis-URL
export const API_BASE_URL = 'http://localhost:8080/api';

// Pokémon API Endpunkte
export const API_ENDPOINTS = {
  POKEMONS: `${API_BASE_URL}/pokemons`,
  POKEMON_BY_ID: (id: number | string) => `${API_BASE_URL}/pokemons/${id}`,
  SEARCH_POKEMONS: (query: string) => `${API_BASE_URL}/pokemons/search?q=${query}`,
  // Fantasy Character Endpoints
  FANTASY_CHARACTERS: `${API_BASE_URL}/characters`,
  FANTASY_CHARACTER_BY_ID: (id: number) => `${API_BASE_URL}/characters/${id}`,
  DELETE_CHARACTER: (id: number) => `${API_BASE_URL}/characters/${id}`,
  GENERATE_IMAGE: `${API_BASE_URL}/images/generate`,
  GENERATE_CHARACTER: `${API_BASE_URL}/characters/generate`, // Added missing endpoint for generator
  SAVE_CHARACTER: `${API_BASE_URL}/characters/save`,
  UPLOAD_IMAGE: `${API_BASE_URL}/images/upload` // Added missing endpoint for image upload
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
  API_REQUEST_TIMEOUT: 60000, // Increased from 10000 to 60000 (60 seconds)
  IMAGE_GENERATION_TIMEOUT: 120000, // New: 120 seconds specifically for image generation
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
  PRIMARY_BLUE: '#6890F0',     // Primärblau
  PRIMARY_LILAC: '#705898',    // Primärlila
};

// Fantasy Character Options - Ausgelagert von FantasyCharacterDialog.vue
export const FANTASY_CHARACTER_OPTIONS = {
  // Base animals - EXACT same values as backend expects
  BASE_ANIMALS: [
    'CAT', 'LIZARD', 'BIRD', 
    'FROG', 'FOX', 'SNAKE', 'HORSE', 'TURTLE',
    'LION', 'EAGLE', 'DEER'
  ],
  
  // Element types - EXACT same values as backend expects
  ELEMENT_TYPES: [
    'FIRE', 'WATER', 'EARTH', 'WIND', 'ELECTRIC', 
    'ICE', 'NATURE', 'SHADOW', 'LIGHT', 'POISON'
  ],
  
  // Colors - EXACT same values as backend expects
  COLORS: [
    'RED', 'BLUE', 'GREEN', 'YELLOW', 'PURPLE', 
    'ORANGE', 'BLACK', 'WHITE', 'PINK', 'BROWN'
  ],
  
  // Style types - EXACT same values as backend expects
  STYLE_TYPES: [
    'DISNEY', 'PIXAR', 'POKEMON', 'STUDIO_GHIBLI', 'DREAMWORKS'
  ],
  
  // Character traits - EXACT same values as backend expects
  CHARACTER_TRAITS: [
    'CUTE', 'SCARY', 'MYSTERIOUS', 'MAJESTIC',
    'FUNNY', 'SMALL', 'GIANT', 'BABY', 'ELDER'
  ]
};

// Fantasy transformations map - Ausgelagert von FantasyCharacterDialog.vue
export const FANTASY_TRANSFORMATIONS: Record<string, string> = {
  'CAT': 'Bakeneko (mystical cat)',
  'LIZARD': 'Dragon',
  'BIRD': 'Phoenix',
  'FROG': 'Frog Prince',
  'FOX': 'Kitsune (nine-tailed fox)',
  'SNAKE': 'Naga (half-snake, half-human)',
  'HORSE': 'Pegasus (winged horse)',
  'TURTLE': 'Koopa (Mario-like turtle)',
  'LION': 'Manticore (lion with scorpion tail)',
  'EAGLE': 'Griffin (eagle-lion hybrid)',
  'DEER': 'Celestial Deer (glowing antlers)'
};

// Element Type zu Farb-Mapping für Fantasy-Elemente
export const ELEMENT_COLORS: Record<string, string> = {
  'FIRE': '#F08030',     // Flammenorange
  'WATER': '#6890F0',    // Wasserblau
  'EARTH': '#E0C068',    // Erdbraun
  'WIND': '#A8E0E0',     // Hellblau
  'ELECTRIC': '#F8D030', // Elektrischgelb
  'ICE': '#98D8D8',      // Eisblau
  'NATURE': '#78C850',   // Naturgrün
  'SHADOW': '#705898',   // Schattenlila
  'LIGHT': '#F8F878',    // Lichtgelb
  'POISON': '#A040A0'    // Giftlila
};