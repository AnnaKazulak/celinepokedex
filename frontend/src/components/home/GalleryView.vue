<template>
  <div class="gallery-container">
    <v-row>
      <template v-for="item in items" :key="getItemKey(item)">
        <v-col
          cols="6"
          sm="4"
          md="3"
          lg="2"
          xl="1"
        >
          <v-card
            class="gallery-card"
            @click="openDetailView(item)"
            elevation="2"
          >
            <v-img
              :src="getItemImageUrl(item)"
              class="gallery-image"
              height="200"
              cover
              :alt="getItemName(item)"
            >
              <template v-slot:placeholder>
                <v-row
                  class="fill-height ma-0"
                  align="center"
                  justify="center"
                >
                  <v-progress-circular
                    indeterminate
                    color="grey-lighten-5"
                  ></v-progress-circular>
                </v-row>
              </template>
            </v-img>
            <div class="gallery-overlay d-flex flex-column justify-end">
              <div class="gallery-name pa-2">
                {{ getItemName(item) }}
                <div class="gallery-subtitle" v-if="isPokemon(item)">
                  #{{ item.pokedexNumber }}
                </div>
              </div>
            </div>
          </v-card>
        </v-col>
      </template>
    </v-row>
  </div>
</template>

<script setup lang="ts">
import { Pokemon, FantasyCharacter } from '@/types/pokemon';
import { useRouter } from 'vue-router';

const props = defineProps<{
  items: Array<Pokemon | FantasyCharacter>;
}>();

const router = useRouter();

// Helper functions to handle both Pokemon and FantasyCharacter types
function isPokemon(item: any): item is Pokemon {
  return 'pokedexNumber' in item;
}

function getItemKey(item: Pokemon | FantasyCharacter): string | number {
  if (isPokemon(item)) {
    return item.pokedexNumber;
  } else {
    return item.id || Math.random().toString(36).substring(2, 11);
  }
}

function getItemImageUrl(item: Pokemon | FantasyCharacter): string {
  if (isPokemon(item)) {
    return item.imageUrl || '';
  } else {
    return item.imageUrl || '';
  }
}

function getItemName(item: Pokemon | FantasyCharacter): string {
  if (isPokemon(item)) {
    return item.name;
  } else {
    return item.name || 'Fantasy Character';
  }
}

function openDetailView(item: Pokemon | FantasyCharacter) {
  if (isPokemon(item)) {
    router.push({ name: 'pokemonDetail', params: { id: item.pokedexNumber } });
  } else {
    router.push({ name: 'fantasyCharacterDetail', params: { id: item.id } });
  }
}
</script>

<style scoped>
.gallery-container {
  padding: 8px;
}

.gallery-card {
  position: relative;
  overflow: hidden;
  transition: transform 0.3s ease;
  height: 200px;
}

.gallery-card:hover {
  transform: scale(1.05);
}

.gallery-image {
  transition: transform 0.5s ease;
}

.gallery-card:hover .gallery-image {
  transform: scale(1.1);
}

.gallery-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(to top, rgba(0,0,0,0.7) 0%, transparent 100%);
  color: white;
  height: 100%;
  pointer-events: none;
}

.gallery-name {
  font-weight: bold;
  font-size: 1rem;
  text-shadow: 1px 1px 2px rgba(0,0,0,0.8);
}

.gallery-subtitle {
  font-size: 0.8rem;
  opacity: 0.8;
}

/* Mobile Anpassungen */
@media (max-width: 600px) {
  .gallery-card {
    height: 150px;
  }
  
  .gallery-name {
    font-size: 0.8rem;
  }
  
  .gallery-subtitle {
    font-size: 0.7rem;
  }
}
</style>