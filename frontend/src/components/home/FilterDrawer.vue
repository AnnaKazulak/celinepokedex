<template>
  <v-navigation-drawer
    v-model="drawerModel"
    location="end"
    :temporary="!isLargeScreen"
    width="300"
    class="filter-drawer"
  >
    <v-card flat class="h-100 d-flex flex-column">
      <div class="d-flex justify-end pa-2 mr-5">
      <v-btn
        icon="mdi-close"
        variant="text"
        color="#705898"
        size="small"
        @click="drawerModel = false"
      ></v-btn>
      </div>
      
      <!-- Drawer Content -->
      <v-card-text class="py-4 px-4 flex-grow-1 overflow-y-auto">
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
        
        <!-- Typ-Filter-Komponente für Pokemon und "Alle" Ansicht -->
        <div v-if="contentTypeModel !== 'fantasy'">
          <h3 class="text-subtitle-1 font-weight-medium mb-3 d-flex align-center">
            <v-icon class="mr-2" size="small" color="primary">mdi-filter-variant</v-icon>
            Sort Pokémons
          </h3>
          <TypeFilter
            v-model:selected-types="selectedTypesModel"
            :pokemon-types="pokemonTypes"
            @filter-changed="onFilterChanged"
          />
        </div>
        
        <!-- Element-Type-Filter-Komponente für Fantasy und "Alle" Ansicht -->
        <div v-if="contentTypeModel !== 'pokemon'">
          <h3 class="text-subtitle-1 font-weight-medium mb-3 d-flex align-center mt-4">
            <v-icon class="mr-2" size="small" color="primary">mdi-filter-variant</v-icon>
            Sort Fantasy Characters
          </h3>
          <ElementTypeFilter
            v-model:selected-element-types="selectedElementTypesModel"
            @filter-changed="onFilterChanged"
          />
        </div>
      </v-card-text>
      
      <!-- Footer  -->
      <div class="footer-container d-flex justify-space-between py-3 px-4 py-md-4 px-md-6">
        <v-btn 
          color="#705898"
          variant="text" 
          @click="resetFilters"
          class="reset-btn"
        >
          <v-icon>mdi-refresh</v-icon>
        </v-btn>
        
        <v-spacer class="d-md-none"></v-spacer>
        
        <v-btn 
          color="#705898" 
          variant="text" 
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
import { useDisplay } from 'vuetify/lib/framework.mjs';
import SortToggle from '@/components/home/SortToggle.vue';
import TypeFilter from '@/components/home/TypeFilter.vue';
import ElementTypeFilter from '@/components/fantasy/ElementTypeFilter.vue';
import { PokemonType } from '@/types/pokemon';

// Use Vuetify's display composable for responsive breakpoints
const { mdAndUp } = useDisplay();

// Computed property for large screen detection
const isLargeScreen = computed(() => mdAndUp.value);

// Interface für die Sortieroption
interface SortOption {
  text: string;
  value: string;
  icon?: string;
}

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
    type: Array as () => PokemonType[],
    required: true
  },
  selectedElementTypes: {
    type: Array as () => string[],
    required: true
  },
  pokemonTypes: {
    type: Array as () => { label: string; value: PokemonType }[],
    required: true
  },
  sortOptions: {
    type: Array as () => SortOption[],
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
  'update:selectedElementTypes',
  'filter-changed',
  'reset-filters',
  'screen-size-changed'
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

const selectedElementTypesModel = computed({
  get: () => props.selectedElementTypes,
  set: (value) => emit('update:selectedElementTypes', value)
});

// Watchers für die Filterbuttons, um sicherzustellen, dass beim erneuten Klicken
// auf den gleichen Button ebenfalls ein Event ausgelöst wird
watch(contentTypeModel, () => {
  onFilterChanged();
});

watch(viewModeModel, () => {
  onFilterChanged();
});

// When switching from mobile to desktop, adjust content padding
watch(isLargeScreen, (newValue) => {
  // Emit an event that can be used to adjust layout
  setTimeout(() => {
    emit('screen-size-changed', newValue);
  }, 0);
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


.filter-drawer :deep(.v-card-title) {
  font-weight: 500;
}

/* Responsive styles */
@media (min-width: 960px) {
  /* Hide scrim on desktop as we're not in overlay mode */
  .filter-drawer :deep(.v-navigation-drawer__scrim) {
    display: none !important;
  }
}

.filter-drawer :deep(.v-divider) {
  background-color:rgb(244, 243, 243);
}

.filter-drawer :deep(.v-card-actions) {
  background-color: #fafafa;
  /* border-top: 1px solid #e0e0e0; */
}

.reset-btn {
  color: #6200ee;
}

.apply-btn {
  color: #705898 ;
}

/* Styles für den Footer im Drawer */
.footer-container {
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