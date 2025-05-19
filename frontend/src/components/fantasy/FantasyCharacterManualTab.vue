<template>
  <div class="pa-4 fantasy-tab-container">
    <v-row>
      <v-col cols="12" md="6">
        <v-form @submit.prevent="onSaveCharacter">
          <!-- Character Name Input -->
          <v-text-field
            v-model="characterName"
            label="Name des Charakters"
            variant="underlined"
            density="compact"
            required
            :rules="[v => !!v || 'Name wird benötigt']"
          ></v-text-field>

          <!-- Base Animal select -->
          <v-select
            v-model="baseAnimal"
            :items="baseAnimalOptions"
            label="Base Animal"
            variant="underlined"
            density="compact"
            class="mb-4"
            @update:model-value="$emit('update:baseAnimal', baseAnimal)"
          ></v-select>
          
          <!-- Element Type select -->
          <v-select
            v-model="elementType"
            :items="elementTypeOptions"
            label="Element Type"
            variant="underlined"
            density="compact"
            class="mb-4"
            @update:model-value="$emit('update:elementType', elementType)"
          ></v-select>

          <!-- Character Description Input -->
          <v-textarea
            v-model="characterDescription"
            label="Beschreibe deinen Fantasy-Charakter"
            rows="3"
            auto-grow
            variant="underlined"
            density="compact"
            required
            :rules="[v => !!v || 'Beschreibung wird benötigt']"
          ></v-textarea>
          
          <!-- Public Character Checkbox -->
          <v-checkbox
            v-model="isPublic"
            label="Öffentlich sichtbar"
            hint="Legt fest, ob andere Benutzer diesen Charakter sehen können"
            persistent-hint
            density="compact"
            class="mb-4"
            @update:model-value="$emit('update:isPublic', isPublic)"
          ></v-checkbox>
          
          <div class="d-flex justify-center mt-4">
            <v-btn
              color="primary"
              min-width="200"
              height="44"
              type="submit"
              :disabled="!isFormValid || isSaving"
              :loading="isSaving"
              class="text-none font-weight-medium elevation-2"
            >
              Charakter speichern
            </v-btn>
          </div>

          <!-- Error message -->
          <div v-if="error" class="mt-4">
            <v-alert type="error" variant="tonal" closable>
              {{ error }}
            </v-alert>
          </div>
        </v-form>
      </v-col>
      
      <v-col cols="12" md="6" class="d-flex flex-column align-center">
        <!-- Image Preview Section -->
        <div v-if="manualImageUrl" class="text-center w-100 mb-4">
          <v-img
            :src="manualImageUrl"
            max-width="240"
            max-height="240"
            class="rounded-lg elevation-8 mx-auto mb-3"
            contain
          ></v-img>
          <p v-if="characterName" class="text-caption mt-2">{{ characterName }}</p>
        </div>
        
        <div v-else class="text-center w-100 mb-4">
          <v-icon size="64" color="grey-lighten-1">mdi-image-outline</v-icon>
          <div class="mt-2 text-medium-emphasis">Bild hochladen, um deinen Charakter zu erstellen</div>
        </div>

        <!-- Upload Section - Now positioned below the image -->
        <v-card class="bg-grey-lighten-5 upload-zone w-100" variant="outlined" rounded="lg" flat>
          <v-card-text class="pa-2">
            <div 
              class="dropzone-container compact" 
              @dragover.prevent="isDragging = true"
              @dragleave.prevent="isDragging = false"
              @drop.prevent="handleFileDrop"
              :class="{ 'dropzone-dragging': isDragging }"
            >
              <div class="upload-content text-center">
                <v-icon size="32" class="mb-1" :color="manualImageUrl ? 'primary' : 'grey-lighten-1'">
                  {{ manualImageUrl ? 'mdi-check-circle' : 'mdi-cloud-upload' }}
                </v-icon>
                
                <div v-if="!manualImageUrl" class="upload-text">
                  <div class="text-body-2 font-weight-medium">Bild hierher ziehen</div>
                  <div class="text-caption text-medium-emphasis">oder</div>
                  <v-btn 
                    class="mt-1" 
                    color="primary" 
                    variant="tonal"
                    size="small"
                    density="comfortable"
                    @click="triggerFileInput"
                  >
                    Bild auswählen
                  </v-btn>
                </div>
                
                <div v-else class="upload-text">
                  <div class="text-caption">{{ imageFile?.name || 'Bild hochgeladen' }}</div>
                  <v-btn
                    class="mt-1" 
                    color="grey-darken-1" 
                    size="x-small"
                    variant="text"
                    @click="resetImage"
                  >
                    Ändern
                  </v-btn>
                </div>

                <!-- Versteckter File Input -->
                <v-file-input
                  ref="fileInput"
                  v-model="imageFile"
                  class="d-none"
                  accept="image/*"
                  :rules="[v => !!manualImageUrl || !!v || 'Bild wird benötigt']"
                  @update:model-value="handleImageFileChange"
                ></v-file-input>
              </div>
            </div>

            <div class="text-caption mt-1 ps-1">
              <v-icon size="x-small" class="me-1">mdi-information-outline</v-icon>
              JPG, PNG, GIF, WebP. Max 5MB.
            </div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

    <!-- Spacer to maintain minimum height -->
    <div class="flex-grow-1 min-height-spacer"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, defineProps, defineEmits, watch } from 'vue';

// Props
const props = defineProps<{
  characterName: string;
  characterDescription: string;
  manualImageUrl: string;
  isSaving: boolean;
  error: string;
  baseAnimal?: string;
  elementType?: string;
  baseAnimalOptions?: string[];
  elementTypeOptions?: string[];
  isPublic?: boolean;
}>();

// Emits
const emit = defineEmits<{
  'save-character': [];
  'update:characterName': [value: string];
  'update:characterDescription': [value: string];
  'update:manualImageUrl': [value: string];
  'update:imageFile': [value: File | null];
  'update:error': [value: string];
  'update:baseAnimal': [value: string];
  'update:elementType': [value: string];
  'update:isPublic': [value: boolean];
}>();

// Local state
const characterName = ref(props.characterName);
const characterDescription = ref(props.characterDescription);
const baseAnimal = ref(props.baseAnimal || '');
const elementType = ref(props.elementType || '');
const isPublic = ref(props.isPublic !== undefined ? props.isPublic : true);
const imageFile = ref<File | null>(null);
const isDragging = ref(false);

// Default options for selects if not provided
const baseAnimalOptions = props.baseAnimalOptions || [
  'CAT', 'LIZARD', 'BIRD', 
  'FROG', 'FOX', 'SNAKE', 'HORSE', 'TURTLE',
  'LION', 'EAGLE', 'DEER'
];

const elementTypeOptions = props.elementTypeOptions || [
  'FIRE', 'WATER', 'EARTH', 'WIND', 'ELECTRIC', 
  'ICE', 'NATURE', 'SHADOW', 'LIGHT', 'POISON'
];

// Watch for prop changes
watch(() => props.characterName, (newVal) => {
  characterName.value = newVal;
});

watch(() => props.characterDescription, (newVal) => {
  characterDescription.value = newVal;
});

watch(() => props.baseAnimal, (newVal) => {
  if (newVal !== undefined) baseAnimal.value = newVal;
});

watch(() => props.elementType, (newVal) => {
  if (newVal !== undefined) elementType.value = newVal;
});

watch(() => props.isPublic, (newVal) => {
  if (newVal !== undefined) isPublic.value = newVal;
});

// Watch for local changes and update parent
watch(characterName, (newVal) => {
  emit('update:characterName', newVal);
});

watch(characterDescription, (newVal) => {
  emit('update:characterDescription', newVal);
});

watch(baseAnimal, (newVal) => {
  emit('update:baseAnimal', newVal);
});

watch(elementType, (newVal) => {
  emit('update:elementType', newVal);
});

watch(isPublic, (newVal) => {
  emit('update:isPublic', newVal);
});

// Computed properties
const isFormValid = computed(() => {
  return !!characterName.value && 
         !!characterDescription.value && 
         (!!imageFile.value || !!props.manualImageUrl);
});

// Methods
const handleImageFileChange = (file: File | null) => {
  if (!file) {
    emit('update:manualImageUrl', '');
    return;
  }
  
  // Check file size (5MB limit)
  const maxSize = 5 * 1024 * 1024; // 5MB in bytes
  if (file.size > maxSize) {
    emit('update:error', 'Die Datei ist zu groß. Maximale Größe: 5MB');
    imageFile.value = null;
    return;
  }
  
  // Create URL for preview
  emit('update:manualImageUrl', URL.createObjectURL(file));
  emit('update:imageFile', file);
};

const handleFileDrop = (event: DragEvent) => {
  const files = event.dataTransfer?.files;
  if (files && files.length > 0) {
    handleImageFileChange(files[0]);
  }
  isDragging.value = false;
};

const triggerFileInput = () => {
  const fileInput = document.querySelector<HTMLInputElement>('.upload-zone input[type="file"]');
  if (fileInput) {
    fileInput.click();
  }
};

const resetImage = () => {
  imageFile.value = null;
  emit('update:manualImageUrl', '');
};

const onSaveCharacter = () => {
  if (!isFormValid.value || props.isSaving) return;
  emit('save-character');
};
</script>

<style scoped>
.fantasy-tab-container {
  display: flex;
  flex-direction: column;
  min-height: 500px;
}

.min-height-spacer {
  min-height: 20px;
}

.upload-zone {
  position: relative;
  overflow: hidden;
}

.dropzone-container {
  border: 2px dashed var(--v-theme-on-surface);
  border-radius: var(--v-border-radius-lg);
  padding: 20px;
  transition: background-color 0.3s ease;
}

.dropzone-container.compact {
  padding: 12px 8px;
  min-height: 120px;
}

.dropzone-dragging {
  background-color: var(--v-theme-primary-lighten-5);
}

.upload-content {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.upload-text {
  margin-top: 10px;
}

.compact .upload-text {
  margin-top: 4px;
}
</style>