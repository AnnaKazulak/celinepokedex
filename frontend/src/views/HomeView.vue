<template>
  <v-container class="py-5">
    <h3>Hallo Pokemones</h3>
    <v-row>
      <v-col
        v-for="pokemon in pokemons"
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
  </v-container>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import axios from 'axios'

const pokemons = ref<any[]>([])

onMounted(async () => {
  const response = await axios.get('http://localhost:8080/api/pokemons')
  pokemons.value = response.data
})
</script>

<style scoped>
.v-container {
  max-width: 1200px;
}
</style>
