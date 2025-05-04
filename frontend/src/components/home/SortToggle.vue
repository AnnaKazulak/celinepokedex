<template>
  <div class="sorting-container mb-4">
    <div class="sort-chip-wrapper">
      <v-chip
        v-for="(option, index) in sortOptions"
        :key="index"
        :color="localSortOptionIndex === index ? '#705898' : undefined"
        :text-color="localSortOptionIndex === index ? 'white' : undefined"
        :variant="localSortOptionIndex === index ? 'elevated' : 'tonal'"
        :size="isDesktop ? 'default' : 'small'"
        class="sort-chip"
        @click="localSortOptionIndex = index"
      >
        <v-icon v-if="option.icon && isDesktop" start size="small" class="mr-1">{{ option.icon }}</v-icon>
        {{ option.text }}
      </v-chip>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, computed, onMounted, onBeforeUnmount } from 'vue';

interface SortOption {
  text: string;
  value: string;
  icon?: string;
}

const props = defineProps<{
  sortOptions: SortOption[];
  sortOptionIndex: number;
}>();

const emit = defineEmits<{
  (e: 'update:sortOptionIndex', value: number): void;
}>();

// Lokale Kopie des sortOptionIndex für v-model
const localSortOptionIndex = ref(props.sortOptionIndex);
const windowWidth = ref(window.innerWidth);
const isDesktop = computed(() => windowWidth.value > 768);

// Watch für Prop-Änderungen
watch(() => props.sortOptionIndex, (newValue) => {
  localSortOptionIndex.value = newValue;
});

// Watch für lokale Änderungen
watch(localSortOptionIndex, (newValue) => {
  emit('update:sortOptionIndex', newValue);
});

// Funktion zum Aktualisieren der Fenstergröße
const updateWindowWidth = () => {
  windowWidth.value = window.innerWidth;
};

// Event-Listener beim Mounten und Unmounten
onMounted(() => {
  window.addEventListener('resize', updateWindowWidth);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', updateWindowWidth);
});
</script>

<style scoped>
.sort-chip-wrapper {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
  margin-bottom: 15px;
  padding: 0 8px;
}

.sort-chip {
  font-weight: 500;
  transition: all 0.2s ease;
  text-align: center;
  justify-content: center;
}

.sort-chip :deep(.v-chip__content) {
  width: 100%;
  justify-content: center;
  text-align: center;
}

.sort-chip:hover {
  transform: translateY(-2px);
}

/* Mobile Anpassungen */
@media (max-width: 768px) {
  .sort-chip-wrapper {
    padding: 0 12px;
  }
}

/* Desktop Anpassungen */
@media (min-width: 769px) {
  .sort-chip-wrapper {
    gap: 12px;
  }
  
  .sort-chip {
    min-width: 120px;
  }
}
</style>