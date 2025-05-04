/**
 * TypeScript-Definitionen, die auf den Backend-Java-Modellen basieren
 */

/**
 * Entspricht dem Type-Enum im Backend
 */
export enum PokemonType {
  NORMAL = 'NORMAL',
  FEUER = 'FEUER',
  WASSER = 'WASSER',
  PFLANZE = 'PFLANZE',
  ELEKTRO = 'ELEKTRO',
  EIS = 'EIS',
  KAMPF = 'KAMPF',
  GIFT = 'GIFT',
  BODEN = 'BODEN',
  FLUG = 'FLUG',
  PSYCHO = 'PSYCHO',
  KÄFER = 'KÄFER',
  GESTEIN = 'GESTEIN',
  GEIST = 'GEIST',
  DRACHE = 'DRACHE',
  UNLICHT = 'UNLICHT',
  STAHL = 'STAHL',
  FEE = 'FEE'
}

/**
 * Interface für das Pokemon-Modell, entspricht der Pokemon-Klasse im Backend
 */
export interface Pokemon {
  pokedexNumber: string; // Jetzt Primary Key
  name: string;
  description?: string;
  imageUrl?: string;
  height?: number;
  weight?: number;
  type1?: PokemonType | null;
  type2?: PokemonType | null;
  category?: string;
  ability?: string;
  // Evolution fields
  evolutionChainId?: number;
  evolvesFromId?: string; // Jetzt String, da es auf die pokedexNumber verweist
  evolutionTrigger?: string;
  evolutionCondition?: string;
  // Flag für Anzeige in der Evolutionskette
  inUserCollection?: boolean;
  createdAt?: string; // Datum als ISO-String (2025-05-03T12:34:56.789Z)
}

/**
 * Interface für neue oder zu bearbeitende Pokemon-Daten
 */
export interface PokemonFormData {
  pokedexNumber: string; // Jetzt Primary Key
  name: string;
  description: string;
  imageUrl: string;
  height: number | null;
  weight: number | null;
  type1: PokemonType | null;
  type2: PokemonType | null;
  category: string;
  ability: string;
  evolutionChainId?: number | null;
  evolvesFromId?: string | null; // Jetzt String
  evolutionTrigger?: string;
  evolutionCondition?: string;
}

/**
 * Interface für die Evolutionskette
 */
export interface EvolutionChain {
  id: number;
  stages: EvolutionStage[];
  // Map für Verzweigungsentwicklungen, Schlüssel ist pokedexNumber des Basis-Pokémon
  branches?: Record<string, EvolutionStage[]>;
}

/**
 * Interface für eine Stufe in der Evolutionskette
 */
export interface EvolutionStage {
  pokemon: Pokemon;
  trigger?: string;
  condition?: string;
}