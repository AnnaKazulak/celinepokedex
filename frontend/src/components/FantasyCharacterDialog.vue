<template>
  <v-dialog 
    v-model="dialogVisible" 
    max-width="800px"
    content-class="fantasy-dialog-wrapper"
  >
    <v-card class="fantasy-main-card" :class="{'has-image': generatedImageUrl}">
      <!-- Title first - ganz oben -->
      <v-card-title class="dialog-title">
        Create Fantasy Character
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
        <div v-if="generatedImageUrl" class="floating-image-container">
          <v-img
            :src="generatedImageUrl"
            class="fantasy-floating-image"
            contain
          ></v-img>
        </div>
        
        <!-- Beschreibung unter dem Bild, außerhalb des Footers -->
        <div v-if="generatedImageUrl && !isGenerating" class="character-description">
          <p class="description-text">{{ prompt }}</p>
        </div>
        
        <div class="level-text"></div>
        
        <v-form @submit.prevent="generateImage">
          <!-- Eingabefeld nur anzeigen, wenn kein Bild generiert wurde -->
          <div v-if="!generatedImageUrl || isGenerating" class="fantasy-form">
            <div class="form-section">
              <v-textarea
                v-model="prompt"
                label="Beschreibe deinen Fantasy-Charakter"
                hint="Je detaillierter die Beschreibung, desto besser das Ergebnis"
                rows="3"
                auto-grow
                counter
                variant="underlined"
                density="compact"
                :disabled="isGenerating"
              ></v-textarea>
              
              <div class="text-center mt-4 mb-4">
                <v-btn 
                  color="primary" 
                  :loading="isGenerating"
                  :disabled="!prompt || isGenerating"
                  @click="generateImage"
                  class="generate-btn"
                >
                  {{ isGenerating ? 'Generiere...' : 'Charakter erstellen' }}
                </v-btn>
              </div>
            </div>
          </div>
          
          <!-- Einfacherer Ladebildschirm, der garantiert sichtbar ist -->
          <v-card v-if="isGenerating" class="mt-4 mb-4 pa-4 loading-status" variant="outlined">
            <div class="text-center">
              <v-icon size="x-large" color="primary" class="mb-3">mdi-creation</v-icon>
              <h3 class="text-h6 mb-2">Dein Fantasy-Charakter wird erstellt</h3>
              <p class="text-body-2 mb-4">Die KI arbeitet an deinem einzigartigen Charakter...</p>
              
              <v-progress-linear
                indeterminate
                color="primary"
                class="mb-3"
                height="6"
                rounded
              ></v-progress-linear>
              
              <!-- Vereinfachte Schrittanzeige -->
              <div class="d-flex justify-space-between mb-2">
                <v-chip
                  size="small"
                  :color="loadingStep >= 1 ? 'primary' : 'grey-lighten-1'" 
                  :variant="loadingStep >= 1 ? 'elevated' : 'flat'"
                >
                  <v-icon size="x-small" start>mdi-pencil</v-icon>Analysieren
                </v-chip>
                <v-chip
                  size="small"
                  :color="loadingStep >= 2 ? 'primary' : 'grey-lighten-1'"
                  :variant="loadingStep >= 2 ? 'elevated' : 'flat'"
                >
                  <v-icon size="x-small" start>mdi-brush</v-icon>Zeichnen
                </v-chip>
                <v-chip
                  size="small"
                  :color="loadingStep >= 3 ? 'primary' : 'grey-lighten-1'"
                  :variant="loadingStep >= 3 ? 'elevated' : 'flat'"
                >
                  <v-icon size="x-small" start>mdi-palette</v-icon>Details
                </v-chip>
                <v-chip
                  size="small"
                  :color="loadingStep >= 4 ? 'primary' : 'grey-lighten-1'"
                  :variant="loadingStep >= 4 ? 'elevated' : 'flat'"
                >
                  <v-icon size="x-small" start>mdi-image</v-icon>Fertigstellen
                </v-chip>
              </div>
              <p class="text-caption mt-3">Dieser Vorgang kann 15-30 Sekunden dauern</p>
            </div>
          </v-card>
          
          <div v-if="error" class="error-message mt-4">
            <v-alert type="error" dismissible>
              {{ error }}
            </v-alert>
          </div>
        </v-form>
      </div>
      
      <!-- Footer nur mit Aktionen -->
      <div v-if="generatedImageUrl && !isGenerating" class="fantasy-footer" :style="{ background: '#6890F0' }">
        <div class="footer-section actions">
          <v-spacer></v-spacer>
          <v-btn 
            color="white" 
            variant="text" 
            @click="resetForm"
            class="cancel-btn"
          >
            Neuer Charakter
          </v-btn>
          <v-btn 
            color="white" 
            variant="text" 
            @click="saveCharacter"
            class="save-btn"
            :loading="isSaving"
          >
            <v-icon start>mdi-content-save</v-icon>
            Speichern
          </v-btn>
          <v-btn 
            color="white" 
            variant="elevated" 
            @click="downloadImage"
            class="download-btn"
          >
            <v-icon start>mdi-download</v-icon>
            Download
          </v-btn>
          <v-spacer></v-spacer>
        </div>
      </div>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import axios from 'axios';
import { API_ENDPOINTS } from '../utils/constants';
import { eventBus } from '../utils/eventBus';
import type { FantasyCharacter } from '../types/pokemon';

// Props and emits
const props = defineProps<{
  dialog: boolean;
}>();

const emit = defineEmits<{
  'update:dialog': [value: boolean];
  'character-created': [character: FantasyCharacter];
}>();

// Reactive variables
const dialogVisible = ref(props.dialog);
const prompt = ref('');
const isGenerating = ref(false);
const generatedImageUrl = ref('');
const error = ref('');
const loadingStep = ref(0); // Status für den Ladebalken-Fortschritt
const isSaving = ref(false); // Status für den Speichervorgang

// Watch for dialog prop changes
watch(() => props.dialog, (newVal) => {
  dialogVisible.value = newVal;
});

// Watch for dialog changes and emit updates
watch(dialogVisible, (newVal) => {
  emit('update:dialog', newVal);
});

// Close the dialog
const closeDialog = () => {
  dialogVisible.value = false;
  resetForm();
};

// Reset the form
const resetForm = () => {
  prompt.value = '';
  generatedImageUrl.value = '';
  error.value = '';
  loadingStep.value = 0;
};

// Generate image
const generateImage = async () => {
  if (!prompt.value || isGenerating.value) return;
  
  isGenerating.value = true;
  error.value = '';
  generatedImageUrl.value = '';
  loadingStep.value = 0;
  
  // Simuliere Fortschrittsanimation für den Ladebalken
  const loadingInterval = setInterval(() => {
    if (loadingStep.value < 4) {
      loadingStep.value++;
    }
  }, 3000); // Alle 3 Sekunden ein neuer Schritt
  
  try {
    // Erster Schritt wird direkt ausgelöst
    loadingStep.value = 1;
    
    const response = await axios.post(API_ENDPOINTS.GENERATE_IMAGE, {
      prompt: prompt.value
    });
    
    if (response.data && response.data.imageUrl) {
      generatedImageUrl.value = response.data.imageUrl;
    } else {
      throw new Error('No image URL returned from server');
    }
  } catch (err) {
    console.error('Error generating image:', err);
    
    // More detailed error handling
    if (err.response) {
      // The request was made and the server responded with a status code
      // that falls out of the range of 2xx
      if (err.response.data && err.response.data.error) {
        error.value = `Error: ${err.response.data.error}`;
      } else {
        error.value = `Server error (${err.response.status}): Please try again later`;
      }
    } else if (err.request) {
      // The request was made but no response was received
      error.value = 'No response from server. Please check if the backend is running.';
    } else {
      // Something happened in setting up the request
      error.value = 'Failed to generate image: ' + (err.message || 'Unknown error');
    }
  } finally {
    clearInterval(loadingInterval);
    loadingStep.value = 4; // Setze auf den letzten Schritt, falls es fertig ist
    isGenerating.value = false;
  }
};

// Download the generated image
const downloadImage = () => {
  if (!generatedImageUrl.value) return;
  
  // For base64 images
  if (generatedImageUrl.value.startsWith('data:')) {
    const link = document.createElement('a');
    link.href = generatedImageUrl.value;
    link.download = `fantasy-character-${Date.now()}.png`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    return;
  }
  
  // For URL images
  const link = document.createElement('a');
  link.href = generatedImageUrl.value;
  link.download = `fantasy-character-${Date.now()}.png`;
  link.target = '_blank';
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
};

// Save the character
const saveCharacter = async () => {
  if (!generatedImageUrl.value || isSaving.value) return;

  isSaving.value = true;
  error.value = '';

  try {
    const response = await axios.post(API_ENDPOINTS.SAVE_CHARACTER, {
      prompt: prompt.value,
      imageUrl: generatedImageUrl.value
    });

    if (response.data) {
      const savedCharacter = response.data;
      
      // Close dialog
      dialogVisible.value = false;
      
      // Emit event to parent component
      emit('character-created', savedCharacter);
      
      // Also emit global event for HomeView
      eventBus.emit('fantasy-character-created', savedCharacter);
      
      // Reset form after successful save and close
      setTimeout(() => {
        resetForm();
      }, 300);
    } else {
      throw new Error('Failed to save character');
    }
  } catch (err) {
    console.error('Error saving character:', err);

    if (err.response) {
      if (err.response.data && err.response.data.error) {
        error.value = `Error: ${err.response.data.error}`;
      } else {
        error.value = `Server error (${err.response.status}): Please try again later`;
      }
    } else if (err.request) {
      error.value = 'No response from server. Please check if the backend is running.';
    } else {
      error.value = 'Failed to save character: ' + (err.message || 'Unknown error');
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
  padding-top: 0; /* Kein Padding mehr, da Titel über dem Bild */
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

/* Neue Styling für die Beschreibung unter dem Bild */
.character-description {
  text-align: center;
  margin: 0 auto 16px;
  max-width: 80%;
}

.description-text {
  font-size: 0.95rem;
  color: rgba(0, 0, 0, 0.7);
  font-style: italic;
  line-height: 1.4;
}

.card-header {
  padding: 16px 24px;
  flex-grow: 1;
}

.level-text {
  font-size: 16px;
  color: #6890F0;
  font-weight: 600;
  margin-bottom: 16px;
  text-align: center;
}

.fantasy-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-section {
  margin-bottom: 8px;
}

.generate-btn {
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  text-transform: none;
  font-weight: 500;
  padding: 0 24px;
  min-width: 200px;
  height: 44px;
}

.generate-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.15);
}

.loading-status {
  border-radius: 12px;
  background-color: white !important;
  position: relative;
  z-index: 5;
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
  
  .character-description {
    max-width: 95%;
    margin-bottom: 12px;
  }
  
  .description-text {
    font-size: 0.9rem;
  }
  
  .fantasy-footer {
    flex-direction: column;
    gap: 16px;
  }
}
</style>