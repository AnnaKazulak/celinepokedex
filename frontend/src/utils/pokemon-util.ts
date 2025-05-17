/**
 * Hilfsfunktionen speziell für Pokémon-Operationen
 */
import type { PokemonGenRange } from "./constants";

/**
 * Generiert eine zufällige Pokémon-ID basierend auf verfügbaren Generationsbereichen
 * 
 * @param maxId Die maximale verfügbare Pokémon-ID
 * @param ranges Optionale Array mit definierten Generationsbereichen
 * @returns Eine zufällige, gültige Pokémon-ID als String
 */
export function generateRandomPokemonId(maxId: number, ranges?: PokemonGenRange[]): string {
  let randomNum: number;
  
  // Nutze definierte Bereiche wenn verfügbar
  if (ranges && ranges.length > 0) {
    // Wähle eine zufällige Generation aus
    const randomGenIndex = Math.floor(Math.random() * ranges.length);
    const selectedRange = ranges[randomGenIndex];
    
    // Generiere eine Zufallszahl innerhalb des ausgewählten Bereichs
    randomNum = Math.floor(Math.random() * (selectedRange.end - selectedRange.start + 1)) + selectedRange.start;
  } else {
    // Fallback: Generiere eine Zufallszahl zwischen 1 und maxId
    randomNum = Math.floor(Math.random() * maxId) + 1;
  }
  
  // Formatiere die Nummer mit führenden Nullen basierend auf ihrer Größe
  return formatPokemonId(randomNum);
}

/**
 * Formatiert eine Pokémon-ID mit der richtigen Anzahl an führenden Nullen
 * 
 * @param id Die zu formatierende Pokémon-ID
 * @returns Formatierte ID als String
 */
export function formatPokemonId(id: number): string {
  // IDs unter 1000 haben traditionell ein 3-stelliges Format mit führenden Nullen
  // IDs ab 1000 werden mit 4 Stellen ohne führende Nullen dargestellt
  return id < 1000 ? String(id).padStart(3, '0') : String(id);
}
