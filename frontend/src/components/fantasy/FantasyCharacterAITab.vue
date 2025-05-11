<template>
  <div class="pa-4 fantasy-tab-container">
    <v-row>
      <v-col cols="12" md="6">
        <v-form @submit.prevent="onGenerateImage" v-if="!isGenerating || !generatedImageUrl">
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

          <!-- Error message display -->
          <div v-if="error" class="mt-4">
            <v-alert type="error" variant="tonal" closable>
              {{ error }}
            </v-alert>
          </div>
        </v-form>
      </v-col>
      
      <v-col cols="12" md="6" class="d-flex align-center">
        <div v-if="isGenerating" class="text-center w-100">
          <!-- Loading indicator -->
          <fantasy-loading-indicator 
            :loading-step="loadingStep" 
            class="mt-4 mb-4"
          />
        </div>
        
        <div v-else-if="generatedImageUrl" class="text-center w-100">
          <v-img
            :src="generatedImageUrl"
            max-width="240"
            max-height="240"
            class="rounded-lg elevation-8 mx-auto mb-3"
            contain
          ></v-img>
          <p class="text-caption mt-2">{{ prompt }}</p>
        </div>
        
        <div v-else class="text-center w-100">
          <v-icon size="64" color="grey-lighten-1">mdi-image-outline</v-icon>
          <div class="mt-2 text-medium-emphasis">Beschreibe deinen Charakter, um ihn zu generieren</div>
        </div>
      </v-col>
    </v-row>

    <!-- Spacer to maintain minimum height -->
    <div class="flex-grow-1 min-height-spacer"></div>
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