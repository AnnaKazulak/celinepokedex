<template>
  <div class="pokemon-header">
    <div class="pokemon-image-container">
      <v-img
        :src="pokemon.imageUrl"
        height="400"
        width="400"
        contain
        class="pokemon-image"
        @load="extractColorFromImage"
      ></v-img>
    </div>
    <div class="pokemon-info">
      <div class="mb-4">
        <div class="d-flex align-center">
          <h1 class="text-h2 font-weight-bold mr-2" 
              :style="{ color: isColorLoaded ? dominantColor : 'rgba(0,0,0,0.87)' }">
            {{ pokemon.name }}
          </h1>
          <span class="text-h5 text-grey">#{{ pokemon.pokedexNumber }}</span>
        </div>
        <p class="text-subtitle-1">{{ pokemon.description || 'Keine Beschreibung verfügbar' }}</p>
      </div>

      <!-- Types -->
      <div class="d-flex mb-6">
        <v-chip
          v-if="pokemon.type1" 
          class="mr-2"
          :color="getTypeColor(pokemon.type1)"
          text-color="white"
        >
          {{ pokemon.type1 }}
        </v-chip>
        <v-chip
          v-if="pokemon.type2" 
          :color="getTypeColor(pokemon.type2)"
          text-color="white"
        >
          {{ pokemon.type2 }}
        </v-chip>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue';
import { type Pokemon, PokemonType } from '@/types/pokemon';
import { extractDominantColor } from '@/utils/colorUtils';
import { eventBus } from '@/utils/eventBus';

// Props
const props = defineProps<{
  pokemon: Pokemon;
}>();

// Emit events
const emit = defineEmits<{
  (event: 'colorLoaded', color: string): void;
  (event: 'colorLoadingStateChanged', isLoaded: boolean): void;
}>();

// States
const dominantColor = ref('#b20072'); // Standard-Fallback-Farbe
const isColorLoaded = ref(false);

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

// Extrahiere die dominante Farbe aus dem Pokémon-Bild
async function extractColorFromImage() {
  try {
    if (props.pokemon && props.pokemon.imageUrl) {
      // Kurze Verzögerung, um sicherzustellen, dass das Bild geladen ist
      setTimeout(async () => {
        const color = await extractDominantColor(props.pokemon?.imageUrl || '');
        dominantColor.value = color;
        
        // Farbe an die übergeordnete Komponente senden
        emit('colorLoaded', color);
        
        // Nach kurzer Verzögerung aktivieren, damit die Transition funktioniert
        setTimeout(() => {
          isColorLoaded.value = true;
          emit('colorLoadingStateChanged', true);
        }, 50);
      }, 100);
    }
  } catch (error) {
    console.error('Fehler bei der Farbextraktion:', error);
    // Selbst bei einem Fehler sollten wir die Komponente anzeigen
    isColorLoaded.value = true;
    emit('colorLoadingStateChanged', true);
  }
}

// Beim Verlassen der Seite die Farbe zurücksetzen
onBeforeUnmount(() => {
  // Null oder leerer String signalisiert, dass wir zur Standard-Farbe zurückkehren sollen
  eventBus.emit('detail-page-color-change', '');
});
</script>

<style scoped>
.pokemon-header {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 1rem;
  max-width: 1400px;
  margin: 0 auto;
  justify-content: center;
}

.pokemon-image-container {
  flex: 1;
  min-width: 250px; /* Reduziert für besseres mobiles Layout */
  display: flex;
  justify-content: center;
  padding: 0;
}

.pokemon-info {
  flex: 1;
  min-width: 280px; /* Reduziert für besseres mobiles Layout */
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.pokemon-image {
  filter: drop-shadow(0 8px 16px rgba(0, 0, 0, 0.2));
  transform: scale(1.05);
  transition: transform 0.5s ease;
}

.pokemon-image:hover {
  transform: scale(1.1);
}

/* Größere Desktop-Bildschirme */
@media (min-width: 1200px) {
  .pokemon-header {
    flex-direction: row-reverse;
    align-items: center;
    gap: 0;
    padding: 0 2rem;
  }
  
  .pokemon-image-container {
    margin-left: -50px; /* Mehr Überlappung auf großen Screens */
  }
  
  .pokemon-info {
    padding-right: 30px;
    padding-left: 50px;
    margin-left: 30px;
  }
  
  .pokemon-image {
    height: 450px !important;
    width: 450px !important;
  }
}

/* Mittlere Desktops und Tablets im Querformat */
@media (min-width: 769px) and (max-width: 1199px) {
  .pokemon-header {
    flex-direction: row-reverse;
    align-items: center;
    gap: 0;
  }
  
  .pokemon-image-container {
    margin-left: -30px;
  }
  
  .pokemon-info {
    padding-right: 20px;
    padding-left: 40px;
    margin-left: 20px;
  }
  
  .pokemon-image {
    height: 350px !important;
    width: 350px !important;
  }
}

/* Tablets im Hochformat */
@media (min-width: 601px) and (max-width: 768px) {
  .pokemon-header {
    flex-direction: column;
    gap: 0.5rem;
  }
  
  .pokemon-image {
    height: 320px !important;
    width: 320px !important;
  }
  
  .pokemon-info {
    text-align: center;
    padding: 0 1rem;
  }
  
  .pokemon-info .d-flex {
    justify-content: center;
  }
}

/* Mobile Geräte */
@media (max-width: 600px) {
  .pokemon-header {
    flex-direction: column;
    gap: 0.5rem;
  }
  
  .pokemon-image {
    height: 250px !important;
    width: 250px !important;
  }
  
  .pokemon-info {
    text-align: center;
    padding: 0 0.5rem;
  }
  
  .pokemon-info .d-flex {
    justify-content: center;
    flex-wrap: wrap;
  }
  
  .pokemon-info h1 {
    font-size: 2rem !important;
    line-height: 1.2;
    margin-right: 0.5rem;
  }
  
  .pokemon-info .text-h5 {
    font-size: 1.25rem !important;
  }
}
</style>