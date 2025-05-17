<template>
  <div>
    <div v-if="isLoading" class="d-flex justify-center align-center" style="min-height: 80vh">
      <v-progress-circular indeterminate color="primary" size="64"></v-progress-circular>
    </div>

    <template v-else-if="pokemon">
      <!-- Komponente für den Dialog zum Hinzufügen fehlender Pokémon -->
      <MissingPokemonDialog
        v-model="showMissingPokemonDialog"
        :pokemon="missingPokemon"
        :dominant-color="dominantColor"
        @create="createMissingPokemon"
      />

      <!-- Pokemon Form Dialog für das Erstellen des fehlenden Pokémons -->
      <PokemonFormDialog
        v-model:dialog="showPokemonFormDialog"
        :prefill-data="prefillPokemonData"
        @pokemon-created="onPokemonCreated"
      />

      <div 
        class="pa-8 pa-sm-10 px-md-12 w-100 transition-fade"
        :style="{
          background: isColorLoaded ? `linear-gradient(180deg, ${dominantColorLight} 0%, white 100%)` : 'transparent',
          opacity: isColorLoaded ? 1 : 0,
          minHeight: '100vh'
        }"
        :class="{ 'opacity-1': isColorLoaded }"
      >
        <!-- Back Button -->
        <div class="pb-4">
          <v-btn
            prepend-icon="mdi-arrow-left"
            variant="text"
            @click="goBack"
            class="mb-4 font-weight-bold transition-transform"
            :style="{ color: isColorLoaded ? dominantColor : 'rgba(0,0,0,0.7)' }"
          >
            Zurück
          </v-btn>
        </div>

        <!-- Komponente für den Pokemon-Header -->
        <PokemonHeader 
          :pokemon="pokemon"
          @color-loaded="onColorLoaded"
          @color-loading-state-changed="onColorLoadingStateChanged"
        />

        <!-- Komponente für die Pokemon-Details -->
        <PokemonDetailsCard
          :pokemon="pokemon"
          :dominant-color="dominantColor"
        />
        
        <!-- Komponente für die Evolution Chain -->
        <EvolutionChainCard
          :pokemon="pokemon"
          :evolution-chain="evolutionChain"
          :dominant-color="dominantColor"
          :current-pokemon-id="pokemonId"
          :known-evolution-chains="knownEvolutionChains"
          @missing-pokemon-click="handleMissingPokemonClick"
        />
      </div>
    </template>

    <div v-else class="d-flex flex-column align-center justify-center pa-8" style="min-height: 50vh">
      <v-alert
        type="error"
        variant="tonal"
        width="100%"
        max-width="500px"
      >
        Pokemon wurde nicht gefunden.
      </v-alert>
      <v-btn
        class="mt-4"
        color="primary"
        @click="goBack"
      >
        Zurück zur Übersicht
      </v-btn>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, onBeforeUnmount, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import axios from 'axios';
import { type Pokemon, type PokemonFormData } from '@/types/pokemon';
import { eventBus } from '@/utils/eventBus';
import PokemonHeader from '@/components/pokemon/PokemonHeader.vue';
import PokemonDetailsCard from '@/components/pokemon/PokemonDetailsCard.vue';
import EvolutionChainCard from '@/components/pokemon/EvolutionChainCard.vue';
import MissingPokemonDialog from '@/components/pokemon/MissingPokemonDialog.vue';
import PokemonFormDialog from '@/components/pokemon/PokemonFormDialog.vue';

// Dialog state variables for missing Pokemon
const showMissingPokemonDialog = ref(false);
const showPokemonFormDialog = ref(false);
const missingPokemon = ref<{ name: string, pokedexNumber: string, imageUrl?: string }>({ name: '', pokedexNumber: '' });
const prefillPokemonData = ref<PokemonFormData | null>(null);

// Other existing variables
const router = useRouter();
const route = useRoute();
const pokemon = ref<Pokemon | null>(null);
const isLoading = ref(true);
const error = ref<string | null>(null);
const dominantColor = ref('#b20072'); // Standard-Fallback-Farbe
const dominantColorLight = ref('rgba(178, 0, 114, 0.15)'); // Hellere Version für Hintergründe
const isColorLoaded = ref(false); // State-Variable für den Übergang
const evolutionChain = ref<any | null>(null); // Typ auf "any" geändert, um TypeScript-Fehler zu vermeiden
const evolutionLoading = ref(false);
const missingEvolutions = ref<string[]>([]); // Variable für fehlende Evolutionen
const knownEvolutionChains = ref<Record<number, {name: string, pokedexNumber: string}[]>>({});

// Pokemon ID aus der URL lesen
const pokemonId = computed(() => {
  return route.params.id as string;
});

onMounted(async () => {
  await fetchPokemonDetails();
});

// Watch for route parameter changes to reload data when navigating between Pokémon
watch(() => route.params.id, async (newId, oldId) => {
  if (newId !== oldId) {
    isLoading.value = true;
    isColorLoaded.value = false;
    await fetchPokemonDetails();
  }
});

async function fetchPokemonDetails() {
  isLoading.value = true;
  isColorLoaded.value = false; // Reset beim Laden neuer Daten
  error.value = null;
  evolutionChain.value = null;
  missingEvolutions.value = []; // Reset fehlende Evolutionen

  try {
    const response = await axios.get(`http://localhost:8080/api/pokemons/${pokemonId.value}`);
    pokemon.value = response.data;
    
    // Nach dem Laden des Pokemons die bekannten Evolutionsketten laden
    if (pokemon.value.evolutionChainId) {
      await fetchKnownEvolutionChains(pokemon.value.evolutionChainId);
    }
    
    // Nach dem Laden des Pokemons auch die Evolutionsdaten laden
    await fetchEvolutionChain();
  } catch (err) {
    console.error('Fehler beim Laden der Pokemon-Details:', err);
    error.value = 'Pokemon konnte nicht geladen werden';
  } finally {
    isLoading.value = false;
  }
}

async function fetchEvolutionChain() {
  // Prüfe, ob das Pokemon existiert und ob es eine evolutionChainId hat
  if (!pokemon.value || !pokemon.value.evolutionChainId) {
    evolutionLoading.value = false;
    return;
  }
  
  evolutionLoading.value = true;
  
  try {
    const response = await axios.get(`http://localhost:8080/api/pokemons/${pokemon.value.pokedexNumber}/evolution-chain`);
    evolutionChain.value = response.data;
    console.log('Evolutionskette geladen:', evolutionChain.value);
    
    // Nach dem Laden der Evolutionskette überprüfen, ob Pokémon fehlen
    checkMissingEvolutions();
    
  } catch (err) {
    console.error('Fehler beim Laden der Evolutionskette:', err);
    // Keine Fehlermeldung anzeigen, da die Evolutionskette optional ist
  } finally {
    evolutionLoading.value = false;
  }
}

// Lädt bekannte Pokémon aus der gleichen Evolutionskette
async function fetchKnownEvolutionChains(chainId: number) {
  try {
    // Holt alle Pokémon mit der gleichen evolutionChainId aus der Datenbank
    const response = await axios.get('http://localhost:8080/api/pokemons', {
      params: { evolutionChainId: chainId }
    });
    
    if (response.data && response.data.length > 0) {
      // Speichert die Pokémon nach evolutionChainId
      knownEvolutionChains.value[chainId] = response.data.map((p: Pokemon) => ({
        name: p.name,
        pokedexNumber: p.pokedexNumber
      }));
    }
  } catch (err) {
    console.error('Fehler beim Laden bekannter Evolutionsketten:', err);
  }
}

// Überprüft, ob Pokémon in der Evolutionskette fehlen
async function checkMissingEvolutions() {
  if (!evolutionChain.value || !evolutionChain.value.id || !evolutionChain.value.stages) return;
  
  try {
    // ...existing code...
    const stages = evolutionChain.value.stages;
    if (stages.length <= 1) return; // Keine Evolution notwendig
    
    // Sortiere die Stufen nach Pokédex-Nummer
    const sortedStages = [...stages].sort((a, b) => 
      parseInt(a.pokemon.pokedexNumber) - parseInt(b.pokemon.pokedexNumber)
    );
    
    // Prüfe auf fehlende Nummern zwischen dem ersten und letzten Pokémon
    for (let i = 0; i < sortedStages.length - 1; i++) {
      const currentNum = parseInt(sortedStages[i].pokemon.pokedexNumber);
      const nextNum = parseInt(sortedStages[i+1].pokemon.pokedexNumber);
      
      // Wenn die Differenz größer als 1 ist, fehlen Pokémon dazwischen
      if (nextNum - currentNum > 1) {
        for (let j = currentNum + 1; j < nextNum; j++) {
          // Füge die fehlende Pokédex-Nummer zur Liste hinzu
          const missingNum = j.toString().padStart(3, '0');
          if (!missingEvolutions.value.includes(missingNum)) {
            missingEvolutions.value.push(missingNum);
          }
        }
      }
    }
    
    // Prüfe alle bekannten Pokémon mit derselben evolutionChainId
    if (pokemon.value?.evolutionChainId && knownEvolutionChains.value[pokemon.value.evolutionChainId]) {
      const knownPokemon = knownEvolutionChains.value[pokemon.value.evolutionChainId];
      const stagePokedexNumbers = stages.map(s => s.pokemon.pokedexNumber);
      
      // Finde Pokémon, die in der Datenbank vorhanden sind, aber nicht in der aktuellen Evolutionskette angezeigt werden
      knownPokemon.forEach(p => {
        if (!stagePokedexNumbers.includes(p.pokedexNumber) && !missingEvolutions.value.includes(p.pokedexNumber)) {
          missingEvolutions.value.push(p.pokedexNumber);
        }
      });
    }
    
    // Sortiere die fehlenden Pokédex-Nummern
    missingEvolutions.value.sort((a, b) => parseInt(a) - parseInt(b));
    // ...existing code...
  } catch (err) {
    console.error('Fehler beim Überprüfen fehlender Evolutionen:', err);
  }
}

function goBack() {
  router.push({ name: 'home' });
}

// Callback-Funktion für die Farbe aus dem PokemonHeader
function onColorLoaded(color: string) {
  dominantColor.value = color;
  
  // Hellere Version der Farbe für Hintergründe erstellen
  const rgbMatch = color.match(/rgb\((\d+),\s*(\d+),\s*(\d+)\)/);
  if (rgbMatch) {
    const r = parseInt(rgbMatch[1], 10);
    const g = parseInt(rgbMatch[2], 10);
    const b = parseInt(rgbMatch[3], 10);
    dominantColorLight.value = `rgba(${r}, ${g}, ${b}, 0.15)`;
  } else if (color.startsWith('#')) {
    // Hex-Farbe umwandeln
    const r = parseInt(color.slice(1, 3), 16);
    const g = parseInt(color.slice(3, 5), 16);
    const b = parseInt(color.slice(5, 7), 16);
    dominantColorLight.value = `rgba(${r}, ${g}, ${b}, 0.15)`;
  }

  // Farbe an die Navbar senden
  eventBus.emit('detail-page-color-change', color);
}

// Callback-Funktion für den Ladestatus der Farbe
function onColorLoadingStateChanged(isLoaded: boolean) {
  isColorLoaded.value = isLoaded;
}

// Handle click on greyed-out Pokémon
function handleMissingPokemonClick(pokemon: { name: string, pokedexNumber: string, imageUrl?: string }) {
  missingPokemon.value = pokemon;
  showMissingPokemonDialog.value = true;
}

// Create missing Pokémon
function createMissingPokemon() {
  prefillPokemonData.value = {
    name: missingPokemon.value.name,
    pokedexNumber: missingPokemon.value.pokedexNumber,
    description: '', // Leere Beschreibung als Platzhalter
    imageUrl: missingPokemon.value.imageUrl || '', // Bildpfad übernehmen, falls vorhanden
    height: null, // Standardwert für numerische Felder
    weight: null, // Standardwert für numerische Felder
    type1: null, // Standardwert für Typen
    type2: null, // Standardwert für Typen
    category: '', // Leere Kategorie als Platzhalter
    ability: '', // Leere Fähigkeit als Platzhalter
    evolutionChainId: pokemon.value?.evolutionChainId || null,
    evolvesFromId: pokemon.value?.pokedexNumber, // Aktuelles Pokémon als Vorgänger setzen, falls es eine Entwicklung ist
    evolutionTrigger: '',
    evolutionCondition: ''
  };
  showMissingPokemonDialog.value = false;
  showPokemonFormDialog.value = true;
}

// Handle Pokémon created event
function onPokemonCreated(newPokemon: Pokemon) {
  // Zur Detailansicht des neu erstellten Pokémons navigieren
  router.push({ name: 'pokemonDetail', params: { id: newPokemon.pokedexNumber } });
}
</script>

<style scoped>
/* Nur die nötigsten speziellen Stile behalten, die nicht durch Vuetify-Klassen abgedeckt werden */
.transition-fade {
  transition: all 0.8s ease;
}

.transition-transform {
  transition: transform 0.3s ease;
}

.transition-transform:hover {
  transform: translateX(-5px);
}
</style>