<template>
  <div>
    <!-- Confirmation Dialog for missing Pokemon -->
    <v-dialog v-model="showDialog" max-width="400" content-class="missing-pokemon-dialog">
      <v-card class="position-relative rounded-lg overflow-hidden elevation-5" style="padding-top: 60px">
        <!-- Schwebendes Bild Container -->
        <div class="position-absolute" style="top: 0px; left: 65%; transform: translateX(-50%); z-index: 10; width: 140px; height: 140px;">
          <v-img
            :src="pokemon.imageUrl || 'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/0.png'"
            width="140"
            height="140"
            class="pokemon-image-shadow"
            contain
          ></v-img>
        </div>

        <v-card-item :class="`pa-4 d-flex align-start ${textColorClass}`" :style="{ backgroundColor: dominantColor }">
          <v-icon color="white" class="mr-2">mdi-pokeball</v-icon>
          <div class="d-flex flex-column align-start">
            <span class="text-h5 font-weight-bold">{{ pokemon.name }}</span>
            <span class="text-subtitle-2 text-white text-opacity-85 mt-n1">#{{ pokemon.pokedexNumber }}</span>
          </div>
        </v-card-item>

        <v-card-text class="pa-5 pt-6 pb-2">
          <div class="text-center">            
            <p class="text-body-1 text-medium-emphasis">
              Dieses Pokémon ist noch nicht in deiner Sammlung. Möchtest du es jetzt hinzufügen?
            </p>
          </div>
        </v-card-text>

        <v-card-actions class="pa-6 pt-3">
          <v-spacer></v-spacer>
          <v-btn color="grey-darken-1" variant="text" @click="closeDialog" class="text-none font-weight-medium">
            Abbrechen
          </v-btn>
          <v-btn :color="dominantColor" variant="elevated" @click="createPokemon" 
                class="text-none font-weight-bold text-white elevation-2 px-5 py-2 create-button">
            <v-icon class="mr-1">mdi-plus</v-icon>
            Hinzufügen
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue';

// Props
const props = defineProps<{
  modelValue: boolean;
  pokemon: { name: string, pokedexNumber: string, imageUrl?: string };
  dominantColor: string;
}>();

// Emit events
const emit = defineEmits<{
  (event: 'update:modelValue', value: boolean): void;
  (event: 'create', pokemon: { name: string, pokedexNumber: string, imageUrl?: string }): void;
}>();

// Computed
const textColorClass = computed(() => 'text-white');

// Sync with v-model
const showDialog = ref(props.modelValue);

// Watchers
watch(() => props.modelValue, (newValue) => {
  showDialog.value = newValue;
});

watch(showDialog, (newValue) => {
  emit('update:modelValue', newValue);
});

// Methods
function closeDialog() {
  showDialog.value = false;
}

function createPokemon() {
  emit('create', props.pokemon);
  showDialog.value = false;
}
</script>

<style scoped>
.missing-pokemon-dialog :deep(.v-overlay__content) {
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.2);
}

.pokemon-image-shadow {
  filter: drop-shadow(0 8px 16px rgba(0, 0, 0, 0.3));
  transform: scale(1.05);
  transition: transform 0.5s ease;
}

.create-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.2) !important;
}

/* Responsive für Mobile */
@media (max-width: 600px) {
  .v-card {
    padding-top: 50px !important;
  }
  
  .position-absolute {
    top: 5px !important;
    width: 120px !important;
    height: 120px !important;
  }
  
  .pokemon-image-shadow {
    width: 120px !important;
    height: 120px !important;
  }
  
  .text-h5 {
    font-size: 1.15rem !important;
  }
  
  .text-subtitle-2 {
    font-size: 0.8rem !important;
  }
}
</style>