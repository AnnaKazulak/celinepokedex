<template>
  <v-dialog 
    v-model="dialogVisible" 
    max-width="800px"
    :content-class="$style.fantasyDialogWrapper"
  >
    <v-card 
      class="mx-auto overflow-hidden position-relative d-flex flex-column justify-space-between" 
      :class="[$style.fantasyMainCard, {'has-image': character.imageUrl}]"
      width="100%" 
      max-width="600px"
      min-height="520px"
      rounded="xl"
      elevation="8"
    >
      <!-- Title first - ganz oben -->
      <v-card-title :class="$style.dialogTitle" class="text-white text-center font-weight-medium letter-spacing-1 position-relative">
        Bearbeiten
      </v-card-title>

      <!-- Close-Button nach rechts oben positioniert -->
      <v-btn
        icon
        @click="closeDialog"
        :class="$style.closeBtn"
        class="text-white position-absolute"
        size="small"
        variant="text"
      >
        <v-icon>mdi-close</v-icon>
      </v-btn>
      
      <div :class="$style.contentWrapper">
        <!-- Schwebendes Bild etwas nach unten versetzt -->
        <div v-if="character.imageUrl" class="d-flex justify-center mx-auto my-5 position-relative" :class="$style.floatingImageContainer">
          <v-img
            :src="character.imageUrl"
            :class="$style.fantasyFloatingImage"
            contain
            width="240"
            height="240"
          ></v-img>
        </div>
        
        <v-form @submit.prevent="updateCharacter">
          <div class="d-flex flex-column gap-4">
            <div class="mb-2">
              <v-text-field
                v-model="editedName"
                label="Name deines Fantasy-Charakters"
                hint="Bearbeite den Namen deines Charakters"
                variant="underlined"
                density="compact"
                :disabled="isSaving"
              ></v-text-field>
            </div>
            
            <!-- Base Animal Select -->
            <div class="mb-2">
              <v-select
                v-model="editedBaseAnimal"
                :items="baseAnimalOptions"
                label="Base Animal"
                hint="Wähle das Basistier aus"
                variant="underlined"
                density="compact"
                :disabled="isSaving"
              ></v-select>
            </div>
            
            <!-- Element Type Select -->
            <div class="mb-2">
              <v-select
                v-model="editedElementType"
                :items="elementTypeOptions"
                label="Element Type"
                hint="Wähle den Elementtyp aus"
                variant="underlined"
                density="compact"
                :disabled="isSaving"
              ></v-select>
            </div>
            
            <div class="mb-2">
              <v-textarea
                v-model="editedPrompt"
                label="Beschreibung deines Fantasy-Charakters"
                hint="Bearbeite die Beschreibung deines Charakters"
                rows="3"
                auto-grow
                counter
                variant="underlined"
                density="compact"
                :disabled="isSaving"
              ></v-textarea>
            </div>
          </div>
          
          <div v-if="error" class="mt-4">
            <v-alert type="error" dismissible>
              {{ error }}
            </v-alert>
          </div>
        </v-form>
      </div>
      
      <!-- Footer mit Aktionen -->
      <div :class="$style.fantasyFooter" class="d-flex justify-center align-center py-4 px-6">
        <div class="d-flex w-100 justify-center">
          <v-spacer></v-spacer>
          <v-btn 
            color="white" 
            variant="text" 
            @click="closeDialog"
          >
            Abbrechen
          </v-btn>
          <v-btn 
            color="white" 
            variant="elevated" 
            @click="updateCharacter"
            :class="$style.saveBtn"
            :loading="isSaving"
          >
            <v-icon start>mdi-content-save</v-icon>
            Speichern
          </v-btn>
          <v-spacer></v-spacer>
        </div>
      </div>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue';
import axios from 'axios';
import { API_ENDPOINTS } from '../utils/constants';
import { eventBus } from '../utils/eventBus';
import type { FantasyCharacter } from '../types/pokemon';

// Props and emits
const props = defineProps<{
  dialog: boolean;
  character: FantasyCharacter;
}>();

const emit = defineEmits<{
  'update:dialog': [value: boolean];
  'character-updated': [character: FantasyCharacter];
}>();

// Reactive variables
const dialogVisible = ref(props.dialog);
const editedName = ref<string>('');
const editedPrompt = ref('');
const editedBaseAnimal = ref('');
const editedElementType = ref('');
const error = ref('');
const isSaving = ref(false);

// Optionen für die Select-Felder mit den korrekten Werten aus dem System
const baseAnimalOptions = [
  'CAT', 'LIZARD', 'BIRD', 
  'FROG', 'FOX', 'SNAKE', 'HORSE', 'TURTLE',
  'LION', 'EAGLE', 'DEER'
];

const elementTypeOptions = [
  'FIRE', 'WATER', 'EARTH', 'WIND', 'ELECTRIC', 
  'ICE', 'NATURE', 'SHADOW', 'LIGHT', 'POISON'
];

// Watch for dialog prop changes
watch(() => props.dialog, (newVal) => {
  dialogVisible.value = newVal;
});

// Watch for character prop changes
watch(() => props.character, (newVal) => {
  if (newVal) {
    editedName.value = newVal.name || '';
    editedPrompt.value = newVal.prompt || '';
    editedBaseAnimal.value = newVal.baseAnimal || '';
    editedElementType.value = newVal.elementType || '';
  }
});

// Watch for dialog changes and emit updates
watch(dialogVisible, (newVal) => {
  emit('update:dialog', newVal);
});

onMounted(() => {
  // Initialize with character data
  if (props.character) {
    editedName.value = props.character.name || '';
    editedPrompt.value = props.character.prompt || '';
    editedBaseAnimal.value = props.character.baseAnimal || '';
    editedElementType.value = props.character.elementType || '';
  }
});

// Close the dialog
const closeDialog = () => {
  dialogVisible.value = false;
};

// Update the character
const updateCharacter = async () => {
  if (!editedPrompt.value || isSaving.value) return;
  
  isSaving.value = true;
  error.value = '';

  // Stellt sicher, dass der Name mit einem Großbuchstaben beginnt
  let nameToSave = editedName.value;
  if (nameToSave && nameToSave.length > 0) {
    nameToSave = nameToSave.charAt(0).toUpperCase() + nameToSave.slice(1);
    editedName.value = nameToSave; // Aktualisiere auch das Eingabefeld
  }

  try {
    const response = await axios.put(`${API_ENDPOINTS.FANTASY_CHARACTERS}/${props.character.id}`, {
      name: nameToSave,
      prompt: editedPrompt.value,
      baseAnimal: editedBaseAnimal.value,
      elementType: editedElementType.value,
      imageUrl: props.character.imageUrl
    });

    if (response.data) {
      const updatedCharacter = response.data;
      
      // Close dialog
      dialogVisible.value = false;
      
      // Emit update event
      emit('character-updated', updatedCharacter);
      
      // Emit via event bus for global updates
      eventBus.emit('fantasy-character-updated', updatedCharacter);
    } else {
      throw new Error('Failed to update character');
    }
  } catch (err: any) {
    console.error('Error updating character:', err);

    if (err.response) {
      if (err.response.data && err.response.data.error) {
        error.value = `Error: ${err.response.data.error}`;
      } else {
        error.value = `Server error (${err.response.status}): Please try again later`;
      }
    } else if (err.request) {
      error.value = 'No response from server. Please check if the backend is running.';
    } else {
      error.value = 'Failed to update character: ' + (err.message || 'Unknown error');
    }
  } finally {
    isSaving.value = false;
  }
};
</script>

<style module>
.fantasyDialogWrapper {
  box-shadow: none;
  background: transparent;
}

.fantasyMainCard {
  background: white;
  padding-top: 0;
  display: flex;
  flex-direction: column;
}

.fantasyMainCard.has-image {
  padding-top: 0;
}

.contentWrapper {
  flex: 1;
  overflow-y: auto;
  padding: 0 24px;
  max-height: calc(80vh - 140px); /* Ensure content doesn't exceed viewport minus header/footer */
}

.dialogTitle {
  background: linear-gradient(135deg, #6890F0 0%, #705898 100%);
  padding: 16px 20px;
  z-index: 25;
}

.closeBtn {
  top: 8px;
  right: 8px;
  z-index: 30;
  background-color: rgba(255, 255, 255, 0.2);
}

.closeBtn:hover {
  background-color: rgba(255, 255, 255, 0.3);
}

.floatingImageContainer {
  width: 240px;
  z-index: 5;
}

.fantasyFloatingImage {
  border-radius: 8px;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.2);
}

.fantasyFooter {
  color: white;
  transition: background-color 0.5s ease;
  background: linear-gradient(135deg, #6890F0 0%, #705898 100%);
  position: sticky;
  bottom: 0;
  z-index: 10;
}

.saveBtn {
  background-color: rgba(255, 255, 255, 0.2) !important;
}

@media (max-width: 600px) {
  .contentWrapper {
    max-height: calc(100vh - 140px);
    padding: 0 16px;
  }

  .floatingImageContainer {
    width: 180px;
  }
  
  .fantasyFloatingImage {
    width: 180px;
    height: 180px;
  }
  
  .fantasyFooter {
    flex-direction: column;
    gap: 16px;
  }
}
</style>