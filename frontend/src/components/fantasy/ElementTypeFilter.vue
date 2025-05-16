<template>
  <div class="element-chip-container">
    <v-chip
      v-for="element in elementTypes"
      :key="element"
      :color="isElementSelected(element) ? getElementTypeColor(element) : 'default'"
      :class="['element-chip', isElementSelected(element) ? 'selected' : '']"
      :text-color="isElementSelected(element) ? 'white' : ''"
      @click="toggleElementFilter(element)"
      filter
      :filter-icon="isElementSelected(element) ? 'mdi-check' : ''"
      variant="elevated"
    >
      {{ formatElementType(element) }}
    </v-chip>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{
  selectedElementTypes: string[];
}>();

const emit = defineEmits<{
  (e: 'update:selectedElementTypes', value: string[]): void;
  (e: 'filterChanged'): void;
}>();

// Element types available for filtering
const elementTypes = [
  'FIRE', 'WATER', 'EARTH', 'WIND', 'ELECTRIC', 
  'ICE', 'NATURE', 'SHADOW', 'LIGHT', 'POISON'
];

// Function to check if an element type is selected
function isElementSelected(element: string): boolean {
  return props.selectedElementTypes.includes(element);
}

// Function to toggle an element type filter
function toggleElementFilter(element: string) {
  const newSelectedElements = [...props.selectedElementTypes];
  
  if (isElementSelected(element)) {
    // Remove element if already selected
    const index = newSelectedElements.indexOf(element);
    if (index !== -1) {
      newSelectedElements.splice(index, 1);
    }
  } else {
    // Add element if not already selected
    newSelectedElements.push(element);
  }
  
  emit('update:selectedElementTypes', newSelectedElements);
  emit('filterChanged');
}

// Format element type for display
function formatElementType(element: string): string {
  return element.charAt(0) + element.slice(1).toLowerCase();
}

// Function to get the color for an element type
function getElementTypeColor(element: string): string {
  const elementColors: Record<string, string> = {
    'FIRE': 'red',
    'WATER': 'blue',
    'EARTH': 'brown',
    'WIND': 'teal',
    'ELECTRIC': 'amber',
    'ICE': 'light-blue',
    'NATURE': 'green',
    'SHADOW': 'deep-purple',
    'LIGHT': 'yellow',
    'POISON': 'purple'
  };
  
  return elementColors[element] || 'grey';
}
</script>

<style scoped>
.element-chip-container {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px;
  margin: 0 auto;
  padding: 8px;
}

.element-chip {
  margin: 0;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid rgba(0, 0, 0, 0.1);
}

.element-chip:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.element-chip.selected {
  font-weight: 500;
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

/* Mobile Anpassungen */
@media (max-width: 600px) {
  .element-chip-container {
    padding: 4px;
    gap: 4px;
  }
  
  .element-chip {
    font-size: 12px;
    height: 28px;
  }
}
</style>