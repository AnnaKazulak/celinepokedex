<template>
  <v-dialog 
    v-model="dialogVisible" 
    max-width="800px"
    content-class="fantasy-dialog-wrapper"
  >
    <v-card class="fantasy-main-card" :class="{'has-image': character.imageUrl}">
      <!-- Title first - ganz oben -->
      <v-card-title class="dialog-title">
        Fantasy Character bearbeiten
      </v-card-title>

      <!-- Close-Button nach rechts oben positioniert -->
      <v-btn
        icon
        @click="closeDialog"
        class="close-btn"
      >
        <v-icon>mdi-close</v-icon>
      </v-btn>
      
      <div class="card-header">
        <!-- Schwebendes Bild etwas nach unten versetzt -->
        <div v-if="character.imageUrl" class="floating-image-container">
          <v-img
            :src="character.imageUrl"
            class="fantasy-floating-image"
            contain
          ></v-img>
        </div>
        
        <v-form @submit.prevent="updateCharacter">
          <div class="fantasy-form">
            <div class="form-section">
              <v-text-field
                v-model="editedName"
                label="Name deines Fantasy-Charakters"
                hint="Bearbeite den Namen deines Charakters"
                variant="underlined"
                density="compact"
                :disabled="isSaving"
              ></v-text-field>
            </div>
            
            <div class="form-section">
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
          
          <div v-if="error" class="error-message mt-4">
            <v-alert type="error" dismissible>
              {{ error }}
            </v-alert>
          </div>
        </v-form>
      </div>
      
      <!-- Footer mit Aktionen -->
      <div class="fantasy-footer" :style="{ background: '#6890F0' }">
        <div class="footer-section actions">
          <v-spacer></v-spacer>
          <v-btn 
            color="white" 
            variant="text" 
            @click="closeDialog"
            class="cancel-btn"
          >
            Abbrechen
          </v-btn>
          <v-btn 
            color="white" 
            variant="elevated" 
            @click="updateCharacter"
            class="save-btn"
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
const editedName = ref('');
const editedPrompt = ref('');
const error = ref('');
const isSaving = ref(false);

// Watch for dialog prop changes
watch(() => props.dialog, (newVal) => {
  dialogVisible.value = newVal;
});

// Watch for character prop changes
watch(() => props.character, (newVal) => {
  if (newVal) {
    editedName.value = newVal.name;
    editedPrompt.value = newVal.prompt;
  }
});

// Watch for dialog changes and emit updates
watch(dialogVisible, (newVal) => {
  emit('update:dialog', newVal);
});

onMounted(() => {
  // Initialize with character data
  if (props.character) {
    editedName.value = props.character.name;
    editedPrompt.value = props.character.prompt;
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
  } catch (err) {
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

<style scoped>
/* Styling für den Dialog selbst */
:deep(.fantasy-dialog-wrapper) {
  box-shadow: none;
  background: transparent;
}

/* Dialog Card mit festgelegter Höhe */
.fantasy-main-card {
  width: 100%;
  max-width: 600px;
  min-height: 520px;
  background: white;
  padding-top: 0;
  border-radius: 16px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  margin: 0 auto;
  overflow: hidden;
  position: relative;
}

.fantasy-main-card.has-image {
  padding-top: 0;
}

.dialog-title {
  background: linear-gradient(135deg, #6890F0 0%, #705898 100%);
  color: white;
  padding: 16px 20px;
  position: relative;
  z-index: 25;
  text-align: center;
  font-weight: 500;
  letter-spacing: 0.5px;
}

/* Neu positionierter Close-Button */
.close-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  z-index: 30;
  background-color: rgba(255, 255, 255, 0.2);
  color: white;
}

.close-btn:hover {
  background-color: rgba(255, 255, 255, 0.3);
}

.floating-image-container {
  position: relative;
  margin: 20px auto;
  width: 240px;
  display: flex;
  justify-content: center;
  z-index: 5;
}

.fantasy-floating-image {
  width: 240px;
  height: 240px;
  border-radius: 8px;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.2);
}

.card-header {
  padding: 16px 24px;
  flex-grow: 1;
}

.fantasy-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-section {
  margin-bottom: 8px;
}

.error-message {
  margin-top: 16px;
}

.fantasy-footer {
  color: white;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 16px 24px;
  margin-top: auto;
  transition: background-color 0.5s ease;
}

.footer-section {
  display: flex;
  gap: 16px;
  justify-content: center;
  width: 100%;
}

.actions {
  display: flex;
  gap: 16px;
}

.save-btn {
  background-color: rgba(255, 255, 255, 0.2) !important;
}

/* Mobile Anpassungen */
@media (max-width: 600px) {
  .floating-image-container {
    width: 180px;
    margin: 15px auto;
  }
  
  .fantasy-floating-image {
    width: 180px;
    height: 180px;
  }
  
  .fantasy-footer {
    flex-direction: column;
    gap: 16px;
  }
}
</style>