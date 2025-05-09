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

export default defineComponent({
  name: 'FantasyCharacterGenerator',
  
  setup() {
    const dialog = ref(false);
    const isLoading = ref(false);
    const imageData = ref('');
    const generatedPrompt = ref('');
    
    // Form values
    const baseAnimal = ref('');
    const elementType = ref('');
    const styleType = ref('');
    const traits = ref<string[]>([]);
    
    // Options for dropdowns
    const baseAnimalOptions = [
      'CAT', 'DOG', 'LIZARD', 'MOUSE', 'BIRD', 
      'FROG', 'FOX', 'SNAKE', 'HORSE', 'TURTLE'
    ];
    
    const elementTypeOptions = [
      'FIRE', 'WATER', 'EARTH', 'WIND', 'ELECTRIC', 
      'ICE', 'NATURE', 'SHADOW', 'LIGHT', 'POISON'
    ];
    
    const styleTypeOptions = [
      'DISNEY', 'PIXAR', 'POKEMON', 'STUDIO_GHIBLI', 'DREAMWORKS'
    ];
    
    const characterTraitOptions = [
      'CUTE', 'SCARY', 'MYSTERIOUS', 'MAJESTIC',
      'FUNNY', 'SMALL', 'GIANT', 'BABY', 'ELDER'
    ];
    
    // Computed properties
    const isFormValid = computed(() => {
      return baseAnimal.value && elementType.value && styleType.value;
    });
    
    // Methods
    const generateCharacter = async () => {
      if (!isFormValid.value) return;
      
      isLoading.value = true;
      try {
        const response = await axios.post('/api/characters/generate', {
          baseAnimal: baseAnimal.value,
          elementType: elementType.value,
          styleType: styleType.value,
          traits: traits.value
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
        await axios.post('/api/characters/save-generated', {
          baseAnimal: baseAnimal.value,
          elementType: elementType.value,
          styleType: styleType.value,
          traits: traits.value
        });
        
        alert('Character saved successfully!');
        dialog.value = false;
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
      styleType,
      traits,
      baseAnimalOptions,
      elementTypeOptions,
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