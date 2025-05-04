<template>
  <div class="search-wrapper">
    <v-text-field
      v-model="localSearchQuery"
      label="Pokémon suchen..."
      prepend-inner-icon="mdi-magnify"
      variant="solo"
      density="comfortable"
      hide-details
      clearable
      rounded
      class="search-field"
      @update:model-value="onSearch"
    ></v-text-field>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';

const props = defineProps<{
  searchQuery: string;
}>();

const emit = defineEmits<{
  (e: 'update:searchQuery', value: string): void;
  (e: 'search', value: string): void;
}>();

// Lokale Kopie des Suchbegriffs für v-model
const localSearchQuery = ref(props.searchQuery);

// Watch für Prop-Änderungen
watch(() => props.searchQuery, (newValue) => {
  localSearchQuery.value = newValue;
});

// Bei Änderung des Suchbegriffs das Event emittieren
function onSearch(value: string) {
  emit('update:searchQuery', value);
  emit('search', value);
}
</script>

<style scoped>
.search-wrapper {
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
  margin-bottom: 1rem;
}

.search-field {
  border-radius: 28px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(162, 125, 250, 0.15);
}

.search-field:deep(.v-field__input) {
  padding: 12px 16px;
}

.search-field:deep(.v-field__prepend-inner) {
  padding-left: 8px;
}

.search-field:deep(.v-field__append-inner) {
  padding-right: 8px;
}

.search-field:deep(.v-field__outline) {
  opacity: 0;
}

.search-field:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(162, 125, 250, 0.25);
}

.search-field:focus-within {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(162, 125, 250, 0.3);
}

/* Mobile Anpassungen */
@media (max-width: 600px) {
  .search-field {
    border-radius: 20px;
  }
  
  .search-field:deep(.v-field__input) {
    padding: 8px 12px;
  }
}
</style>