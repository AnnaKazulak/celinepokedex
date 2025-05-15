<template>
  <div class="gallery-container">
    <div class="masonry-gallery">
      <div 
        v-for="(item, index) in items" 
        :key="getItemKey(item)"
        class="masonry-item"
        :class="getImageSizeClass(item, index)"
        @click="openDetailView(item)"
      >
        <v-img
          :src="getItemImageUrl(item)"
          :aspect-ratio="getAspectRatio(item, index)"
          cover
          class="gallery-image rounded-lg"
          :lazy-src="getPlaceholderUrl(item)"
          :transition="'fade-transition'"
          loading="lazy"
        >
          <template v-slot:placeholder>
            <div class="d-flex align-center justify-center fill-height">
              <v-progress-circular
                indeterminate
                color="grey-lighten-5"
              ></v-progress-circular>
            </div>
          </template>
          <div class="gallery-overlay d-flex flex-column justify-end">
            <div class="gallery-name pa-2">
              {{ getItemName(item) }}
              <div class="gallery-subtitle" v-if="isPokemon(item)">
                #{{ item.pokedexNumber }}
              </div>
            </div>
          </div>
        </v-img>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Pokemon, FantasyCharacter } from '@/types/pokemon';
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

// Generate low-resolution placeholder for lazy loading
function getPlaceholderUrl(item: Pokemon | FantasyCharacter): string {
  let imageUrl = '';
  
  if (isPokemon(item)) {
    imageUrl = item.imageUrl || '';
  } else {
    imageUrl = item.imageUrl || '';
  }
  
  // Add tiny placeholder transformation for Cloudinary images
  if (imageUrl && imageUrl.includes('cloudinary.com')) {
    const parts = imageUrl.split('/upload/');
    if (parts.length === 2) {
      // Create a tiny, blurred placeholder (10px wide, low quality)
      const placeholderParams = 'w_10,h_10,q_10,e_blur:1000/';
      imageUrl = `${parts[0]}/upload/${placeholderParams}${parts[1]}`;
    }
  }
  
  return imageUrl;
}

function getItemImageUrl(item: Pokemon | FantasyCharacter): string {
  let imageUrl = '';
  
  if (isPokemon(item)) {
    imageUrl = item.imageUrl || '';
  } else {
    imageUrl = item.imageUrl || '';
  }
  
  // Add Cloudinary transformations if the URL contains cloudinary
  if (imageUrl && imageUrl.includes('cloudinary.com')) {
    // Get size class to determine appropriate dimensions
    const sizeClass = getImageSizeClass(item, typeof item.id === 'number' ? item.id : 0);
    
    // Extract the base URL and path
    const parts = imageUrl.split('/upload/');
    if (parts.length === 2) {
      // Define transformation parameters based on size class
      let transformParams = '';
      
      switch (sizeClass) {
        case 'wide-horizontal':
          transformParams = 'c_fill,w_600,h_338,q_auto,f_auto/'; // 16:9 ratio
          break;
        case 'vertical-tall':
          transformParams = 'c_fill,w_400,h_533,q_auto,f_auto/'; // 3:4 ratio
          break;
        case 'vertical-medium':
          transformParams = 'c_fill,w_400,h_600,q_auto,f_auto/'; // 2:3 ratio
          break;
        case 'square-large':
          transformParams = 'c_fill,w_500,h_500,q_auto,f_auto/'; // 1:1 ratio
          break;
        case 'square-medium':
          transformParams = 'c_fill,w_400,h_400,q_auto,f_auto/'; // 1:1 ratio
          break;
        case 'square-small':
          transformParams = 'c_fill,w_300,h_300,q_auto,f_auto/'; // 1:1 ratio
          break;
        default:
          transformParams = 'c_fill,w_400,h_400,q_auto,f_auto/'; // Default square
      }
      
      // Reconstruct URL with transformations
      imageUrl = `${parts[0]}/upload/${transformParams}${parts[1]}`;
    }
  }
  
  return imageUrl;
}

function getItemName(item: Pokemon | FantasyCharacter): string {
  if (isPokemon(item)) {
    return item.name;
  } else {
    return item.name || 'Fantasy Character';
  }
}

function getImageSizeClass(item: Pokemon | FantasyCharacter, index: number): string {
  // For Pokémon, use a pattern based on pokedex number
  if (isPokemon(item)) {
    const pokedexNumber = typeof item.pokedexNumber === 'number' ? 
      item.pokedexNumber : parseInt(item.pokedexNumber.toString(), 10);
    
    const pattern = pokedexNumber % 12;
    if (pattern === 0 || pattern === 7) {
      return 'wide-horizontal';
    } else if (pattern === 3 || pattern === 9) {
      return 'square-large';
    } else if (pattern === 5) {
      return 'vertical-tall';
    }
  }
  
  // For fantasy characters or as fallback
  const pattern = index % 12;
  if (pattern === 0 || pattern === 7) {
    return 'wide-horizontal';
  } else if (pattern === 3 || pattern === 9) {
    return 'square-large';
  } else if (pattern === 5) {
    return 'vertical-tall';
  } else if (pattern === 1 || pattern === 8 || pattern === 11) {
    return 'vertical-medium';
  } else if (pattern === 6) {
    return 'square-small';
  } else {
    return 'square-medium';
  }
}

function getAspectRatio(item: Pokemon | FantasyCharacter, index: number): number {
  const sizeClass = getImageSizeClass(item, index);
  
  switch (sizeClass) {
    case 'wide-horizontal': return 16/9;
    case 'vertical-tall': return 3/4;
    case 'vertical-medium': return 2/3;
    case 'square-large':
    case 'square-medium':
    case 'square-small':
    default: return 1;
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
  width: 100%;
  padding: 2px;
}

.masonry-gallery {
  column-count: 2;
  column-gap: 2px;
}

/* Responsive column adjustments */
@media (min-width: 600px) {
  .masonry-gallery {
    column-count: 3;
  }
}

@media (min-width: 960px) {
  .masonry-gallery {
    column-count: 4;
  }
}

@media (min-width: 1264px) {
  .masonry-gallery {
    column-count: 5;
  }
}

@media (min-width: 1904px) {
  .masonry-gallery {
    column-count: 6;
  }
}

.masonry-item {
  position: relative;
  cursor: pointer;
  display: inline-block;
  width: 100%;
  margin-bottom: 2px;
  break-inside: avoid;
  overflow: hidden;
}

.wide-horizontal {
  width: 100%;
}

.vertical-tall {
  width: 100%;
}

.vertical-medium {
  width: 100%;
}

.square-large {
  width: 100%;
}

.square-medium {
  width: 100%;
}

.square-small {
  width: 100%;
}

.gallery-image {
  display: block;
  width: 100%;
  height: auto;
  transition: transform 0.3s ease;
}

.masonry-item:hover .gallery-image {
  transform: scale(1.03);
}

.gallery-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(to top, rgba(0,0,0,0.8) 0%, rgba(0,0,0,0.3) 50%, transparent 100%);
  color: white;
  height: 100%;
  pointer-events: none;
}

.gallery-name {
  font-weight: bold;
  font-size: 0.9rem;
  text-shadow: 1px 1px 2px rgba(0,0,0,0.8);
}

.gallery-subtitle {
  font-size: 0.75rem;
  opacity: 0.8;
}

@media (max-width: 600px) {
  .gallery-container {
    padding: 1px;
  }
  
  .masonry-gallery {
    column-gap: 1px;
  }
  
  .masonry-item {
    margin-bottom: 1px;
  }

  .gallery-name {
    font-size: 0.85rem;
  }
  
  .gallery-subtitle {
    font-size: 0.7rem;
  }
}

/* Add fade transition for lazy loading */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.5s ease;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>