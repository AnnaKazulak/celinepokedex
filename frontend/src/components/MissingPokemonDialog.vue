<template>
  <div>
    <!-- Confirmation Dialog for missing Pokemon -->
    <v-dialog v-model="showDialog" max-width="400px" content-class="missing-pokemon-dialog">
      <v-card class="dialog-card">
        <!-- Schwebendes Bild Container -->
        <div class="floating-image-container">
          <v-img
            :src="pokemon.imageUrl || 'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/0.png'"
            class="pokemon-floating-image"
            contain
          ></v-img>
        </div>

        <v-card-title class="dialog-header" 
                     :style="{ background: dominantColor, color: 'white' }">
          <v-icon color="white" class="mr-2">mdi-pokeball</v-icon>
          <div class="pokemon-title-container">
            <span class="pokemon-dialog-title">{{ pokemon.name }}</span>
            <span class="pokemon-number-subtitle">#{{ pokemon.pokedexNumber }}</span>
          </div>
        </v-card-title>

        <v-card-text class="dialog-content">
          <div class="pokemon-info-container">            
            <p class="message">
              Dieses Pokémon ist noch nicht in deiner Sammlung. Möchtest du es jetzt hinzufügen?
            </p>
          </div>
        </v-card-text>

        <v-card-actions class="dialog-actions">
          <v-spacer></v-spacer>
          <v-btn color="grey-darken-1" variant="text" @click="closeDialog" class="cancel-btn">
            Abbrechen
          </v-btn>
          <v-btn :color="dominantColor" variant="elevated" @click="createPokemon" class="create-btn">
            <v-icon class="mr-1">mdi-plus</v-icon>
            Hinzufügen
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';

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

.dialog-card {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
  padding-top: 60px;
  position: relative;
}

.floating-image-container {
  position: absolute;
  top: 0px;
  left: 65%;
  transform: translateX(-50%);
  z-index: 10;
  width: 140px;
  height: 140px;
}

.pokemon-floating-image {
  width: 140px;
  height: 140px;
  filter: drop-shadow(0 8px 16px rgba(0, 0, 0, 0.3));
  transform: scale(1.05);
  transition: transform 0.5s ease;
}

.dialog-header {
  font-size: 1.3rem;
  padding: 16px 20px;
  font-weight: 600;
  letter-spacing: 0.5px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: flex-start;
}

.pokemon-title-container {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.pokemon-dialog-title {
  font-size: 1.3rem;
  font-weight: 600;
  line-height: 1.3;
}

.pokemon-number-subtitle {
  font-size: 0.85rem;
  opacity: 0.85;
  font-weight: 400;
  margin-top: -2px;
}

.dialog-content {
  padding: 20px 20px 10px 20px;
}

.pokemon-info-container {
  text-align: center;
}

.message {
  color: rgba(0, 0, 0, 0.7);
  font-size: 1.1rem;
  line-height: 1.5;
  margin: 0;
}

.dialog-actions {
  padding: 12px 24px 24px;
}

.cancel-btn {
  font-weight: 500;
  text-transform: none;
  letter-spacing: 0.5px;
}

.create-btn {
  color: white;
  font-weight: 600;
  text-transform: none;
  letter-spacing: 0.5px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
  padding: 0 20px;
  height: 40px;
}

.create-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.2);
}

/* Responsive für Mobile */
@media (max-width: 600px) {
  .floating-image-container {
    top: 5px;
    left: 65%;
    width: 120px;
    height: 120px;
  }
  
  .pokemon-floating-image {
    width: 120px;
    height: 120px;
  }
  
  .dialog-card {
    padding-top: 50px;
  }
  
  .dialog-header {
    font-size: 1.1rem;
  }
  
  .pokemon-dialog-title {
    font-size: 1.15rem;
  }
  
  .pokemon-number-subtitle {
    font-size: 0.8rem;
  }
}
</style>