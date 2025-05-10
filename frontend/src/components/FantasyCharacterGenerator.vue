<template>
  <v-dialog v-model="dialog" max-width="800px">
    <template v-slot:activator="{ props }">
      <v-btn color="primary" v-bind="props">
        Create Fantasy Character
      </v-btn>
    </template>
    
    <v-card>
      <v-card-title>
        <span class="text-h5">Fantasy Character Generator</span>
      </v-card-title>
      
      <v-card-text>
        <v-row>
          <v-col cols="12" md="6">
            <v-select
              v-model="baseAnimal"
              :items="baseAnimalOptions"
              label="Base Animal"
              required
            ></v-select>
            
            <v-select
              v-model="elementType"
              :items="elementTypeOptions"
              label="Element Type"
              required
            ></v-select>
            
            <v-select
              v-model="dominantColor"
              :items="colorOptions"
              label="Dominant Color"
              required
            ></v-select>
            
            <v-select
              v-model="styleType"
              :items="styleTypeOptions"
              label="Art Style"
              required
            ></v-select>
            
            <v-select
              v-model="traits"
              :items="characterTraitOptions"
              label="Character Traits"
              multiple
              chips
            ></v-select>
          </v-col>
          
          <v-col cols="12" md="6" class="preview-container">
            <div v-if="isLoading" class="text-center">
              <v-progress-circular 
                indeterminate 
                color="primary"
                size="64"
              ></v-progress-circular>
              <div class="mt-3">Generating character...</div>
            </div>
            
            <div v-else-if="imageData" class="preview-image">
              <img :src="imageData" alt="Generated character" width="100%" />
              <div class="mt-2 text-caption">{{ generatedPrompt }}</div>
            </div>
            
            <div v-else class="text-center preview-placeholder">
              <v-icon size="64">mdi-image-outline</v-icon>
              <div class="mt-2">Select options and generate a character</div>
            </div>
          </v-col>
        </v-row>
      </v-card-text>
      
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn color="blue-darken-1" text @click="dialog = false">
          Close
        </v-btn>
        <v-btn color="blue-darken-1" text @click="resetForm">
          Reset
        </v-btn>
        <v-btn 
          color="primary" 
          :disabled="!isFormValid || isLoading" 
          @click="generateCharacter"
        >
          Generate
        </v-btn>
        <v-btn 
          color="success" 
          :disabled="!imageData || isLoading" 
          @click="saveCharacter"
        >
          Save Character
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import { defineComponent, ref, computed } from 'vue';
import axios from 'axios';
import { eventBus } from '../utils/eventBus';

export default defineComponent({
  name: 'FantasyCharacterGenerator',
  
  emits: ['character-created'],
  
  setup(props, { emit }) {
    const dialog = ref(false);
    const isLoading = ref(false);
    const imageData = ref('');
    const generatedPrompt = ref('');
    
    // Form values
    const baseAnimal = ref('');
    const elementType = ref('');
    const dominantColor = ref('');
    const styleType = ref('');
    const traits = ref<string[]>([]);
    
    // Options for dropdowns
    const baseAnimalOptions = [
      'CAT', 'LIZARD', 'BIRD', 
      'FROG', 'FOX', 'SNAKE', 'HORSE', 'TURTLE',
      'LION', 'EAGLE', 'DEER'
    ];
    
    const elementTypeOptions = [
      'FIRE', 'WATER', 'EARTH', 'WIND', 'ELECTRIC', 
      'ICE', 'NATURE', 'SHADOW', 'LIGHT', 'POISON'
    ];
    
    const colorOptions = [
      'RED', 'BLUE', 'GREEN', 'YELLOW', 'PURPLE', 
      'ORANGE', 'BLACK', 'WHITE', 'PINK', 'BROWN'
    ];
    
    const styleTypeOptions = [
      'DISNEY', 'PIXAR', 'POKEMON', 'STUDIO_GHIBLI', 'DREAMWORKS'
    ];
    
    const characterTraitOptions = [
      'CUTE', 'SCARY', 'MYSTERIOUS', 'MAJESTIC',
      'FUNNY', 'SMALL', 'GIANT', 'BABY', 'ELDER'
    ];
    
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
    const isFormValid = computed(() => {
      return baseAnimal.value && elementType.value && dominantColor.value && styleType.value;
    });
    
    // Methods
    const generateCharacter = async () => {
      if (!isFormValid.value) return;
      
      isLoading.value = true;
      try {
        // Transform the base animal into its fantasy version
        const fantasyCreature = fantasyTransformations[baseAnimal.value] || baseAnimal.value;
        
        const response = await axios.post('/api/characters/generate', {
          baseAnimal: baseAnimal.value,
          elementType: elementType.value,
          dominantColor: dominantColor.value,
          styleType: styleType.value,
          traits: traits.value,
          fantasyCreature: fantasyCreature // Add the fantasy transformation
        });
        
        imageData.value = response.data.imageData;
        generatedPrompt.value = response.data.prompt;
      } catch (error) {
        console.error('Error generating character:', error);
        alert('Failed to generate character. Please try again.');
      } finally {
        isLoading.value = false;
      }
    };
    
    const saveCharacter = async () => {
      if (!imageData.value) return;
      
      isLoading.value = true;
      try {
        // Direkt das generierte Bild speichern, anstatt ein neues zu generieren
        const response = await axios.post('/api/characters/save', {
          prompt: generatedPrompt.value,
          imageUrl: imageData.value
        });
        
        // Only emit via event bus for global updates
        const savedCharacter = response.data;
        eventBus.emit('fantasy-character-created', savedCharacter);
        
        // Close dialog and reset form
        dialog.value = false;
        resetForm();
      } catch (error) {
        console.error('Error saving character:', error);
        alert('Failed to save character. Please try again.');
      } finally {
        isLoading.value = false;
      }
    };
    
    const resetForm = () => {
      baseAnimal.value = '';
      elementType.value = '';
      dominantColor.value = '';
      styleType.value = '';
      traits.value = [];
      imageData.value = '';
      generatedPrompt.value = '';
    };
    
    return {
      dialog,
      isLoading,
      imageData,
      generatedPrompt,
      baseAnimal,
      elementType,
      dominantColor,
      styleType,
      traits,
      baseAnimalOptions,
      elementTypeOptions,
      colorOptions,
      styleTypeOptions,
      characterTraitOptions,
      isFormValid,
      generateCharacter,
      saveCharacter,
      resetForm
    };
  }
});
</script>

<style scoped>
.preview-container {
  min-height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.preview-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: rgba(0, 0, 0, 0.38);
}

.preview-image {
  width: 100%;
}
</style>