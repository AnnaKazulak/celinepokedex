<template>
  <v-container class="pt-24 pb-5 px-2">
    <!-- Header und Suchfeld -->
    <div class="search-container">
      <!-- Suchfeld-Komponente -->
      <SearchBar
        v-model:searchQuery="searchQuery"
        @search="searchPokemons"
      />
      
      <!-- Filter- und Sortieroptionen -->
      <div class="filter-container">
        <!-- Sortierung-Komponente -->
        <SortToggle
          :sort-options="sortOptions"
          v-model:sortOptionIndex="sortOptionIndex"
        />
        
        <!-- Typ-Filter-Komponente -->
        <TypeFilter
          v-model:selectedTypes="selectedTypes"
          :pokemon-types="pokemonTypes"
          @filterChanged="filterPokemonsByType"
        />
      </div>
    </div>
    
    <!-- Keine Ergebnisse Nachricht -->
    <NoResultsMessage v-if="filteredPokemons.length === 0 && !isLoading" />
    
    <!-- Pokemon Grid -->
    <PokemonGrid v-else :pokemons="filteredPokemons" />
    
    <!-- Ladeindikator -->
    <LoadingIndicator v-if="isLoading" />
  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, onBeforeUnmount } from 'vue';
import axios from 'axios';
import { eventBus } from '../utils/eventBus';
import { Pokemon, PokemonType } from '../types/pokemon';

// Importiere die neuen Unterkomponenten
import SearchBar from '@/components/home/SearchBar.vue';
import TypeFilter from '@/components/home/TypeFilter.vue';
import SortToggle from '@/components/home/SortToggle.vue';
import PokemonGrid from '@/components/home/PokemonGrid.vue';
import NoResultsMessage from '@/components/home/NoResultsMessage.vue';
import LoadingIndicator from '@/components/home/LoadingIndicator.vue';

const pokemons = ref<Pokemon[]>([])
const searchQuery = ref('')
const isLoading = ref(false)
const dialog = ref(false)
const isFocused = ref(false) // Variable für den Fokuszustand
const selectedTypes = ref<PokemonType[]>([])

// Erstelle eine Liste aller Pokémon-Typen für das Dropdown
const pokemonTypes = Object.values(PokemonType).map(type => ({
  label: type, // Wir nutzen den Enum-Wert selbst als Label
  value: type
}));

// Sortieroptionen
const sortOptions = [
  { text: 'Name', value: 'name' },
  { text: 'Pokedex Nummer', value: 'pokedexNumber' },
  { text: 'Neueste', value: 'newestFirst' },
  { text: 'Älteste', value: 'oldestFirst' }
]
const sortOptionIndex = ref(0) // Defaultwert ist 0 (Name)

// Computed property für sortOption basierend auf dem sortOptionIndex
const sortOption = computed(() => {
  return sortOptions[sortOptionIndex.value].value
})

// Computed property to filter and sort pokemons based on search query and selected types
const filteredPokemons = computed(() => {
  let result = [...pokemons.value];
  
  // Nach Typen filtern, falls ausgewählt
  if (selectedTypes.value.length > 0) {
    result = result.filter(pokemon => 
      // Ein Pokémon wird angezeigt, wenn es einen der ausgewählten Typen hat
      selectedTypes.value.some(type => 
        pokemon.type1 === type || pokemon.type2 === type
      )
    );
  }
  
  // Dann nach Suchbegriff filtern, falls vorhanden
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase();
    result = result.filter(pokemon => 
      pokemon.name.toLowerCase().includes(query) || 
      pokemon.pokedexNumber.includes(query)
    );
  }
  
  // Nach der ausgewählten Sortieroption sortieren
  if (sortOption.value === 'name') {
    result.sort((a, b) => a.name.localeCompare(b.name));
  } else if (sortOption.value === 'pokedexNumber') {
    result.sort((a, b) => a.pokedexNumber.localeCompare(b.pokedexNumber));
  } else if (sortOption.value === 'newestFirst') {
    result.sort((a, b) => {
      const dateA = a.createdAt ? new Date(a.createdAt).getTime() : 0;
      const dateB = b.createdAt ? new Date(b.createdAt).getTime() : 0;
      return dateB - dateA; // Neueste zuerst (absteigend)
    });
  } else if (sortOption.value === 'oldestFirst') {
    result.sort((a, b) => {
      const dateA = a.createdAt ? new Date(a.createdAt).getTime() : 0;
      const dateB = b.createdAt ? new Date(b.createdAt).getTime() : 0;
      return dateA - dateB; // Älteste zuerst (aufsteigend)
    });
  }
  
  return result;
})

// Funktion zum Leeren der Typ-Filter
function clearTypeFilter() {
  selectedTypes.value = [];
  loadAllPokemons();
}

// Funktion zur Filterung von Pokémon nach Typen
async function filterPokemonsByType() {
  isLoading.value = true;
  try {
    if (selectedTypes.value.length > 0) {
      // Wenn Typen ausgewählt sind, verwende den API-Endpunkt mit den Typ-Parametern
      const response = await axios.get<Pokemon[]>('http://localhost:8080/api/pokemons', {
        params: { 
          // Axios wandelt Array in RequestParams mit Indizes um: ?types[0]=FEUER&types[1]=WASSER
          types: selectedTypes.value 
        },
        // Stellt sicher, dass Arrays als separate Request-Parameter gesendet werden
        paramsSerializer: {
          indexes: null // null bedeutet keine Indizes, also ?types=FEUER&types=WASSER
        }
      });
      pokemons.value = response.data;
    } else {
      // Wenn kein Typ ausgewählt ist, lade alle Pokémon
      await loadAllPokemons();
    }
  } catch (error) {
    console.error('Fehler beim Filtern nach Typ:', error);
  } finally {
    isLoading.value = false;
  }
}

// Load all pokemons initially
onMounted(async () => {
  await loadAllPokemons()
})

// Function to load all pokemons
async function loadAllPokemons() {
  isLoading.value = true
  try {
    const response = await axios.get<Pokemon[]>('http://localhost:8080/api/pokemons')
    pokemons.value = response.data
  } catch (error) {
    console.error('Error loading pokemons:', error)
  } finally {
    isLoading.value = false
  }
}

// Function to search pokemons using the API
async function searchPokemons() {
  // Only use the API search if the query is at least 2 characters
  if (searchQuery.value && searchQuery.value.length >= 2) {
    isLoading.value = true
    try {
      const response = await axios.get<Pokemon[]>('http://localhost:8080/api/pokemons/search', {
        params: {
          name: isNaN(Number(searchQuery.value)) ? searchQuery.value : null,
          pokedexNumber: !isNaN(Number(searchQuery.value)) ? searchQuery.value : null,
          types: selectedTypes.value.length > 0 ? selectedTypes.value : null
        },
        paramsSerializer: {
          indexes: null
        }
      })
      pokemons.value = response.data
    } catch (error) {
      console.error('Error searching pokemons:', error)
    } finally {
      isLoading.value = false
    }
  } else if (!searchQuery.value) {
    // If search is cleared, load all pokemons again (with type filter if active)
    if (selectedTypes.value.length > 0) {
      await filterPokemonsByType();
    } else {
      await loadAllPokemons();
    }
  }
}

// Event-Listener registrieren und bereinigen
onMounted(() => {
  eventBus.on('pokemon-created', handleNewPokemon);
});

onBeforeUnmount(() => {
  eventBus.off('pokemon-created', handleNewPokemon);
});

// Event-Listener für "pokemon-created" registrieren
function handleNewPokemon(newPokemon: Pokemon) {
  // Füge das neue Pokémon am Anfang der Liste hinzu, wenn es noch nicht vorhanden ist
  if (!pokemons.value.some(p => p.pokedexNumber === newPokemon.pokedexNumber)) {
    pokemons.value.unshift(newPokemon); // Neues Pokémon am Anfang der Liste hinzufügen
  }
}
</script>

<style scoped>
.v-container {
  max-width: 1400px;
}

.search-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 2rem;
}

.filter-container {
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
}

/* Mobile Anpassungen */
@media (max-width: 600px) {
  .search-wrapper, .filter-container {
    padding: 0 8px;
  }
  
  .v-container {
    padding: 16px 6px;
  }
}

/* Tablet-Anpassungen */
@media (min-width: 601px) and (max-width: 960px) {
  .v-container {
    padding: 20px 12px;
    max-width: 95%;
  }

  .search-wrapper, .filter-container {
    max-width: 90%;
  }
}

/* Desktop-Anpassungen */
@media (min-width: 961px) {
  .v-container {
    padding: 24px 16px;
    max-width: 95%;
  }
  
  .search-wrapper, .filter-container {
    max-width: 70%;
  }
}

/* Große Desktop-Anpassungen */
@media (min-width: 1440px) {
  .search-wrapper, .filter-container {
    max-width: 60%;
  }
}
</style>
