<template>
  <div class="details-section">
    <div class="details-container">
      <div class="detail-item">
        <v-icon size="small" :color="dominantColor || 'rgba(0,0,0,0.5)'">mdi-ruler</v-icon>
        <span class="detail-label">Größe:</span>
        <span class="detail-value">{{ pokemon.height }} m</span>
      </div>

      <div class="detail-item">
        <v-icon size="small" :color="dominantColor || 'rgba(0,0,0,0.5)'">mdi-weight</v-icon>
        <span class="detail-label">Gewicht:</span>
        <span class="detail-value">{{ pokemon.weight }} kg</span>
      </div>

      <div class="detail-item">
        <v-icon size="small" :color="dominantColor || 'rgba(0,0,0,0.5)'">mdi-shape</v-icon>
        <span class="detail-label">Kategorie:</span>
        <span class="detail-value">{{ pokemon.category || 'Unbekannt' }}</span>
      </div>

      <div class="detail-item">
        <v-icon size="small" :color="dominantColor || 'rgba(0,0,0,0.5)'">mdi-star</v-icon>
        <span class="detail-label">Fähigkeit:</span>
        <span class="detail-value">{{ pokemon.ability || 'Unbekannt' }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { type Pokemon } from '@/types/pokemon';

// Props
const props = defineProps<{
  pokemon: Pokemon;
  dominantColor: string;
}>();
</script>

<style scoped>
.details-section {
  max-width: 1400px;
  margin: 2rem auto;
}

.details-container {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  gap: 1rem;
  padding: 1rem;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  transition: all 0.3s ease;
}

.detail-item:hover {
  background-color: rgba(0, 0, 0, 0.05);
}

.detail-label {
  font-weight: bold;
}

.detail-value {
  color: rgba(0, 0, 0, 0.7);
}

@media (max-width: 600px) {
  .details-container {
    flex-direction: column;
  }
}
</style>