<template>
  <v-navigation-drawer
    v-model="drawerModel"
    location="end"
    temporary
    width="300"
    class="filter-drawer"
  >
    <v-card flat class="h-100 d-flex flex-column">
      <!-- Drawer Header with Gradient -->
      <v-card-title class="text-white py-4 px-6 bg-gradient d-flex align-center">
        <v-icon class="mr-2" color="white">mdi-tune</v-icon>
        Filter & Sortierung
        <v-spacer></v-spacer>
        <v-btn
          icon="mdi-close"
          variant="text"
          color="white"
          size="small"
          @click="drawerModel = false"
        ></v-btn>
      </v-card-title>
      
      <v-divider></v-divider>
      
      <!-- Drawer Content -->
      <v-card-text class="py-4 px-4 flex-grow-1 overflow-y-auto">
        <!-- Content Type Filter -->
        <h3 class="text-subtitle-1 font-weight-medium mb-3 d-flex align-center">
          <v-icon class="mr-2" size="small" color="primary">mdi-view-grid</v-icon>
          Inhaltstyp
        </h3>
        
        <div class="mb-4 filter-buttons-container">
          <v-chip-group
            v-model="contentTypeModel"
            mandatory
            selected-class="primary"
            column
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
        
        <v-divider class="my-4"></v-divider>

        <!-- View Mode Toggle -->
        <h3 class="text-subtitle-1 font-weight-medium mb-3 d-flex align-center">
          <v-icon class="mr-2" size="small" color="primary">mdi-view-dashboard</v-icon>
          Anzeigemodus
        </h3>
        
        <div class="mb-4 filter-buttons-container">
          <v-chip-group
            v-model="viewModeModel"
            mandatory
            selected-class="secondary"
            column
            class="filter-chips-group"
          >
            <v-chip
              value="cards"
              filter
              variant="elevated"
              class="filter-chip ma-1"
            >
              <v-icon start size="small">mdi-view-grid-outline</v-icon>
              Karten
            </v-chip>
            <v-chip
              value="gallery"
              filter
              variant="elevated"
              class="filter-chip ma-1"
            >
              <v-icon start size="small">mdi-image-multiple-outline</v-icon>
              Galerie
            </v-chip>
          </v-chip-group>
        </div>
        
        <v-divider class="my-4"></v-divider>

        <h3 class="text-subtitle-1 font-weight-medium mb-3 d-flex align-center">
          <v-icon class="mr-2" size="small" color="primary">mdi-sort-variant</v-icon>
          Sortierung
        </h3>
        
        <!-- Sortierung-Komponente -->
        <SortToggle
          :sort-options="sortOptions"
          v-model:sort-option-index="sortOptionIndexModel"
        />
        
        <v-divider class="my-4"></v-divider>
        
        <!-- Typ-Filter-Komponente, nur für Pokemon-Ansicht -->
        <div v-if="contentTypeModel !== 'fantasy'">
          <h3 class="text-subtitle-1 font-weight-medium mb-3 d-flex align-center">
            <v-icon class="mr-2" size="small" color="primary">mdi-filter-variant</v-icon>
            Pokémon-Typen
          </h3>
          <TypeFilter
            v-model:selected-types="selectedTypesModel"
            :pokemon-types="pokemonTypes"
            @filter-changed="onFilterChanged"
          />
        </div>
      </v-card-text>
      
      <!-- Footer mit Gradient Hintergrund -->
      <div class="footer-container d-flex justify-space-between py-3 px-4 py-md-4 px-md-6">
        <v-btn 
          color="white" 
          variant="text" 
          @click="resetFilters"
          class="reset-btn"
        >
          <v-icon>mdi-refresh</v-icon>
        </v-btn>
        
        <v-spacer class="d-md-none"></v-spacer>
        
        <v-btn 
          color="white" 
          variant="elevated" 
          @click="drawerModel = false"
          class="apply-btn"
        >
          <v-icon>mdi-check</v-icon>
        </v-btn>
      </div>
    </v-card>
  </v-navigation-drawer>
</template>

<script setup lang="ts">
import { computed, watch } from 'vue';
import SortToggle from '@/components/home/SortToggle.vue';
import TypeFilter from '@/components/home/TypeFilter.vue';

// Props für die Komponente
const props = defineProps({
  drawer: {
    type: Boolean,
    required: true
  },
  contentType: {
    type: String,
    required: true
  },
  viewMode: {
    type: String,
    required: true
  },
  sortOptionIndex: {
    type: Number,
    required: true
  },
  selectedTypes: {
    type: Array,
    required: true
  },
  pokemonTypes: {
    type: Array,
    required: true
  },
  sortOptions: {
    type: Array,
    required: true
  }
});

// Events, die wir emittieren
const emit = defineEmits([
  'update:drawer',
  'update:contentType',
  'update:viewMode',
  'update:sortOptionIndex',
  'update:selectedTypes',
  'filter-changed',
  'reset-filters'
]);

// Lokale v-model Werte mit zwei-Wege-Bindung zu den Props
const drawerModel = computed({
  get: () => props.drawer,
  set: (value) => emit('update:drawer', value)
});

const contentTypeModel = computed({
  get: () => props.contentType,
  set: (value) => emit('update:contentType', value)
});

const viewModeModel = computed({
  get: () => props.viewMode,
  set: (value) => emit('update:viewMode', value)
});

const sortOptionIndexModel = computed({
  get: () => props.sortOptionIndex,
  set: (value) => emit('update:sortOptionIndex', value)
});

const selectedTypesModel = computed({
  get: () => props.selectedTypes,
  set: (value) => emit('update:selectedTypes', value)
});

// Event-Handler für Filter-Änderungen
function onFilterChanged() {
  emit('filter-changed');
}

// Event-Handler für Filter zurücksetzen
function resetFilters() {
  emit('reset-filters');
}
</script>

<style scoped>
/* Styles für den Filter-Drawer */
.filter-drawer :deep(.v-card) {
  background-color: #f4f4f9;
  border-left: 4px solid #6200ee;
}

.bg-gradient {
  background: linear-gradient(135deg, #6890F0 0%, #705898 100%);
}

.filter-drawer :deep(.v-card-title) {
  font-weight: 500;
}

.filter-drawer :deep(.v-divider) {
  background-color: #e0e0e0;
}

.filter-drawer :deep(.v-card-actions) {
  background-color: #fafafa;
  border-top: 1px solid #e0e0e0;
}

.reset-btn {
  color: #6200ee;
}

.apply-btn {
  background: linear-gradient(135deg, #6890F0 0%, #705898 100%);
  color: white;
}

/* Styles für den Footer im Drawer */
.footer-container {
  background: linear-gradient(135deg, #705898 0%, #6890F0 100%);
  color: white;
  border-top: 1px solid #e0e0e0;
}

.footer-container .v-btn {
  min-width: 120px;
}

.footer-container .v-icon {
  font-size: 1.2rem;
}

/* Styles für die Filter-Chips im Drawer */
.filter-buttons-container {
  display: flex;
  justify-content: center;
  width: 100%;
}

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

/* Sicherstellen, dass die Filter-Chips immer sichtbar sind und genug Platz haben */
.v-chip-group.filter-chips-group .v-chip {
  margin: 4px !important;
}
</style>