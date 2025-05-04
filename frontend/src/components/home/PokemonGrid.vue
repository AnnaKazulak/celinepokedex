<template>
  <v-row class="justify-center pokemon-grid">
    <v-col
      v-for="pokemon in pokemons"
      :key="pokemon.id"
      cols="12"
      sm="6"
      md="4"
      lg="3"
      xl="2"
      class="d-flex justify-center pokemon-col"
    >
      <PokemonCard :pokemon="pokemon" />
    </v-col>
  </v-row>
</template>

<script setup lang="ts">
import { Pokemon } from '@/types/pokemon';
import PokemonCard from '@/components/PokemonCard.vue';

defineProps<{
  pokemons: Pokemon[];
}>();
</script>

<style scoped>
.pokemon-grid {
  margin: 0 -4px;
}

.pokemon-col {
  padding: 8px 4px;
}

/* Mobile Anpassungen */
@media (max-width: 600px) {
  .pokemon-col {
    padding: 6px 3px;
  }
  
  .pokemon-grid {
    margin: 0 -3px;
  }
}

/* Tablet-Anpassungen */
@media (min-width: 601px) and (max-width: 960px) {
  .pokemon-col {
    padding: 8px 6px;
  }
}

/* Desktop-Anpassungen */
@media (min-width: 961px) {
  .pokemon-col {
    padding: 10px 8px;
  }
}

/* Große Desktop-Anpassungen */
@media (min-width: 1440px) {
  .pokemon-grid {
    margin: 0 -6px;
  }
  
  .pokemon-col {
    padding: 12px 10px;
  }
}
</style>