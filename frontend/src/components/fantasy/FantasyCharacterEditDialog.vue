<template>
  <v-dialog 
    v-model="dialogVisible" 
    max-width="800px"
    content-class="bg-transparent shadow-none"
  >
    <v-card 
      class="mx-auto overflow-hidden position-relative d-flex flex-column justify-space-between rounded-xl" 
      :class="{'has-image': character.imageUrl}"
      width="100%" 
      max-width="600px"
      min-height="520px"
      elevation="8"
    >
      <!-- Title first - ganz oben -->
      <v-card-title class="text-white text-center text-h5position-relative bg-gradient-primary py-4 px-4">
        Bearbeiten
      </v-card-title>

      <!-- Close-Button nach rechts oben positioniert -->
      <v-btn
        icon
        @click="closeDialog"
        class="text-white position-absolute close-btn"
        size="small"
        variant="text"
        style="top: 8px; right: 8px; z-index: 30; background-color: rgba(255, 255, 255, 0.2);"
      >
        <v-icon>mdi-close</v-icon>
      </v-btn>
      
      <div class="content-wrapper px-6 flex-1 overflow-y-auto" style="max-height: calc(80vh - 140px);">
        <!-- Schwebendes Bild etwas nach unten versetzt mit Edit-Option -->
        <div class="position-relative mx-auto my-5" style="width: 240px; height: 240px;">
          <v-img
            v-if="character.imageUrl || previewImageUrl"
            :src="previewImageUrl || character.imageUrl"
            class="rounded floating-image"
            contain
            width="240"
            height="240"
            style="box-shadow: 0 8px 16px rgba(0, 0, 0, 0.2);"
          ></v-img>
          
          <!-- Bild-Bearbeiten-Button -->
          <v-btn
            icon
            color="white"
            variant="tonal"
            size="small"
            class="position-absolute"
            style="right: 8px; bottom: 8px; background-color: rgba(0, 0, 0, 0.5);"
            @click="triggerFileInput"
          >
            <v-icon>mdi-camera</v-icon>
          </v-btn>
        </div>
        
        <!-- Versteckter File Input -->
        <v-file-input
          ref="fileInput"
          v-model="imageFile"
          class="d-none"
          accept="image/*"
          @update:model-value="handleImageFileChange"
        ></v-file-input>
        
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

            <!-- Public/Private Checkbox -->
            <div class="mb-3">
              <v-checkbox
                v-model="editedIsPublic"
                label="Öffentlich sichtbar"
                hint="Legt fest, ob andere Benutzer diesen Charakter sehen können"
                persistent-hint
                density="compact"
                :disabled="isSaving"
              ></v-checkbox>
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
      <div class="bg-gradient-primary d-flex justify-center align-center py-4 px-6 text-white footer">
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
            class="save-btn"
            :loading="isSaving"
            style="background-color: rgba(255, 255, 255, 0.2) !important;"
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
import { API_ENDPOINTS } from '../../utils/constants';
import { eventBus } from '../../utils/eventBus';
import type { FantasyCharacter } from '../../types/pokemon';

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
const editedIsPublic = ref(true);
const error = ref('');
const isSaving = ref(false);
const imageFile = ref<File | null>(null);
const previewImageUrl = ref<string | null>(null);
const fileInput = ref<HTMLInputElement | null>(null);

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
    editedIsPublic.value = newVal.isPublic !== undefined ? newVal.isPublic : true;
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
    editedIsPublic.value = props.character.isPublic !== undefined ? props.character.isPublic : true;
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
    let updatedImageUrl = props.character.imageUrl;
    
    // First, if we have a new image, upload it separately
    if (imageFile.value) {
      const imageFormData = new FormData();
      imageFormData.append('image', imageFile.value);
      
      try {
        const imageResponse = await axios.post(API_ENDPOINTS.UPLOAD_IMAGE, imageFormData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        });
        
        if (imageResponse.data && imageResponse.data.imageUrl) {
          updatedImageUrl = imageResponse.data.imageUrl;
        }
      } catch (imageErr) {
        console.error('Error uploading image:', imageErr);
        error.value = 'Failed to upload image. Please try again.';
        isSaving.value = false;
        return;
      }
    }
    
    // Now update the character with regular JSON data and the new image URL
    const characterData = {
      name: nameToSave,
      prompt: editedPrompt.value,
      baseAnimal: editedBaseAnimal.value,
      elementType: editedElementType.value,
      imageUrl: updatedImageUrl,
      is_public: editedIsPublic.value,
      description: editedPrompt.value
    };

    const response = await axios.put(
      `${API_ENDPOINTS.FANTASY_CHARACTERS}/${props.character.id}`, 
      characterData
    );

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

// Trigger file input click
const triggerFileInput = () => {
  const input = fileInput.value;
  if (input) {
    input.click();
  }
};

// Handle image file change
const handleImageFileChange = (file: File) => {
  if (file) {
    imageFile.value = file;
    const reader = new FileReader();
    reader.onload = (e: any) => {
      previewImageUrl.value = e.target.result;
    };
    reader.readAsDataURL(file);
  } else {
    imageFile.value = null;
    previewImageUrl.value = null;
  }
};
</script>

<style>
/* Benutzerdefinierte Klassen für die Komponentendarstellung */
.bg-gradient-primary {
  background: linear-gradient(135deg, #6890F0 0%, #705898 100%);
  z-index: 25;
}

.has-image {
  padding-top: 0;
}

.content-wrapper {
  max-height: calc(80vh - 140px);
}

.close-btn:hover {
  background-color: rgba(255, 255, 255, 0.3) !important;
}

.footer {
  position: sticky;
  bottom: 0;
  z-index: 10;
  transition: background-color 0.5s ease;
}

@media (max-width: 600px) {
  .content-wrapper {
    max-height: calc(100vh - 140px);
    padding: 0 16px;
  }

  .floating-image {
    width: 180px !important;
    height: 180px !important;
  }
  
  .footer {
    flex-direction: column;
    gap: 16px;
  }
}
</style>