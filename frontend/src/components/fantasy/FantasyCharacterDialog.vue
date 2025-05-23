<template>
  <v-dialog 
    v-model="dialogVisible" 
    max-width="800"
    min-height="700"
    content-class="fantasy-dialog-wrapper"
  >
    <v-card class="rounded-lg overflow-hidden" :class="{'has-image': shouldShowImage}">
      <!-- Title header -->
      <v-card-title class="text-center text-white py-4 bg-gradient">
        Create Fantasy Character
        
        <v-btn
          icon
          variant="text"
          color="white"
          @click="closeDialog"
          size="small"
          class="dialog-close-btn"
        >
          <v-icon>mdi-close</v-icon>
        </v-btn>
      </v-card-title>
      
      <!-- Tabs for selection -->
      <v-tabs
        v-model="activeTab"
        centered
        color="primary"
        class="bg-primary-lighten-4"
      >
        <v-tab value="ai">
          <v-icon>mdi-robot</v-icon>
          <span class="d-none d-md-inline ml-2">Ki-generierung</span>
        </v-tab>
        <v-tab value="manual">
          <v-icon>mdi-pencil</v-icon>
          <span class="d-none d-md-inline ml-2">Manuell erstellen</span>
        </v-tab>
        <v-tab value="generator">
          <v-icon>mdi-creation-outline</v-icon>
          <span class="d-none d-md-inline ml-2">Generator</span>
        </v-tab>
      </v-tabs>

      <!-- Tab content -->
      <v-window v-model="activeTab" class="card-content">
        <!-- AI Generation Tab -->
        <v-window-item value="ai">
          <fantasy-character-a-i-tab
            :prompt="prompt"
            :character-name="characterName"
            :base-animal="selectedBaseAnimal"
            :element-type="selectedElementType"
            :is-generating="isGenerating"
            :generated-image-url="generatedImageUrl"
            :error="error"
            :loading-step="loadingStep"
            :base-animal-options="baseAnimalOptions"
            :element-type-options="elementTypeOptions"
            @update:prompt="prompt = $event"
            @update:error="error = $event"
            @update:character-name="characterName = $event"
            @update:base-animal="selectedBaseAnimal = $event"
            @update:element-type="selectedElementType = $event"
            @generate="generateImage"
          />
        </v-window-item>

        <!-- Manual Creation Tab -->
        <v-window-item value="manual">
          <fantasy-character-manual-tab
            :character-name="manualCharacterName"
            :character-description="manualCharacterDescription"
            :manual-image-url="manualImageUrl"
            :is-saving="isManualSaving"
            :error="manualError"
            :base-animal="manualBaseAnimal"
            :element-type="manualElementType"
            :is-public="manualIsPublic"
            :base-animal-options="baseAnimalOptions"
            :element-type-options="elementTypeOptions"
            @update:characterName="manualCharacterName = $event"
            @update:characterDescription="manualCharacterDescription = $event"
            @update:manualImageUrl="manualImageUrl = $event"
            @update:imageFile="imageFile = $event"
            @update:error="manualError = $event"
            @update:baseAnimal="manualBaseAnimal = $event"
            @update:elementType="manualElementType = $event"
            @update:isPublic="manualIsPublic = $event"
            @save-character="saveManualCharacter"
          />
        </v-window-item>
        
        <!-- Generator Tab -->
        <v-window-item value="generator">
          <fantasy-character-generator-tab
            :base-animal="generatorBaseAnimal"
            :element-type="generatorElementType"
            :dominant-color="generatorDominantColor"
            :style-type="generatorStyleType"
            :traits="generatorTraits"
            :image-data="generatorImageData"
            :generated-prompt="generatorGeneratedPrompt"
            :is-generating="isGeneratorGenerating"
            :base-animal-options="baseAnimalOptions"
            :element-type-options="elementTypeOptions"
            :color-options="colorOptions"
            :style-type-options="styleTypeOptions"
            :character-trait-options="characterTraitOptions"
            @update:base-animal="generatorBaseAnimal = $event"
            @update:element-type="generatorElementType = $event"
            @update:dominant-color="generatorDominantColor = $event"
            @update:style-type="generatorStyleType = $event"
            @update:traits="generatorTraits = $event"
            @generate-character="generateGeneratorCharacter"
          />
        </v-window-item>
      </v-window>
      
      <!-- AI tab footer - Always show footer for this tab -->
      <fantasy-character-footer 
        v-if="activeTab === 'ai'"
        :reset-label="'Neuer Charakter'"
        :show-save="true"
        :show-download="!!generatedImageUrl"
        :save-disabled="!generatedImageUrl"
        :is-saving="isSaving"
        @reset="resetForm"
        @save="saveCharacter"
        @download="downloadImage"
      />

      <!-- Manual tab footer - Always show footer for this tab -->
      <fantasy-character-footer
        v-if="activeTab === 'manual'"
        :save-disabled="!manualCharacterName || !manualCharacterDescription || (!manualImageUrl && !imageFile)"
        :is-saving="isManualSaving"
        @reset="resetManualForm"
        @save="saveManualCharacter"
      />

      <!-- Generator tab footer - Always show footer for this tab -->
      <fantasy-character-footer
        v-if="activeTab === 'generator'"
        :save-disabled="!generatorImageData"
        :is-saving="isSavingGenerator"
        @reset="resetGeneratorForm"
        @save="saveGeneratorCharacter"
      />
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue';
import { useRouter } from 'vue-router'; 
import axios from 'axios';
import { API_ENDPOINTS, FANTASY_CHARACTER_OPTIONS, FANTASY_TRANSFORMATIONS } from '../../utils/constants';
import { eventBus } from '../../utils/eventBus';
import type { FantasyCharacter } from '../../types/pokemon';
import FantasyCharacterAITab from './FantasyCharacterAITab.vue';
import FantasyCharacterManualTab from './FantasyCharacterManualTab.vue';
import FantasyCharacterGeneratorTab from './FantasyCharacterGeneratorTab.vue';
import FantasyCharacterFooter from './FantasyCharacterFooter.vue';

// Router
const router = useRouter();

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
const activeTab = ref('ai'); // Default to AI generation tab

// AI generation variables
const prompt = ref('');
const characterName = ref('');
const selectedBaseAnimal = ref('');
const selectedElementType = ref('');
const isGenerating = ref(false);
const generatedImageUrl = ref('');
const error = ref('');
const loadingStep = ref(0);
const isSaving = ref(false);

// Manual creation variables
const manualCharacterName = ref('');
const manualCharacterDescription = ref('');
const imageFile = ref<File | null>(null);
const manualImageUrl = ref('');
const isManualSaving = ref(false);
const manualError = ref('');
const manualBaseAnimal = ref('');
const manualElementType = ref('');
const manualIsPublic = ref(true);

// Generator variables
const generatorBaseAnimal = ref('');
const generatorElementType = ref('');
const generatorDominantColor = ref('');
const generatorStyleType = ref('');
const generatorTraits = ref<string[]>([]);
const isGeneratorGenerating = ref(false);
const generatorImageData = ref('');
const generatorGeneratedPrompt = ref('');
const isSavingGenerator = ref(false);

// Options für die Dropdowns - Zugriff auf die zentralen Konstanten
const baseAnimalOptions = FANTASY_CHARACTER_OPTIONS.BASE_ANIMALS;
const elementTypeOptions = FANTASY_CHARACTER_OPTIONS.ELEMENT_TYPES;
const colorOptions = FANTASY_CHARACTER_OPTIONS.COLORS;
const styleTypeOptions = FANTASY_CHARACTER_OPTIONS.STYLE_TYPES;
const characterTraitOptions = FANTASY_CHARACTER_OPTIONS.CHARACTER_TRAITS;

// Fantasy transformations map
const fantasyTransformations: Record<string, string> = {
  'CAT': 'Bakeneko (mystical cat)',
  'LIZARD': 'Dragon',
  'BIRD': 'Phoenix',
  'FROG': 'Frog Prince',
  'FOX': 'Kitsune (nine-tailed fox)',
  'SNAKE': 'Naga (half-snake, half-human)',
  'HORSE': 'Pegasus (winged horse)',
  'TURTLE': 'Koopa (Mario-like turtle)',
  'LION': 'Manticore (lion with scorpion tail)',
  'EAGLE': 'Griffin (eagle-lion hybrid)',
  'DEER': 'Celestial Deer (glowing antlers)'
};

// Computed properties
const shouldShowImage = computed(() => {
  return generatedImageUrl.value || 
         (activeTab.value === 'manual' && manualImageUrl.value) ||
         (activeTab.value === 'generator' && generatorImageData.value);
});

// Watch for dialog prop changes
watch(() => props.dialog, (newVal) => {
  dialogVisible.value = newVal;
});

// Watch for dialog changes and emit updates
watch(dialogVisible, (newVal) => {
  emit('update:dialog', newVal);
});

// Methods
const closeDialog = () => {
  dialogVisible.value = false;
  resetForm();
  resetManualForm();
  resetGeneratorForm();
};

const resetForm = () => {
  prompt.value = '';
  characterName.value = '';
  selectedBaseAnimal.value = '';
  selectedElementType.value = '';
  generatedImageUrl.value = '';
  error.value = '';
  loadingStep.value = 0;
};

const resetManualForm = () => {
  manualCharacterName.value = '';
  manualCharacterDescription.value = '';
  imageFile.value = null;
  manualImageUrl.value = '';
  manualError.value = '';
  manualBaseAnimal.value = '';
  manualElementType.value = '';
  manualIsPublic.value = true;
};

const resetGeneratorForm = () => {
  generatorBaseAnimal.value = '';
  generatorElementType.value = '';
  generatorDominantColor.value = '';
  generatorStyleType.value = '';
  generatorTraits.value = [];
  generatorImageData.value = '';
  generatorGeneratedPrompt.value = '';
};

// Enhance prompt with base animal and element type if selected
const getEnhancedPrompt = () => {
  let enhancedPrompt = prompt.value;
  
  // Add base animal info if selected
  if (selectedBaseAnimal.value) {
    const fantasyCreature = fantasyTransformations[selectedBaseAnimal.value] || selectedBaseAnimal.value;
    if (!enhancedPrompt.toLowerCase().includes(fantasyCreature.toLowerCase())) {
      enhancedPrompt = `A ${fantasyCreature.toLowerCase()}, with ${enhancedPrompt}`;
    }
  }
  
  // Add element type if selected
  if (selectedElementType.value && !enhancedPrompt.toLowerCase().includes(selectedElementType.value.toLowerCase())) {
    enhancedPrompt += ` with ${selectedElementType.value.toLowerCase()} powers`;
  }
  
  return enhancedPrompt;
};

// AI Tab methods
const generateImage = async () => {
  if (!prompt.value || isGenerating.value) return;
  
  isGenerating.value = true;
  error.value = '';
  generatedImageUrl.value = '';
  loadingStep.value = 0;
  
  // Enhance the prompt with selected animal and element type
  const enhancedPrompt = getEnhancedPrompt();
  
  // Simulate progress animation for loading steps
  const loadingInterval = setInterval(() => {
    if (loadingStep.value < 4) {
      loadingStep.value++;
    }
  }, 3000);
  
  // Maximum number of retry attempts
  const maxRetries = 2;
  let retryCount = 0;
  let success = false;
  
  while (retryCount <= maxRetries && !success) {
    try {
      // First step is triggered immediately
      loadingStep.value = 1;
      
      if (retryCount > 0) {
        console.log(`Retry attempt ${retryCount} of ${maxRetries}`);
      }
      
      const response = await axios.post(API_ENDPOINTS.GENERATE_IMAGE, {
        prompt: enhancedPrompt
      }, {
        // Increased timeout for axios request (60 seconds)
        timeout: 60000
      });
      
      if (response.data && response.data.imageUrl) {
        generatedImageUrl.value = response.data.imageUrl;
        success = true;
      } else {
        throw new Error('No image URL returned from server');
      }
    } catch (err: unknown) {
      console.error('Error generating image:', err);
      
      // If we've reached max retries, show error to user
      if (retryCount === maxRetries) {
        // Type guard to check if err is an axios error
        if (axios.isAxiosError(err)) {
          if (err.response) {
            if (err.response.status === 504) {
              error.value = `Gateway Timeout Error: The image generation server is taking too long to respond. Please try a simpler description or try again later.`;
            } else if (err.response.data && typeof err.response.data === 'object' && 'error' in err.response.data) {
              error.value = `Error: ${err.response.data.error}`;
            } else {
              error.value = `Server error (${err.response.status}): Please try again later`;
            }
          } else if (err.request) {
            error.value = 'No response from server. Please check if the backend is running.';
          } else {
            error.value = 'Failed to generate image: ' + (err.message || 'Unknown error');
          }
        } else {
          // Handle non-Axios errors
          error.value = 'Failed to generate image: ' + ((err instanceof Error) ? err.message : 'Unknown error');
        }
      } else {
        retryCount++;
        await new Promise(resolve => setTimeout(resolve, 2000));
      }
    }
  }
  
  clearInterval(loadingInterval);
  loadingStep.value = 4; // Set to the last step when done
  isGenerating.value = false;
};

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

const saveCharacter = async () => {
  if (!generatedImageUrl.value || isSaving.value) return;

  isSaving.value = true;
  error.value = '';

  try {
    const response = await axios.post(API_ENDPOINTS.SAVE_CHARACTER, {
      name: characterName.value || 'Fantasy Character',
      baseAnimal: selectedBaseAnimal.value || null,
      elementType: selectedElementType.value || null,
      prompt: prompt.value,
      imageUrl: generatedImageUrl.value,
      is_public: true,
      description: prompt.value
    });

    if (response.data) {
      const savedCharacter = response.data;
      
      dialogVisible.value = false;
      eventBus.emit('fantasy-character-created', savedCharacter);
      
      if (savedCharacter.id) {
        router.push({
          name: 'fantasyCharacterDetail',
          params: { id: savedCharacter.id }
        });
      }
      
      setTimeout(() => {
        resetForm();
      }, 300);
    } else {
      throw new Error('Failed to save character');
    }
  } catch (err: unknown) {
    console.error('Error saving character:', err);

    if (axios.isAxiosError(err)) {
      if (err.response) {
        if (err.response.data && typeof err.response.data === 'object' && 'error' in err.response.data) {
          error.value = `Error: ${err.response.data.error}`;
        } else {
          error.value = `Server error (${err.response.status}): Please try again later`;
        }
      } else if (err.request) {
        error.value = 'No response from server. Please check if the backend is running.';
      } else {
        error.value = 'Failed to save character: ' + (err.message || 'Unknown error');
      }
    } else {
      error.value = 'Failed to save character: ' + ((err instanceof Error) ? err.message : 'Unknown error');
    }
  } finally {
    isSaving.value = false;
  }
};

// Manual Tab methods
const saveManualCharacter = async () => {
  if (isManualSaving.value) return;
  
  isManualSaving.value = true;
  manualError.value = '';
  
  try {
    let finalImageUrl = manualImageUrl.value;
    
    // If we have a file, we need to upload it first
    if (imageFile.value) {
      const formData = new FormData();
      formData.append('image', imageFile.value);
      
      // Upload image first
      const uploadResponse = await axios.post(API_ENDPOINTS.UPLOAD_IMAGE, formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      });
      
      if (uploadResponse.data && uploadResponse.data.imageUrl) {
        finalImageUrl = uploadResponse.data.imageUrl;
      } else {
        throw new Error('Failed to upload image');
      }
    }
    
    // Now save the character
    const response = await axios.post(API_ENDPOINTS.SAVE_CHARACTER, {
      name: manualCharacterName.value,
      prompt: manualCharacterDescription.value,
      imageUrl: finalImageUrl,
      baseAnimal: manualBaseAnimal.value,
      elementType: manualElementType.value,
      is_public: manualIsPublic.value,
      description: manualCharacterDescription.value
    });
    
    if (response.data) {
      const savedCharacter = response.data;
      
      dialogVisible.value = false;
      eventBus.emit('fantasy-character-created', savedCharacter);
      
      if (savedCharacter.id) {
        router.push({
          name: 'fantasyCharacterDetail',
          params: { id: savedCharacter.id }
        });
      }
      
      setTimeout(() => {
        resetManualForm();
      }, 300);
    } else {
      throw new Error('Failed to save character');
    }
  } catch (err: unknown) {
    console.error('Error saving manual character:', err);
    
    if (axios.isAxiosError(err)) {
      if (err.response) {
        if (err.response.data && typeof err.response.data === 'object' && 'error' in err.response.data) {
          manualError.value = `Error: ${err.response.data.error}`;
        } else {
          manualError.value = `Server error (${err.response.status}): Please try again later`;
        }
      } else if (err.request) {
        manualError.value = 'No response from server. Please check if the backend is running.';
      } else {
        manualError.value = 'Failed to save character: ' + (err.message || 'Unknown error');
      }
    } else {
      manualError.value = 'Failed to save character: ' + ((err instanceof Error) ? err.message : 'Unknown error');
    }
  } finally {
    isManualSaving.value = false;
  }
};

// Generator Tab methods
const generateGeneratorCharacter = async () => {
  isGeneratorGenerating.value = true;
  generatorImageData.value = '';
  generatorGeneratedPrompt.value = '';
  error.value = ''; // Nutze das existierende error Feld für Fehlermeldungen
  
  // Maximum number of retry attempts
  const maxRetries = 2;
  let retryCount = 0;
  let success = false;
  
  while (retryCount <= maxRetries && !success) {
    try {
      if (retryCount > 0) {
        console.log(`Retry attempt ${retryCount} of ${maxRetries}`);
      }
      
      // Transform the base animal into its fantasy version for better AI generation
      const fantasyCreature = fantasyTransformations[generatorBaseAnimal.value] || generatorBaseAnimal.value;
      
      const response = await axios.post(API_ENDPOINTS.GENERATE_CHARACTER, {
        baseAnimal: generatorBaseAnimal.value,
        elementType: generatorElementType.value,
        dominantColor: generatorDominantColor.value,
        styleType: generatorStyleType.value,
        traits: generatorTraits.value,
        fantasyCreature: fantasyCreature
      }, {
        // Increased timeout for axios request (120 seconds)
        timeout: 120000
      });
      
      if (response.data && response.data.imageData) {
        generatorImageData.value = response.data.imageData;
        generatorGeneratedPrompt.value = response.data.prompt;
        success = true;
      } else {
        throw new Error('No image data returned from server');
      }
    } catch (err: unknown) {
      console.error('Error generating character:', err);
      
      // If we've reached max retries, set error message
      if (retryCount === maxRetries) {
        if (axios.isAxiosError(err)) {
          if (err.response) {
            if (err.response.status === 504) {
              error.value = `Gateway Timeout Error: Der Bildgenerierungsserver braucht zu lange. Bitte versuche eine einfachere Konfiguration oder versuche es später noch einmal.`;
            } else if (err.response.data && typeof err.response.data === 'object' && 'error' in err.response.data) {
              error.value = `Error: ${err.response.data.error}`;
            } else {
              error.value = `Server error (${err.response.status}): Bitte versuche es später noch einmal`;
            }
          } else if (err.request) {
            error.value = 'Keine Antwort vom Server. Bitte überprüfe deine Internetverbindung und versuche es erneut.';
          } else if (err.code === 'ECONNABORTED') {
            error.value = 'Zeitüberschreitung bei der Anfrage. Der Server braucht zu lange, um das Bild zu generieren. Bitte versuche es mit einfacheren Optionen.';
          } else {
            error.value = 'Fehler bei der Charaktergenerierung: ' + (err.message || 'Unbekannter Fehler');
          }
        } else {
          error.value = 'Fehler bei der Charaktergenerierung: ' + ((err instanceof Error) ? err.message : 'Unbekannter Fehler');
        }
      } else {
        retryCount++;
        await new Promise(resolve => setTimeout(resolve, 2000));
      }
    }
  }
  
  isGeneratorGenerating.value = false;
};

const saveGeneratorCharacter = async () => {
  if (!generatorImageData.value || isSavingGenerator.value) return;

  isSavingGenerator.value = true;

  try {
    const response = await axios.post(API_ENDPOINTS.SAVE_CHARACTER, {
      prompt: generatorGeneratedPrompt.value,
      imageUrl: generatorImageData.value,
      baseAnimal: generatorBaseAnimal.value,
      elementType: generatorElementType.value
    });

    if (response.data) {
      const savedCharacter = response.data;
      
      dialogVisible.value = false;
      eventBus.emit('fantasy-character-created', savedCharacter);
      
      if (savedCharacter.id) {
        router.push({
          name: 'fantasyCharacterDetail',
          params: { id: savedCharacter.id }
        });
      }
      
      setTimeout(() => {
        resetGeneratorForm();
      }, 300);
    } else {
      throw new Error('Failed to save character');
    }
  } catch (err: unknown) {
    console.error('Error saving character:', err);
  } finally {
    isSavingGenerator.value = false;
  }
};
</script>

<style scoped>
.fantasy-dialog-wrapper {
  box-shadow: none;
  background: transparent;
}

.bg-gradient {
  background: linear-gradient(135deg, #6890F0 0%, #705898 100%);
}

.dialog-close-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  z-index: 30;
}

.card-content {
  flex-grow: 1;
  overflow-y: auto;
  overflow-x: hidden;
  min-height: 500px; /* Consistent height across all tabs */
  display: flex;
  flex-direction: column;
}

/* Make v-window fill the entire container height */
:deep(.v-window) {
  height: 100%;
  overflow-x: hidden;
}

:deep(.v-window__container) {
  height: 100%;
  overflow-x: hidden;
}

:deep(.v-window-item) {
  height: 100%;
  overflow-x: hidden;
}

/* Ensure card takes at least the minimum height */
.has-image {
  min-height: 580px;
}
</style>