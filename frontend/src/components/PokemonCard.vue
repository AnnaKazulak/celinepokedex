<template>
  <div 
    class="pokemon-card-container" 
    :data-id="pokemon.pokedexNumber" 
    ref="cardElement"
    @click="navigateToDetail"
    :class="{ 'clickable': true }"
  >
    <div class="pokemon-image-wrapper">
      <v-img
        :src="pokemon.imageUrl"
        class="pokemon-image"
        contain
        @load="extractColorFromImage"
      ></v-img>
    </div>

    <v-card class="pokemon-card" :style="{ backgroundColor: dominantColor + '22' }">
      <div class="card-content">
        <div class="pokemon-header">
          <div class="pokemon-name">{{ pokemon.name }}</div>
          
          <!-- Type-Chips hinzufügen -->
          <div class="type-chips">
            <v-chip
              v-if="pokemon.type1" 
              class="type-chip"
              :color="getTypeColor(pokemon.type1)"
              text-color="white"
              size="small"
            >
              {{ pokemon.type1 }}
            </v-chip>
            <v-chip
              v-if="pokemon.type2" 
              class="type-chip"
              :color="getTypeColor(pokemon.type2)"
              text-color="white"
              size="small"
            >
              {{ pokemon.type2 }}
            </v-chip>
          </div>
          
          <div class="description">
            {{ pokemon.description || 'Keine Beschreibung verfügbar' }}
          </div>
        </div>

        <div class="pokemon-footer" :style="{ background: dominantColor }">
          <div class="footer-value">
            <strong>{{ pokemon.height }} m</strong>
            <span>Größe</span>
          </div>
          <div class="footer-value">
            <strong>{{ pokemon.weight }} kg</strong>
            <span>Gewicht</span>
          </div>
          <div class="footer-value">
            <strong>#{{ pokemon.pokedexNumber }}</strong>
            <span>Nr.</span>
          </div>
        </div>
      </div>
    </v-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue';
import { useRouter } from 'vue-router';
import { extractDominantColor } from '../utils/colorUtils';
import { eventBus } from '../utils/eventBus';
import { Pokemon, PokemonType } from '../types/pokemon';

const props = defineProps<{
  pokemon: Pokemon
}>();

const router = useRouter();
const dominantColor = ref('#ffffff'); // Standard-Fallback-Farbe
const cardElement = ref<HTMLElement | null>(null);

// Extrahiere die dominante Farbe aus dem Pokémon-Bild
async function extractColorFromImage() {
  try {
    if (props.pokemon.imageUrl) {
      // Kurze Verzögerung, um sicherzustellen, dass das Bild geladen ist
      setTimeout(async () => {
        dominantColor.value = await extractDominantColor(props.pokemon.imageUrl || '');
        
        // Farbe zur globalen Farbpalette hinzufügen
        registerCardColor();
      }, 100);
    }
  } catch (error) {
    console.error('Fehler bei der Farbextraktion:', error);
  }
}

// Separate function to register the card color with the navbar
function registerCardColor() {
  // Make sure we have a reference to the actual DOM element
  const element = cardElement.value || document.querySelector(`.pokemon-card-container[data-id="${props.pokemon.pokedexNumber}"]`);
  
  eventBus.emit('register-pokemon-color', {
    id: props.pokemon.pokedexNumber,
    color: dominantColor.value,
    element: element
  });
}

// Navigation zur Detailansicht
function navigateToDetail() {
  if (props.pokemon.pokedexNumber) {
    router.push({ name: 'pokemonDetail', params: { id: props.pokemon.pokedexNumber } });
  }
}

// Funktion zum Generieren eines zufälligen Levels für Pokémon (1-100)
function generateRandomLevel(): number {
  // Verwenden wir die Pokémon-Nummer für ein konsistentes Level
  const pokedexNum = parseInt(props.pokemon.pokedexNumber.replace(/^0+/, '')) || 0;
  // Kombiniere pokedexNum mit dem Namen für bessere Streuung
  const seed = pokedexNum + props.pokemon.name.length;
  return (seed % 100) + 1; // Level zwischen 1-100
}

// Funktion, um die passende Farbe für den Pokemon-Typ zu erhalten
function getTypeColor(type: PokemonType): string {
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

  return typeColors[type] || '#A8A878'; // Fallback zu Normal-Typ, wenn keine Übereinstimmung
}

onMounted(() => {
  extractColorFromImage();
  
  // Listen for home view mounted event to re-register colors
  eventBus.on('home-view-mounted', () => {
    // Small delay to ensure DOM is ready
    setTimeout(() => {
      registerCardColor();
    }, 100);
  });
  
  // Listen for direct force reregistration event
  eventBus.on('reregister-all-cards', () => {
    // Different delay to avoid timing conflicts
    setTimeout(() => {
      registerCardColor();
    }, 150);
  });
});

onBeforeUnmount(() => {
  // Clean up event listeners
  eventBus.off('home-view-mounted');
  eventBus.off('reregister-all-cards');
});
</script>

<style scoped>
.pokemon-card-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 25px auto;
  position: relative;
  width: 100%;
  height: 480px;
}

.clickable {
  cursor: pointer;
  transition: transform 0.3s ease;
}

.clickable:hover {
  transform: translateY(-8px);
}

.pokemon-image-wrapper {
  position: absolute;
  top: -25px;
  z-index: 10;
  width: 240px;
  height: 240px;
  display: flex;
  justify-content: center;
}

.pokemon-image {
  filter: drop-shadow(0 8px 16px rgba(0, 0, 0, 0.3));
  transform: scale(1.1);
  transition: transform 0.5s ease;
}

.clickable:hover .pokemon-image {
  transform: scale(1.15);
}

.pokemon-card {
  width: 100%;
  max-width: 300px;
  height: 400px;
  border-radius: 16px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
  margin: 0 auto;
  margin-top: 60px;
  overflow: hidden;
  position: relative;
}

.card-content {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
}

.pokemon-header {
  text-align: center;
  padding: 16px;
  background-color: white;
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  padding-top: 180px; /* Erhöht von 140px auf 180px, um Header nach unten zu verschieben */
  position: relative;
}

.pokemon-name {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 8px;
}

.type-chips {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-bottom: 12px;
}

.type-chip {
  font-size: 11px;
  height: 24px !important;
  text-transform: capitalize;
  font-weight: 500;
}

.description {
  font-size: 14px;
  color: #666;
  margin-top: 8px;
  max-height: 80px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}

.pokemon-footer {
  color: white;
  display: flex;
  justify-content: space-around;
  padding: 12px 8px;
  transition: background-color 0.5s ease;
}

.footer-value {
  text-align: center;
}

.footer-value span {
  display: block;
  font-size: 12px;
  font-weight: 400;
}

/* Mobile Anpassungen */
@media (max-width: 600px) {
  .pokemon-card-container {
    margin: 20px auto;
    height: 420px;
  }
  
  .pokemon-image-wrapper {
    width: 200px;
    height: 200px;
    top: -20px;
  }
  
  .pokemon-card {
    max-width: 280px;
    height: 360px;
    margin-top: 40px;
  }
  
  .pokemon-header {
    padding: 12px;
    padding-top: 140px; /* Angepasst für mobile Ansicht */
  }
  
  .pokemon-name {
    font-size: 20px;
    margin-bottom: 6px;
  }
  
  .type-chip {
    font-size: 10px;
    height: 22px !important;
  }
  
  .description {
    font-size: 13px;
    max-height: 65px;
    -webkit-line-clamp: 3;
    margin-top: 6px;
  }
  
  .pokemon-footer {
    padding: 10px 6px;
  }
  
  .footer-value strong {
    font-size: 14px;
  }
  
  .footer-value span {
    font-size: 11px;
  }
}

/* Extra kleine Geräte */
@media (max-width: 320px) {
  .pokemon-card-container {
    height: 400px;
  }
  
  .pokemon-image-wrapper {
    width: 170px;
    height: 170px;
    top: -15px;
  }
  
  .pokemon-card {
    height: 340px;
    margin-top: 30px;
  }
  
  .pokemon-header {
    padding-top: 120px; /* Angepasst für sehr kleine Geräte */
  }
  
  .description {
    -webkit-line-clamp: 2;
    max-height: 60px;
  }
}
</style>
