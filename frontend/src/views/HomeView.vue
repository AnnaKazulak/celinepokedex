<template>
  <v-container class="pt-24 pb-5 px-2">
    <!-- Header und Suchfeld -->
    <div class="search-container">
      <!-- Content Type Toggle -->
      <div class="content-toggle-container mb-5">
        <v-btn-toggle
          v-model="contentType"
          color="primary"
          rounded="pill"
          mandatory
        >
          <v-btn value="all" :ripple="false" class="toggle-btn">
            <v-icon start>mdi-view-grid</v-icon>
            Alle
          </v-btn>
          <v-btn value="pokemon" :ripple="false" class="toggle-btn">
            <v-icon start>mdi-pokeball</v-icon>
            Pokémon
          </v-btn>
          <v-btn value="fantasy" :ripple="false" class="toggle-btn">
            <v-icon start>mdi-magic-staff</v-icon>
            Fantasy
          </v-btn>
        </v-btn-toggle>
      </div>
      
      <!-- Fantasy Character Generator button -->
      <div v-if="contentType === 'fantasy' || contentType === 'all'" class="mb-4">
        <FantasyCharacterGenerator @character-created="handleNewFantasyCharacter" />
      </div>
      
      <!-- Suchfeld-Komponente -->
      <SearchBar
        v-model:searchQuery="searchQuery"
        @search="searchContent"
      />
      
      <!-- Filter- und Sortieroptionen -->
      <div class="filter-container">
        <!-- Sortierung-Komponente -->
        <SortToggle
          :sort-options="sortOptions"
          v-model:sortOptionIndex="sortOptionIndex"
        />
        
        <!-- Typ-Filter-Komponente, nur für Pokemon-Ansicht -->
        <TypeFilter
          v-if="contentType !== 'fantasy'"
          v-model:selectedTypes="selectedTypes"
          :pokemon-types="pokemonTypes"
          @filterChanged="filterPokemonsByType"
        />
      </div>
    </div>
    
    <!-- Keine Ergebnisse Nachricht -->
    <NoResultsMessage 
      v-if="filteredContent.length === 0 && !isLoading" 
      :content-type="contentType"
    />
    
    <v-row class="justify-center content-grid">
      <!-- Pokémon Cards -->
      <template v-for="item in filteredContent" :key="getItemKey(item)">
        <v-col
          cols="12"
          sm="6"
          md="4"
          lg="3"
          xl="2"
          class="d-flex justify-center content-col"
        >
          <!-- Pokemon Card -->
          <PokemonCard v-if="isPokemon(item)" :pokemon="item" />
          
          <!-- Fantasy Character Card -->
          <FantasyCharacterCard v-else :character="item" />
        </v-col>
      </template>
    </v-row>
    
    <!-- Ladeindikator -->
    <LoadingIndicator v-if="isLoading" />
  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, onBeforeUnmount, watch } from 'vue';
import axios from 'axios';
import { eventBus } from '../utils/eventBus';
import { Pokemon, PokemonType, FantasyCharacter } from '../types/pokemon';
import { API_ENDPOINTS, EXTERNAL_API } from '../utils/constants';

// Importiere die Unterkomponenten
import SearchBar from '@/components/home/SearchBar.vue';
import TypeFilter from '@/components/home/TypeFilter.vue';
import SortToggle from '@/components/home/SortToggle.vue';
import NoResultsMessage from '@/components/home/NoResultsMessage.vue';
import LoadingIndicator from '@/components/home/LoadingIndicator.vue';
import PokemonCard from '@/components/PokemonCard.vue';
import FantasyCharacterCard from '@/components/FantasyCharacterCard.vue';
import FantasyCharacterGenerator from '@/components/FantasyCharacterGenerator.vue';

// Content type toggle - 'all', 'pokemon', or 'fantasy'
const contentType = ref('all');

// State variables
const pokemons = ref<Pokemon[]>([]);
const fantasyCharacters = ref<FantasyCharacter[]>([]);
const searchQuery = ref('');
const isLoading = ref(false);
const selectedTypes = ref<PokemonType[]>([]);

// Ensure each Pokemon has an imageUrl
function ensureImageUrls(pokemonList: Pokemon[]): Pokemon[] {
  return pokemonList.map(pokemon => {
    if (!pokemon.imageUrl && pokemon.pokedexNumber) {
      // Extract the numeric ID from pokedexNumber (remove leading zeros)
      const numericId = parseInt(pokemon.pokedexNumber.replace(/^0+/, ''));
      // Use the external API's sprite URL as fallback
      pokemon.imageUrl = EXTERNAL_API.SPRITE_URL(numericId);
    }
    return pokemon;
  });
}

// Erstelle eine Liste aller Pokémon-Typen für das Dropdown
const pokemonTypes = Object.values(PokemonType).map(type => ({
  label: type, // Wir nutzen den Enum-Wert selbst als Label
  value: type
}));

// Sortieroptionen
const sortOptions = [
  { text: 'Name', value: 'name' },
  { text: 'Neueste', value: 'newestFirst' },
  { text: 'Älteste', value: 'oldestFirst' }
];

// Bei Fantasy-Ansicht andere Sortieroptionen anzeigen
const fantasyFilterVisible = computed(() => contentType.value === 'fantasy');

const sortOptionIndex = ref(0); // Defaultwert ist 0 (Name)

// Computed property für sortOption basierend auf dem sortOptionIndex
const sortOption = computed(() => {
  return sortOptions[sortOptionIndex.value].value;
});

// Type-checking helpers
function isPokemon(item: any): item is Pokemon {
  return 'pokedexNumber' in item;
}

function isFantasyCharacter(item: any): item is FantasyCharacter {
  return 'prompt' in item && !('pokedexNumber' in item);
}

function getItemKey(item: Pokemon | FantasyCharacter): string | number {
  if (isPokemon(item)) {
    return item.pokedexNumber;
  } else {
    return item.id || Math.random().toString(36).substring(2, 11);
  }
}

// Watch for content type changes
watch(contentType, () => {
  if (contentType.value === 'all') {
    loadAllContent();
  } else if (contentType.value === 'pokemon') {
    loadAllPokemons();
  } else {
    loadAllFantasyCharacters();
  }
});

// Computed property to show combined and filtered content
const filteredContent = computed(() => {
  // First determine base content based on content type
  let baseContent: Array<Pokemon | FantasyCharacter> = [];
  
  if (contentType.value === 'all') {
    baseContent = [...pokemons.value, ...fantasyCharacters.value];
  } else if (contentType.value === 'pokemon') {
    baseContent = [...pokemons.value];
  } else {
    baseContent = [...fantasyCharacters.value];
  }
  
  // Then apply Pokemon-specific filters if not in fantasy-only mode
  let result = baseContent;
  
  if (contentType.value !== 'fantasy') {
    // Apply type filter for Pokemon items only
    if (selectedTypes.value.length > 0) {
      result = result.filter(item => {
        if (isPokemon(item)) {
          return selectedTypes.value.some(type => 
            item.type1 === type || item.type2 === type
          );
        }
        return contentType.value === 'all'; // Keep fantasy characters in 'all' mode
      });
    }
  }
  
  // Apply search filter
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase();
    result = result.filter(item => {
      if (isPokemon(item)) {
        return item.name.toLowerCase().includes(query) || 
               item.pokedexNumber.includes(query);
      } else {
        // Suche sowohl im Namen als auch in der Beschreibung (prompt)
        return (item.name && item.name.toLowerCase().includes(query)) || 
               item.prompt.toLowerCase().includes(query);
      }
    });
  }
  
  // Apply sorting
  if (sortOption.value === 'name') {
    result.sort((a, b) => {
      // For Pokemon, sort by name
      if (isPokemon(a) && isPokemon(b)) {
        return a.name.localeCompare(b.name);
      } 
      // For Fantasy Characters, sort by prompt (description)
      else if (isFantasyCharacter(a) && isFantasyCharacter(b)) {
        return a.prompt.localeCompare(b.prompt);
      }
      // When mixing, Pokemon come before Fantasy Characters
      else if (isPokemon(a)) {
        return -1;
      } else {
        return 1;
      }
    });
  } else if (sortOption.value === 'newestFirst' || sortOption.value === 'oldestFirst') {
    result.sort((a, b) => {
      const dateA = a.createdAt ? new Date(a.createdAt).getTime() : 0;
      const dateB = b.createdAt ? new Date(b.createdAt).getTime() : 0;
      return sortOption.value === 'newestFirst' ? dateB - dateA : dateA - dateB;
    });
  }
  
  return result;
});

// Load all content (both Pokemon and Fantasy Characters)
async function loadAllContent() {
  isLoading.value = true;
  
  try {
    // Load pokemons and fantasy characters in parallel
    await Promise.all([
      loadAllPokemons(false), // false means don't update isLoading by itself
      loadAllFantasyCharacters(false) // false means don't update isLoading by itself
    ]);
  } catch (error) {
    console.error('Error loading content:', error);
  } finally {
    isLoading.value = false;
  }
}

// Function to load all pokemons
async function loadAllPokemons(updateLoadingState = true) {
  if (updateLoadingState) isLoading.value = true;
  
  try {
    const response = await axios.get<Pokemon[]>(API_ENDPOINTS.POKEMONS);
    pokemons.value = ensureImageUrls(response.data);
  } catch (error) {
    console.error('Error loading pokemons:', error);
  } finally {
    if (updateLoadingState) isLoading.value = false;
  }
}

// Function to load all fantasy characters
async function loadAllFantasyCharacters(updateLoadingState = true) {
  if (updateLoadingState) isLoading.value = true;
  
  try {
    const response = await axios.get<FantasyCharacter[]>(API_ENDPOINTS.FANTASY_CHARACTERS);
    fantasyCharacters.value = response.data;
  } catch (error) {
    console.error('Error loading fantasy characters:', error);
  } finally {
    if (updateLoadingState) isLoading.value = false;
  }
}

// Function to filter pokemons by type
async function filterPokemonsByType() {
  if (contentType.value === 'fantasy') return;
  
  isLoading.value = true;
  try {
    if (selectedTypes.value.length > 0) {
      // When types are selected, use the API endpoint with type parameters
      const response = await axios.get<Pokemon[]>(API_ENDPOINTS.POKEMONS, {
        params: { 
          types: selectedTypes.value 
        },
        paramsSerializer: {
          indexes: null // null means no indexes, so ?types=FEUER&types=WASSER
        }
      });
      pokemons.value = ensureImageUrls(response.data);
    } else {
      // If no type is selected, load all pokemons
      await loadAllPokemons();
    }
  } catch (error) {
    console.error('Error filtering by type:', error);
  } finally {
    isLoading.value = false;
  }
}

// Function to search content using the API
async function searchContent() {
  // Only use the API search if the query is at least 2 characters
  if (searchQuery.value && searchQuery.value.length >= 2) {
    isLoading.value = true;
    
    try {
      // If in Pokemon or All mode, search for Pokemon
      if (contentType.value !== 'fantasy') {
        const pokemonResponse = await axios.get<Pokemon[]>('http://localhost:8080/api/pokemons/search', {
          params: {
            name: isNaN(Number(searchQuery.value)) ? searchQuery.value : null,
            pokedexNumber: !isNaN(Number(searchQuery.value)) ? searchQuery.value : null,
            types: selectedTypes.value.length > 0 ? selectedTypes.value : null
          },
          paramsSerializer: {
            indexes: null
          }
        });
        pokemons.value = ensureImageUrls(pokemonResponse.data);
      }
      
      // If in Fantasy or All mode, search for Fantasy Characters
      if (contentType.value !== 'pokemon') {
        const fantasyResponse = await axios.get<FantasyCharacter[]>(`${API_ENDPOINTS.FANTASY_CHARACTERS}/search`, {
          params: {
            query: searchQuery.value
          }
        });
        fantasyCharacters.value = fantasyResponse.data;
      }
    } catch (error) {
      console.error('Error searching content:', error);
    } finally {
      isLoading.value = false;
    }
  } else if (!searchQuery.value) {
    // If search is cleared, load all content again based on current mode
    if (contentType.value === 'all') {
      await loadAllContent();
    } else if (contentType.value === 'pokemon') {
      if (selectedTypes.value.length > 0) {
        await filterPokemonsByType();
      } else {
        await loadAllPokemons();
      }
    } else {
      await loadAllFantasyCharacters();
    }
  }
}

// Event handlers for new content creation
function handleNewPokemon(newPokemon: Pokemon) {
  // Add the new pokemon to the list if it doesn't exist already
  if (!pokemons.value.some(p => p.pokedexNumber === newPokemon.pokedexNumber)) {
    pokemons.value.unshift(newPokemon); // Add new pokemon at the beginning of the list
  }
}

function handleNewFantasyCharacter(newCharacter: FantasyCharacter) {
  // Add the new character to the list
  fantasyCharacters.value.unshift(newCharacter);
}

function handleFantasyCharacterDeleted(characterId: number) {
  // Entferne den gelöschten Character aus der Liste
  fantasyCharacters.value = fantasyCharacters.value.filter(
    character => character.id !== characterId
  );
}

// Register event listeners
onMounted(async () => {
  // Initial content loading
  loadAllContent();
  
  // Register event listeners
  eventBus.on('pokemon-created', handleNewPokemon);
  eventBus.on('fantasy-character-created', handleNewFantasyCharacter);
  eventBus.on('fantasy-character-deleted', handleFantasyCharacterDeleted);

  // A series of events with increasing delays to ensure navbar updates
  // This makes navigation back to the home page reliable
  setTimeout(() => {
    eventBus.emit('home-view-mounted');
  }, 100);
  
  // After content has likely loaded, trigger navbar reset and card reregistration
  setTimeout(() => {
    eventBus.emit('force-navbar-reset');
    // Tell all cards to reregister
    eventBus.emit('reregister-all-cards');
  }, 500);
});

// Clean up event listeners
onBeforeUnmount(() => {
  eventBus.off('pokemon-created', handleNewPokemon);
  eventBus.off('fantasy-character-created', handleNewFantasyCharacter);
  eventBus.off('fantasy-character-deleted', handleFantasyCharacterDeleted);
});
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

.content-toggle-container {
  display: flex;
  justify-content: center;
}

.toggle-btn {
  min-width: 100px;
}

.filter-container {
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
}

.content-grid {
  margin: 0 -4px;
}

.content-col {
  padding: 8px 4px;
}

/* Mobile Anpassungen */
@media (max-width: 600px) {
  .search-wrapper, .filter-container {
    padding: 0 8px;
  }
  
  .v-container {
    padding: 16px 6px;
  }
  
  .content-col {
    padding: 6px 3px;
  }
  
  .content-grid {
    margin: 0 -3px;
  }
  
  .toggle-btn {
    min-width: 80px;
    font-size: 0.85rem;
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
  
  .content-col {
    padding: 8px 6px;
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
  
  .content-col {
    padding: 10px 8px;
  }
}

/* Große Desktop-Anpassungen */
@media (min-width: 1440px) {
  .search-wrapper, .filter-container {
    max-width: 60%;
  }
  
  .content-col {
    padding: 12px 10px;
  }
}
</style>
