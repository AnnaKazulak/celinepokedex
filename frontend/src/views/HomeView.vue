<template>
  <v-container :class="['pt-24 pb-5 px-2', {'content-with-drawer': drawer}]">
    <!-- Header und Suchfeld -->
    <div class="search-container">
      <!-- Content Type Toggle as chip-group -->
      <div class="content-toggle-container mb-5">
        <v-chip-group
          v-model="contentType"
          mandatory
          selected-class="primary"
          class="filter-chips-group"
        >
          <v-chip
            value="all"
            filter
            variant="elevated"
            class="filter-chip ma-1"
          >
            <v-icon start size="small">mdi-view-grid</v-icon>
            Alle
          </v-chip>
          <v-chip
            value="pokemon"
            filter
            variant="elevated"
            class="filter-chip ma-1"
          >
            <v-icon start size="small">mdi-pokeball</v-icon>
            Pokémon
          </v-chip>
          <v-chip
            value="fantasy"
            filter
            variant="elevated"
            class="filter-chip ma-1"
          >
            <v-icon start size="small">mdi-magic-staff</v-icon>
            Fantasy
          </v-chip>
        </v-chip-group>
      </div>
  
      <!-- View Mode Toggle und Filter Button -->
      <div class="view-toggle-container mb-4">
        <v-chip-group
          v-model="viewMode"
          mandatory
          selected-class="secondary"
          class="filter-chips-group view-mode-chips"
        >
          <v-chip
            value="cards"
            filter
            variant="elevated"
            class="filter-chip ma-1"
          >
            <v-icon start size="small">mdi-view-grid-outline</v-icon>
            <span class="d-none d-sm-inline">Karten</span>
          </v-chip>
          <v-chip
            value="gallery"
            filter
            variant="elevated"
            class="filter-chip ma-1"
          >
            <v-icon start size="small">mdi-image-multiple-outline</v-icon>
            <span class="d-none d-sm-inline">Galerie</span>
          </v-chip>
        </v-chip-group>
        
        <!-- Filter Button zum Öffnen des Drawers -->
        <v-btn
          class="ml-2 filter-btn"
          :color="hasActiveFilters ? 'primary' : 'secondary'"
          :variant="hasActiveFilters ? 'elevated' : 'outlined'"
          @click="drawer = !drawer"
          elevation="2"
        >
          <v-icon start size="small">mdi-filter</v-icon>
          <span class="d-none d-sm-inline">Filter</span>
          <v-badge
            v-if="hasActiveFilters"
            color="info"
            content="!"
            location="top end"
            dot
            inline
            class="ml-1"
          ></v-badge>
        </v-btn>
      </div>
      
      <!-- Suchfeld-Komponente -->
      <SearchBar
        v-model:searchQuery="searchQuery"
        @search="searchContent"
      />
    </div>
    
    <!-- Drawer für Filter- und Sortieroptionen -->
    <FilterDrawer
      v-model:drawer="drawer"
      v-model:content-type="contentType"
      v-model:view-mode="viewMode"
      v-model:sort-option-index="sortOptionIndex"
      v-model:selected-types="selectedTypes"
      v-model:selected-element-types="selectedElementTypes"
      :pokemon-types="pokemonTypes"
      :sort-options="sortOptions"
      @filter-changed="handleFilterChanged"
      @reset-filters="resetFilters"
    />
    
    <!-- Keine Ergebnisse Nachricht -->
    <NoResultsMessage 
      v-if="filteredContent.length === 0 && !isLoading" 
      :content-type="contentType"
    />
    
    <!-- Cards Ansicht -->
    <v-row v-if="viewMode === 'cards'" class="justify-center content-grid">
      <!-- Pokémon/Fantasy Cards -->
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
    
    <!-- Galerie Ansicht -->
    <GalleryView v-else-if="viewMode === 'gallery'" :items="filteredContent" />
    
    <!-- Ladeindikator -->
    <LoadingIndicator v-if="isLoading" />
  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, onBeforeUnmount, watch, nextTick } from 'vue';
import axios from 'axios';
import { eventBus } from '../utils/eventBus';
import { PokemonType } from '../types/pokemon';
import type { Pokemon, FantasyCharacter } from '../types/pokemon';
import { API_ENDPOINTS, EXTERNAL_API } from '../utils/constants';
import SearchBar from '@/components/home/SearchBar.vue';
import NoResultsMessage from '@/components/home/NoResultsMessage.vue';
import LoadingIndicator from '@/components/home/LoadingIndicator.vue';
import PokemonCard from '@/components/pokemon/PokemonCard.vue';
import FantasyCharacterCard from '../components/fantasy/FantasyCharacterCard.vue';
import GalleryView from '@/components/home/GalleryView.vue';
import FilterDrawer from '@/components/home/FilterDrawer.vue';

// Content type toggle - 'all', 'pokemon', or 'fantasy'
const contentType = ref('all');

// State variables
const pokemons = ref<Pokemon[]>([]);
const fantasyCharacters = ref<FantasyCharacter[]>([]);
const searchQuery = ref('');
const isLoading = ref(false);
const selectedTypes = ref<PokemonType[]>([]);
const selectedElementTypes = ref<string[]>([]); // For filtering fantasy characters by element type
const viewMode = ref('cards'); // 'cards' or 'gallery'
const drawer = ref(false); // For the filter drawer

// Ensure each Pokemon has an imageUrl with Cloudinary optimizations
function ensureImageUrls(pokemonList: Pokemon[]): Pokemon[] {
  return pokemonList.map(pokemon => {
    if (!pokemon.imageUrl && pokemon.pokedexNumber) {
      // Extract the numeric ID from pokedexNumber (remove leading zeros)
      const numericId = parseInt(pokemon.pokedexNumber.replace(/^0+/, ''));
      // Use the external API's sprite URL as fallback
      pokemon.imageUrl = EXTERNAL_API.SPRITE_URL(numericId);
    } 
    // Add Cloudinary transformations if it's a Cloudinary URL
    else if (pokemon.imageUrl && pokemon.imageUrl.includes('cloudinary.com')) {
      pokemon.imageUrl = optimizeCloudinaryUrl(pokemon.imageUrl, 'pokemon');
    }
    return pokemon;
  });
}

// Helper function to optimize Cloudinary URLs for different content types
function optimizeCloudinaryUrl(url: string, contentType: 'pokemon' | 'fantasy' = 'pokemon'): string {
  if (!url || !url.includes('cloudinary.com')) return url;
  
  const parts = url.split('/upload/');
  if (parts.length !== 2) return url;
  
  let transformParams;
  
  if (contentType === 'pokemon') {
    // For Pokemon cards - optimized for card view
    transformParams = 'w_400,h_400,c_pad,q_auto,f_auto/';
  } else {
    // For Fantasy characters - keep the entire image visible without cropping
    transformParams = 'w_400,c_scale,q_auto,f_auto/';
  }
  
  return `${parts[0]}/upload/${transformParams}${parts[1]}`;
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


const sortOptionIndex = ref(0); // Defaultwert ist 0 (Name)

// Computed property für sortOption basierend auf dem sortOptionIndex
const sortOption = computed(() => {
  return sortOptions[sortOptionIndex.value].value;
});

// Computed property um zu prüfen, ob aktive Filter vorhanden sind
const hasActiveFilters = computed(() => {
  // Prüfe, ob Typ-Filter gesetzt sind
  if (selectedTypes.value.length > 0) {
    return true;
  }
  
  // Prüfe, ob Element-Typ-Filter gesetzt sind
  if (selectedElementTypes.value.length > 0) {
    return true;
  }
  
  // Prüfe, ob eine andere Sortierung als der Standard (Name) ausgewählt ist
  if (sortOptionIndex.value !== 0) {
    return true;
  }
  
  return false;
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

// Watch for content type changes to ensure filtering works on button clicks
watch(contentType, () => {
  if (contentType.value === 'all') {
    loadAllContent();
  } else if (contentType.value === 'pokemon') {
    loadAllPokemons();
  } else {
    loadAllFantasyCharacters();
  }
}, { flush: 'post' });

// Watch for view mode changes to ensure re-rendering
watch(viewMode, () => {
  // Use nextTick to ensure DOM updates
  nextTick(() => {
    eventBus.emit('view-mode-changed', viewMode.value);
  });
}, { flush: 'post' });

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
  } else {
    // Apply element type filter for Fantasy characters in fantasy-only mode
    if (selectedElementTypes.value.length > 0) {
      result = result.filter(item => {
        if (isFantasyCharacter(item)) {
          return item.elementType && selectedElementTypes.value.includes(item.elementType);
        }
        return false;
      });
    }
  }
  
  // For 'all' mode, apply both pokemon type filters and fantasy element type filters
  if (contentType.value === 'all') {
    if (selectedElementTypes.value.length > 0) {
      result = result.filter(item => {
        if (isPokemon(item)) {
          return selectedTypes.value.length === 0 || selectedTypes.value.some(type => 
            item.type1 === type || item.type2 === type
          );
        } else if (isFantasyCharacter(item)) {
          return item.elementType && selectedElementTypes.value.includes(item.elementType);
        }
        return false;
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

// Function to reset all filters
async function resetFilters() {
  selectedTypes.value = [];
  selectedElementTypes.value = [];
  sortOptionIndex.value = 0; // Zurücksetzen der Sortierung auf "Name"
  
  if (contentType.value === 'all') {
    await loadAllContent();
  } else if (contentType.value === 'pokemon') {
    await loadAllPokemons();
  } else {
    await loadAllFantasyCharacters();
  }
}

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
    
    // Apply Cloudinary optimizations to fantasy character images
    fantasyCharacters.value = response.data.map(character => {
      if (character.imageUrl && character.imageUrl.includes('cloudinary.com')) {
        character.imageUrl = optimizeCloudinaryUrl(character.imageUrl, 'fantasy');
      }
      return character;
    });
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

// Function to filter fantasy characters by element type
async function filterFantasyCharactersByElementType() {
  if (contentType.value === 'pokemon') return;
  
  isLoading.value = true;
  try {
    if (selectedElementTypes.value.length > 0) {
      // When element types are selected, use the API endpoint with elementType parameters
      const response = await axios.get<FantasyCharacter[]>(API_ENDPOINTS.FANTASY_CHARACTERS, {
        params: { 
          elementTypes: selectedElementTypes.value 
        },
        paramsSerializer: {
          indexes: null // null means no indexes, so ?elementTypes=FIRE&elementTypes=WATER
        }
      });
      
      // Apply Cloudinary optimizations to fantasy character images
      fantasyCharacters.value = response.data.map(character => {
        if (character.imageUrl && character.imageUrl.includes('cloudinary.com')) {
          character.imageUrl = optimizeCloudinaryUrl(character.imageUrl, 'fantasy');
        }
        return character;
      });
    } else {
      // If no element type is selected, load all fantasy characters
      await loadAllFantasyCharacters();
    }
  } catch (error) {
    console.error('Error filtering by element type:', error);
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
            query: searchQuery.value,
            elementTypes: selectedElementTypes.value.length > 0 ? selectedElementTypes.value : null
          },
          paramsSerializer: {
            indexes: null
          }
        });
        fantasyCharacters.value = fantasyResponse.data.map(character => {
          if (character.imageUrl && character.imageUrl.includes('cloudinary.com')) {
            character.imageUrl = optimizeCloudinaryUrl(character.imageUrl, 'fantasy');
          }
          return character;
        });
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
      if (selectedElementTypes.value.length > 0) {
        await filterFantasyCharactersByElementType();
      } else {
        await loadAllFantasyCharacters();
      }
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

// Handler for filter changed event from FilterDrawer
async function handleFilterChanged() {
  if (contentType.value === 'pokemon') {
    await filterPokemonsByType();
  } else if (contentType.value === 'fantasy') {
    await filterFantasyCharactersByElementType();
  } else {
    // For 'all' content type, run both filters
    await Promise.all([
      filterPokemonsByType(),
      filterFantasyCharactersByElementType()
    ]);
  }
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
  width: 100%;
  max-width: 400px;
}

/* Filter chip styles to match FilterDrawer */
.filter-chips-group {
  width: 100%;
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
}

.filter-chip {
  min-width: 80px;
  margin: 4px;
  flex-grow: 1;
  text-align: center;
  justify-content: center;
  font-weight: 500;
  transition: all 0.2s ease;
}

.filter-chip:hover {
  transform: translateY(-2px);
}

/* View mode toggle container */
.view-toggle-container {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  max-width: 400px;
}

.view-mode-chips {
  max-width: 240px;
}

.filter-btn {
  height: 40px;
  transition: all 0.3s ease;
  font-weight: 500;
}

.filter-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.content-grid {
  margin: 0 -4px;
}

.content-col {
  padding: 8px 4px;
}

/* Mobile Anpassungen */
@media (max-width: 600px) {
  .search-wrapper {
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
  
  .filter-chip {
    min-width: 70px;
    font-size: 0.85rem;
  }

  .view-mode-chips {
    max-width: 180px;
  }
}

/* Tablet-Anpassungen */
@media (min-width: 601px) and (max-width: 960px) {
  .v-container {
    padding: 20px 12px;
    max-width: 95%;
  }

  .search-wrapper {
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
  
  .search-wrapper {
    max-width: 70%;
  }
  
  .content-col {
    padding: 10px 8px;
  }
  
  .filter-chip {
    min-width: 100px;
  }
}

/* Große Desktop-Anpassungen */
@media (min-width: 1440px) {
  .search-wrapper {
    max-width: 60%;
  }
  
  .content-col {
    padding: 12px 10px;
  }
}
</style>
