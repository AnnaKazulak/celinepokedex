<template>
  <div class="pa-4 fantasy-tab-container">
    <!-- Image display -->
    <div v-if="manualImageUrl" class="d-flex justify-center my-5">
      <v-img
        :src="manualImageUrl"
        max-width="240"
        max-height="240"
        class="rounded-lg elevation-8"
        contain
      ></v-img>
    </div>
    
    <v-form @submit.prevent="onSaveCharacter" class="mt-2">
      <!-- Character Name Input -->
      <v-text-field
        v-model="characterName"
        label="Name des Charakters"
        variant="underlined"
        density="compact"
        required
        :rules="[v => !!v || 'Name wird benötigt']"
      ></v-text-field>

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
      
      <!-- Image Upload -->
      <v-card class="mt-4 bg-grey-lighten-5" variant="outlined" rounded="lg" flat>
        <v-card-text>
          <v-file-input
            v-model="imageFile"
            label="Bild hochladen"
            accept="image/*"
            variant="underlined"
            density="compact"
            prepend-icon="mdi-camera"
            :rules="[v => !!manualImageUrl || !!v || 'Bild wird benötigt']"
            @update:model-value="handleImageFileChange"
          ></v-file-input>

          <div class="text-caption mt-2">
            Unterstützt werden JPG, PNG, GIF und WebP. Maximal 5MB.
          </div>
        </v-card-text>
      </v-card>
      
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
}>();

// Emits
const emit = defineEmits<{
  'save-character': [];
  'update:characterName': [value: string];
  'update:characterDescription': [value: string];
  'update:manualImageUrl': [value: string];
  'update:imageFile': [value: File | null];
  'update:error': [value: string];
}>();

// Local state
const characterName = ref(props.characterName);
const characterDescription = ref(props.characterDescription);
const imageFile = ref<File | null>(null);

// Watch for prop changes
watch(() => props.characterName, (newVal) => {
  characterName.value = newVal;
});

watch(() => props.characterDescription, (newVal) => {
  characterDescription.value = newVal;
});

// Watch for local changes and update parent
watch(characterName, (newVal) => {
  emit('update:characterName', newVal);
});

watch(characterDescription, (newVal) => {
  emit('update:characterDescription', newVal);
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
</style>