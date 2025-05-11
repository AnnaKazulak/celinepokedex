/**
 * Hilfsfunktionen für die Anwendung
 */

/**
 * Erzeugt eine debounced Version einer Funktion, die erst nach einer bestimmten Verzögerung ausgeführt wird
 * @param func Die zu debouncende Funktion
 * @param wait Verzögerungszeit in Millisekunden
 * @returns Eine neue Funktion, die debounced ist
 */
export function debounce<T extends (...args: any[]) => any>(
  func: T,
  wait: number
): (...args: Parameters<T>) => void {
  let timeout: ReturnType<typeof setTimeout> | null = null;
  
  return function(...args: Parameters<T>): void {
    const later = () => {
      timeout = null;
      func(...args);
    };
    
    if (timeout !== null) {
      clearTimeout(timeout);
    }
    
    timeout = setTimeout(later, wait);
  };
}

/**
 * Formatiert einen Text so, dass der erste Buchstabe groß geschrieben wird
 * @param text Der zu formatierende Text
 * @returns Der formatierte Text
 */
export function capitalizeFirstLetter(text: string): string {
  if (!text || typeof text !== 'string') return '';
  return text.charAt(0).toUpperCase() + text.slice(1);
}

/**
 * Prüft, ob ein Wert null, undefined oder ein leerer String ist
 * @param value Der zu prüfende Wert
 * @returns true wenn der Wert null, undefined oder ein leerer String ist
 */
export function isEmpty(value: any): boolean {
  return value === null || value === undefined || value === '';
}

/**
 * Entfernt unnötige Whitespaces und Sonderzeichen aus einem Text
 * @param text Der zu bereinigende Text
 * @returns Der bereinigte Text
 */
export function cleanText(text: string): string {
  if (!text || typeof text !== 'string') return '';
  return text
    .replace(/\n/g, ' ')
    .replace(/\f/g, ' ')
    .replace(/\u000c/g, ' ')
    .replace(/\s+/g, ' ')
    .trim();
}

/**
 * Cleans fantasy character description by truncating to a maximum length and adding ellipsis
 * @param description The original description/prompt text
 * @param maxLength Maximum length of the description (default: 100 characters)
 * @returns The cleaned and truncated description text
 */
export function cleanFantasyDescription(description: string, maxLength: number = 100): string {
  if (!description) return '';
  
  // Clean the text first
  let cleanedText = cleanText(description);
  
  // Truncate to maxLength characters if needed
  if (cleanedText.length > maxLength) {
    // Find the last complete word within the maxLength limit
    const lastSpaceIndex = cleanedText.lastIndexOf(' ', maxLength);
    const truncateIndex = lastSpaceIndex > 0 ? lastSpaceIndex : maxLength;
    
    // Truncate and add ellipsis
    cleanedText = cleanedText.substring(0, truncateIndex) + '...';
  }
  
  return cleanedText;
}

/**
 * Übersetzt den englischen Pokémon-Typ in die deutsche Version
 */
export const translatePokemonType = (englishType: string): string | null => {
  const typeMap: Record<string, string> = {
    'normal': 'NORMAL',
    'fire': 'FEUER',
    'water': 'WASSER',
    'grass': 'PFLANZE',
    'electric': 'ELEKTRO',
    'ice': 'EIS',
    'fighting': 'KAMPF',
    'poison': 'GIFT',
    'ground': 'BODEN',
    'flying': 'FLUG',
    'psychic': 'PSYCHO',
    'bug': 'KÄFER',
    'rock': 'GESTEIN',
    'ghost': 'GEIST',
    'dragon': 'DRACHE',
    'dark': 'UNLICHT',
    'steel': 'STAHL',
    'fairy': 'FEE'
  };

  return typeMap[englishType.toLowerCase()] || null;
};