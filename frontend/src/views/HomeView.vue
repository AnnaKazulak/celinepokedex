<template>
  <v-container class="py-5">
    <h3>Hallo Pokemones</h3>
    
    <!-- Search Field -->
    <v-row class="mb-5">
      <v-col cols="12" md="6">
        <v-text-field
          v-model="searchQuery"
          label="Pokémon suchen"
          prepend-inner-icon="mdi-magnify"
          variant="outlined"
          clearable
          @update:model-value="searchPokemons"
        ></v-text-field>
      </v-col>
    </v-row>
    
    <!-- No results message -->
    <v-row v-if="filteredPokemons.length === 0 && !isLoading">
      <v-col cols="12" class="text-center">
        <v-alert
          type="info"
          text="Es wurden keine Pokémon gefunden, die deiner Suche entsprechen."
          variant="tonal"
        ></v-alert>
      </v-col>
    </v-row>
    
    <!-- Pokemon grid -->
    <v-row v-else>
      <v-col
        v-for="pokemon in filteredPokemons"
        :key="pokemon.id"
        cols="12"
        sm="6"
        md="4"
      >
        <v-card>
          <v-img
            :src="pokemon.imageUrl"
            height="200px"
          ></v-img>
          <v-card-title>{{ pokemon.name }}</v-card-title>
          <v-card-subtitle>Pokedex-Nr: {{ pokemon.pokedexNumber }}</v-card-subtitle>
          <v-card-text>
            <div>{{ pokemon.description }}</div>
            <div>Typ: {{ pokemon.type1 }}<span v-if="pokemon.type2"> / {{ pokemon.type2 }}</span></div>
            <div>Größe: {{ pokemon.height }} m</div>
            <div>Gewicht: {{ pokemon.weight }} kg</div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
    
    <!-- Loading indicator -->
    <v-row v-if="isLoading">
      <v-col cols="12" class="text-center">
        <v-progress-circular
          indeterminate
          color="primary"
          size="64"
        ></v-progress-circular>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'

const pokemons = ref<any[]>([])
const searchQuery = ref('')
const isLoading = ref(false)

// Computed property to filter pokemons based on search query
const filteredPokemons = computed(() => {
  if (!searchQuery.value) {
    return pokemons.value
  }
  
  const query = searchQuery.value.toLowerCase()
  return pokemons.value.filter(pokemon => 
    pokemon.name.toLowerCase().includes(query) || 
    pokemon.pokedexNumber.includes(query)
  )
})

// Load all pokemons initially
onMounted(async () => {
  await loadAllPokemons()
})

// Function to load all pokemons
async function loadAllPokemons() {
  isLoading.value = true
  try {
    const response = await axios.get('http://localhost:8080/api/pokemons')
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
      const response = await axios.get('http://localhost:8080/api/pokemons/search', {
        params: {
          name: isNaN(Number(searchQuery.value)) ? searchQuery.value : null,
          pokedexNumber: !isNaN(Number(searchQuery.value)) ? searchQuery.value : null
        }
      })
      pokemons.value = response.data
    } catch (error) {
      console.error('Error searching pokemons:', error)
    } finally {
      isLoading.value = false
    }
  } else if (!searchQuery.value) {
    // If search is cleared, load all pokemons again
    await loadAllPokemons()
  }
}
</script>

<style scoped>
.v-container {
  max-width: 1200px;
}
</style>
