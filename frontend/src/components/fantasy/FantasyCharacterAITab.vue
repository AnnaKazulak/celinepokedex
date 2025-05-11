<template>
  <div class="pa-4">
    <!-- Floating image display -->
    <div v-if="generatedImageUrl" class="d-flex justify-center my-5">
      <v-img
        :src="generatedImageUrl"
        max-width="240"
        max-height="240"
        class="rounded-lg elevation-8"
        contain
      ></v-img>
    </div>
    
    <!-- Description under the image -->
    <div v-if="generatedImageUrl && !isGenerating" class="text-center mx-auto mb-4" style="max-width: 80%">
      <p class="text-body-2 font-italic text-medium-emphasis">{{ prompt }}</p>
    </div>
    
    <v-form @submit.prevent="onGenerateImage" v-if="!generatedImageUrl || isGenerating">
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
      
      <div class="d-flex justify-center mt-4 mb-4">
        <v-btn 
          color="primary" 
          :loading="isGenerating"
          :disabled="!prompt || isGenerating"
          @click="onGenerateImage"
          min-width="200"
          height="44"
          class="text-none font-weight-medium elevation-2"
        >
          {{ isGenerating ? 'Generiere...' : 'Charakter erstellen' }}
        </v-btn>
      </div>
    </v-form>
    
    <!-- Loading indicator -->
    <fantasy-loading-indicator 
      v-if="isGenerating" 
      :loading-step="loadingStep" 
      class="mt-4 mb-4"
    />
    
    <!-- Error message display -->
    <div v-if="error" class="mt-4">
      <v-alert type="error" variant="tonal" closable>
        {{ error }}
      </v-alert>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, defineEmits } from 'vue';
import FantasyLoadingIndicator from './FantasyLoadingIndicator.vue';

const emit = defineEmits<{
  'generate': [prompt: string];
  'reset': [];
  'save': [];
  'download': [];
  'update:prompt': [value: string];
  'update:error': [value: string];
}>();

// Props
const props = defineProps<{
  prompt: string;
  isGenerating: boolean;
  generatedImageUrl: string;
  error: string;
  loadingStep: number;
}>();

// Local data binding with updates to parent
const prompt = ref(props.prompt);

// Methods
const onGenerateImage = () => {
  if (!prompt.value || props.isGenerating) return;
  emit('update:prompt', prompt.value);
  emit('generate', prompt.value);
};
</script>