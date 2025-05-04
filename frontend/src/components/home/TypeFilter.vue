<template>
  <div class="type-chip-container">
    <v-chip
      v-for="type in pokemonTypes"
      :key="type.value"
      :color="isTypeSelected(type.value) ? getTypeColor(type.value) : 'default'"
      :class="['type-chip', isTypeSelected(type.value) ? 'selected' : '']"
      :text-color="isTypeSelected(type.value) ? 'white' : ''"
      @click="toggleTypeFilter(type.value)"
      filter
      :filter-icon="isTypeSelected(type.value) ? 'mdi-check' : ''"
      variant="elevated"
    >
      {{ type.label }}
    </v-chip>
  </div>
</template>

<script setup lang="ts">
import { PokemonType } from '@/types/pokemon';

const props = defineProps<{
  selectedTypes: PokemonType[];
  pokemonTypes: {label: string, value: PokemonType}[];
}>();

const emit = defineEmits<{
  (e: 'update:selectedTypes', value: PokemonType[]): void;
  (e: 'filterChanged'): void;
}>();

// Funktion zum Überprüfen, ob ein Typ ausgewählt ist
function isTypeSelected(type: PokemonType): boolean {
  return props.selectedTypes.includes(type);
}

// Funktion zum Umschalten des Typ-Filters
function toggleTypeFilter(type: PokemonType) {
  const newSelectedTypes = [...props.selectedTypes];
  
  if (isTypeSelected(type)) {
    // Typ entfernen, wenn er bereits ausgewählt ist
    const index = newSelectedTypes.indexOf(type);
    if (index !== -1) {
      newSelectedTypes.splice(index, 1);
    }
  } else {
    // Typ hinzufügen, wenn er noch nicht ausgewählt ist
    newSelectedTypes.push(type);
  }
  
  emit('update:selectedTypes', newSelectedTypes);
  emit('filterChanged');
}

// Funktion zum Abrufen der Farbe für einen Pokémon-Typ
function getTypeColor(type: PokemonType | null): string {
  if (!type) return '#A8A878'; // Standardfarbe
  
  const typeColors: Record<PokemonType, string> = {
    [PokemonType.NORMAL]: '#A8A878',
    [PokemonType.FEUER]: '#F08030',
    [PokemonType.WASSER]: '#6890F0',
    [PokemonType.PFLANZE]: '#78C850',
    [PokemonType.ELEKTRO]: '#F8D030',
    [PokemonType.EIS]: '#98D8D8',
    [PokemonType.KAMPF]: '#C03028',
    [PokemonType.GIFT]: '#A040A0',
    [PokemonType.BODEN]: '#E0C068',
    [PokemonType.FLUG]: '#A890F0',
    [PokemonType.PSYCHO]: '#F85888',
    [PokemonType.KÄFER]: '#A8B820',
    [PokemonType.GESTEIN]: '#B8A038',
    [PokemonType.GEIST]: '#705898',
    [PokemonType.DRACHE]: '#7038F8',
    [PokemonType.UNLICHT]: '#705848',
    [PokemonType.STAHL]: '#B8B8D0',
    [PokemonType.FEE]: '#EE99AC'
  };

  return typeColors[type];
}
</script>

<style scoped>
.type-chip-container {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
  margin: 0 auto;
  padding: 8px;
}

.type-chip {
  margin: 0;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid rgba(0, 0, 0, 0.1);
}

.type-chip:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.type-chip.selected {
  font-weight: 500;
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

/* Mobile Anpassungen */
@media (max-width: 600px) {
  .type-chip-container {
    gap: 6px;
    padding: 4px;
  }

  .type-chip {
    font-size: 0.75rem !important;
    height: 28px !important;
  }
}

/* Tablet-Anpassungen */
@media (min-width: 601px) and (max-width: 960px) {
  .type-chip-container {
    padding: 6px;
  }
}

/* Desktop-Anpassungen */
@media (min-width: 961px) {
  .type-chip-container {
    gap: 10px;
    padding: 10px;
  }
}
</style>