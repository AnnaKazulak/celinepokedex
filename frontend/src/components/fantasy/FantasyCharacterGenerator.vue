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
      
      <!-- Add tabs to switch between manual prompt, upload, and generator -->
      <v-tabs v-model="activeTab" centered>
        <v-tab value="prompt">
          <v-icon start>mdi-text-box-outline</v-icon>
          Prompt Entry
        </v-tab>
        <v-tab value="upload">
          <v-icon start>mdi-upload</v-icon>
          Upload Your Own
        </v-tab>
        <v-tab value="generator">
          <v-icon start>mdi-robot</v-icon>
          AI Generator
        </v-tab>
      </v-tabs>
      
      <v-card-text>
        <v-window v-model="activeTab">
          <!-- Prompt Entry Tab (New) -->
          <v-window-item value="prompt">
            <v-row>
              <v-col cols="12" md="6">
                <v-text-field
                  v-model="promptCharacterName"
                  label="Character Name"
                  required
                ></v-text-field>
                
                <v-textarea
                  v-model="promptDescription"
                  label="Describe your fantasy character"
                  rows="4"
                  counter
                  hint="Be detailed and descriptive for best results"
                  required
                ></v-textarea>
                
                <v-select
                  v-model="promptBaseAnimal"
                  :items="baseAnimalOptions"
                  label="Base Animal (Optional)"
                ></v-select>
                
                <v-select
                  v-model="promptElementType"
                  :items="elementTypeOptions"
                  label="Element Type (Optional)"
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
                
                <div v-else-if="promptImageData" class="preview-image">
                  <img :src="promptImageData" alt="Generated character" width="100%" />
                  <div class="mt-2 text-caption">{{ promptGeneratedPrompt }}</div>
                </div>
                
                <div v-else class="text-center preview-placeholder">
                  <v-icon size="64">mdi-text-box-edit-outline</v-icon>
                  <div class="mt-2">Enter a detailed description and generate a character</div>
                </div>
              </v-col>
            </v-row>
          </v-window-item>
          
          <!-- Upload Tab -->
          <v-window-item value="upload">
            <v-row>
              <v-col cols="12" md="6">
                <!-- Character details form -->
                <v-text-field
                  v-model="uploadedName"
                  label="Character Name"
                  required
                >
                  <template v-slot:append-inner>
                    <v-tooltip text="Generate AI name">
                      <template v-slot:activator="{ props }">
                        <v-btn 
                          icon 
                          v-bind="props" 
                          variant="text" 
                          color="primary" 
                          @click="generateAIName"
                          :disabled="!uploadedBaseAnimal || !uploadedElementType"
                          :loading="isGeneratingName"
                        >
                          <v-icon>mdi-robot</v-icon>
                        </v-btn>
                      </template>
                    </v-tooltip>
                  </template>
                </v-text-field>
                
                <v-select
                  v-model="uploadedBaseAnimal"
                  :items="baseAnimalOptions"
                  label="Base Animal"
                  required
                ></v-select>
                
                <v-select
                  v-model="uploadedElementType"
                  :items="elementTypeOptions"
                  label="Element Type"
                  required
                ></v-select>
                
                <v-textarea
                  v-model="uploadedDescription"
                  label="Character Description"
                  rows="3"
                  counter
                  required
                >
                  <template v-slot:append-inner>
                    <v-tooltip text="Generate AI description">
                      <template v-slot:activator="{ props }">
                        <v-btn 
                          icon 
                          v-bind="props" 
                          variant="text" 
                          color="primary" 
                          @click="generateAIDescription"
                          :disabled="!uploadedBaseAnimal || !uploadedElementType"
                          :loading="isGeneratingDescription"
                        >
                          <v-icon>mdi-robot</v-icon>
                        </v-btn>
                      </template>
                    </v-tooltip>
                  </template>
                </v-textarea>
              </v-col>
              
              <v-col cols="12" md="6" class="preview-container">
                <div v-if="isLoading" class="text-center">
                  <v-progress-circular 
                    indeterminate 
                    color="primary"
                    size="64"
                  ></v-progress-circular>
                  <div class="mt-3">Processing upload...</div>
                </div>
                
                <div v-else class="upload-area">
                  <div v-if="uploadedImageData" class="preview-image">
                    <img :src="uploadedImageData" alt="Uploaded character" width="100%" />
                  </div>
                  
                  <div v-else class="upload-placeholder">
                    <v-icon size="64" color="grey">mdi-cloud-upload</v-icon>
                    <div class="mt-2">Click to upload an image</div>
                  </div>
                  
                  <input 
                    type="file" 
                    ref="fileInput" 
                    accept="image/*" 
                    @change="onFileChange" 
                    style="display:none"
                  />
                  
                  <v-btn 
                    block 
                    color="primary" 
                    class="mt-4" 
                    @click="triggerFileUpload"
                    :disabled="isLoading"
                  >
                    <v-icon start>mdi-image-plus</v-icon>
                    {{ uploadedImageData ? 'Change Image' : 'Upload Image' }}
                  </v-btn>
                  
                  <v-btn 
                    v-if="uploadedImageData"
                    block 
                    color="error" 
                    class="mt-2" 
                    @click="removeUploadedImage"
                    :disabled="isLoading"
                  >
                    <v-icon start>mdi-delete</v-icon>
                    Remove Image
                  </v-btn>
                </div>
              </v-col>
            </v-row>
          </v-window-item>
          
          <!-- Generator Tab (moved from first position to third) -->
          <v-window-item value="generator">
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
          </v-window-item>
        </v-window>
      </v-card-text>
      
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn color="blue-darken-1" text @click="dialog = false">
          Close
        </v-btn>
        <v-btn color="blue-darken-1" text @click="resetForm">
          Reset
        </v-btn>
        
        <!-- Conditionally show buttons based on active tab -->
        <template v-if="activeTab === 'generator'">
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
        </template>
        
        <template v-else-if="activeTab === 'prompt'">
          <v-btn 
            color="primary" 
            :disabled="!isPromptFormValid || isLoading" 
            @click="generatePromptCharacter"
          >
            Generate
          </v-btn>
          <v-btn 
            color="success" 
            :disabled="!promptImageData || isLoading" 
            @click="savePromptCharacter"
          >
            Save Character
          </v-btn>
        </template>
        
        <template v-else>
          <v-btn 
            color="success" 
            :disabled="!isUploadFormValid || isLoading" 
            @click="saveUploadedCharacter"
          >
            Save Uploaded Character
          </v-btn>
        </template>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import { defineComponent, ref, computed } from 'vue';
import axios from 'axios';
import { eventBus } from '../../utils/eventBus';

export default defineComponent({
  name: 'FantasyCharacterGenerator',
  
  emits: ['character-created'],
  
  setup(props, { emit }) {
    const dialog = ref(false);
    const isLoading = ref(false);
    const imageData = ref('');
    const generatedPrompt = ref('');
    const activeTab = ref('prompt');
    const fileInput = ref<HTMLInputElement | null>(null);
    const isGeneratingDescription = ref(false);
    const isGeneratingName = ref(false);
    
    // Form values for AI generation
    const baseAnimal = ref('');
    const elementType = ref('');
    const dominantColor = ref('');
    const styleType = ref('');
    const traits = ref<string[]>([]);
    
    // Form values for manual upload
    const uploadedImageData = ref('');
    const uploadedName = ref('');
    const uploadedBaseAnimal = ref('');
    const uploadedElementType = ref('');
    const uploadedDescription = ref('');
    
    // Form values for manual prompt entry
    const promptCharacterName = ref('');
    const promptDescription = ref('');
    const promptBaseAnimal = ref('');
    const promptElementType = ref('');
    const promptImageData = ref('');
    const promptGeneratedPrompt = ref('');
    
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
      'CAT': 'Whisker Sprite (magical playful cat)',
      'LIZARD': 'Sparkle Gecko (tiny glitter dragon)',
      'BIRD': 'Featherling (tiny rainbow bird spirit)',
      'FROG': 'Bubble Hopper (friendly marsh explorer)',
      'FOX': 'Flufftail (clever forest helper)',
      'SNAKE': 'Twinklecoil (shiny garden snake sprite)',
      'HORSE': 'Dreampony (starry flying pony)',
      'TURTLE': 'Shellbuddy (gentle forest guardian)',
      'LION': 'Sunnymane (brave but kind lion hero)',
      'EAGLE': 'Cloudglider (friendly sky messenger)',
      'DEER': 'Stardancer (gentle glowing deer spirit)'
    };
    
    // Computed properties
    const isFormValid = computed(() => {
      return baseAnimal.value && elementType.value && dominantColor.value && styleType.value;
    });
    
    const isUploadFormValid = computed(() => {
      return uploadedImageData.value && 
             uploadedName.value && 
             uploadedBaseAnimal.value && 
             uploadedElementType.value && 
             uploadedDescription.value;
    });
    
    const isPromptFormValid = computed(() => {
      return promptCharacterName.value && promptDescription.value;
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
    
    const triggerFileUpload = () => {
      if (fileInput.value) {
        fileInput.value.click();
      }
    };
    
    const onFileChange = (event: Event) => {
      const target = event.target as HTMLInputElement;
      if (target.files && target.files[0]) {
        const file = target.files[0];
        
        // Check file size (limit to 5MB)
        if (file.size > 5 * 1024 * 1024) {
          alert('Image is too large. Maximum size is 5MB.');
          return;
        }
        
        const reader = new FileReader();
        
        reader.onload = (e) => {
          if (e.target?.result) {
            uploadedImageData.value = e.target.result as string;
          }
        };
        
        reader.readAsDataURL(file);
      }
    };
    
    const removeUploadedImage = () => {
      uploadedImageData.value = '';
      if (fileInput.value) {
        fileInput.value.value = '';
      }
    };
    
    const saveUploadedCharacter = async () => {
      if (!isUploadFormValid.value) return;
      
      isLoading.value = true;
      try {
        // Save the uploaded character with user's exact description
        const response = await axios.post('/api/characters/save', {
          name: uploadedName.value,
          prompt: uploadedDescription.value,
          imageUrl: uploadedImageData.value,
          baseAnimal: uploadedBaseAnimal.value,
          elementType: uploadedElementType.value
        });
        
        // Emit the saved character
        const savedCharacter = response.data;
        eventBus.emit('fantasy-character-created', savedCharacter);
        
        // Close dialog and reset form
        dialog.value = false;
        resetForm();
      } catch (error) {
        console.error('Error saving uploaded character:', error);
        alert('Failed to save character. Please try again.');
      } finally {
        isLoading.value = false;
      }
    };

    const generateAIDescription = async () => {
      if (!uploadedBaseAnimal.value || !uploadedElementType.value) return;

      isGeneratingDescription.value = true;
      try {
        const fantasyCreature = fantasyTransformations[uploadedBaseAnimal.value] || uploadedBaseAnimal.value;
        const response = await axios.post('/api/characters/generate-description', {
          baseAnimal: uploadedBaseAnimal.value,
          elementType: uploadedElementType.value,
          fantasyCreature: fantasyCreature
        });

        uploadedDescription.value = response.data.description;
      } catch (error) {
        console.error('Error generating description:', error);
        alert('Failed to generate description. Please try again.');
      } finally {
        isGeneratingDescription.value = false;
      }
    };

    const generateAIName = async () => {
      if (!uploadedBaseAnimal.value || !uploadedElementType.value) return;

      isGeneratingName.value = true;
      try {
        const fantasyCreature = fantasyTransformations[uploadedBaseAnimal.value] || uploadedBaseAnimal.value;
        const response = await axios.post('/api/characters/generate-name', {
          baseAnimal: uploadedBaseAnimal.value,
          elementType: uploadedElementType.value,
          fantasyCreature: fantasyCreature
        });

        uploadedName.value = response.data.name;
      } catch (error) {
        console.error('Error generating name:', error);
        alert('Failed to generate name. Please try again.');
      } finally {
        isGeneratingName.value = false;
      }
    };

    const generatePromptCharacter = async () => {
      if (!isPromptFormValid.value) return;

      isLoading.value = true;
      try {
        const response = await axios.post('/api/characters/generate-prompt', {
          name: promptCharacterName.value,
          description: promptDescription.value,
          baseAnimal: promptBaseAnimal.value,
          elementType: promptElementType.value
        });

        promptImageData.value = response.data.imageData;
        promptGeneratedPrompt.value = response.data.prompt;
      } catch (error) {
        console.error('Error generating character from prompt:', error);
        alert('Failed to generate character. Please try again.');
      } finally {
        isLoading.value = false;
      }
    };

    const savePromptCharacter = async () => {
      if (!promptImageData.value) return;

      isLoading.value = true;
      try {
        const response = await axios.post('/api/characters/save', {
          name: promptCharacterName.value,
          prompt: promptGeneratedPrompt.value,
          imageUrl: promptImageData.value
        });

        const savedCharacter = response.data;
        eventBus.emit('fantasy-character-created', savedCharacter);

        dialog.value = false;
        resetForm();
      } catch (error) {
        console.error('Error saving character from prompt:', error);
        alert('Failed to save character. Please try again.');
      } finally {
        isLoading.value = false;
      }
    };
    
    const resetForm = () => {
      // Reset AI generation form
      baseAnimal.value = '';
      elementType.value = '';
      dominantColor.value = '';
      styleType.value = '';
      traits.value = [];
      imageData.value = '';
      generatedPrompt.value = '';
      
      // Reset upload form
      uploadedImageData.value = '';
      uploadedName.value = '';
      uploadedBaseAnimal.value = '';
      uploadedElementType.value = '';
      uploadedDescription.value = '';
      
      // Reset prompt form
      promptCharacterName.value = '';
      promptDescription.value = '';
      promptBaseAnimal.value = '';
      promptElementType.value = '';
      promptImageData.value = '';
      promptGeneratedPrompt.value = '';
      
      if (fileInput.value) {
        fileInput.value.value = '';
      }
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
      resetForm,
      // New properties for tabs and upload functionality
      activeTab,
      fileInput,
      uploadedImageData,
      uploadedName,
      uploadedBaseAnimal,
      uploadedElementType,
      uploadedDescription,
      isUploadFormValid,
      triggerFileUpload,
      onFileChange,
      removeUploadedImage,
      saveUploadedCharacter,
      generateAIDescription,
      isGeneratingDescription,
      generateAIName,
      isGeneratingName,
      // New properties for prompt entry functionality
      promptCharacterName,
      promptDescription,
      promptBaseAnimal,
      promptElementType,
      promptImageData,
      promptGeneratedPrompt,
      isPromptFormValid,
      generatePromptCharacter,
      savePromptCharacter
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

/* New styles for upload feature */
.upload-area {
  width: 100%;
  min-height: 300px;
  display: flex;
  flex-direction: column;
  border: 2px dashed #ccc;
  border-radius: 8px;
  padding: 16px;
}

.upload-placeholder {
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #999;
  min-height: 200px;
  cursor: pointer;
}
</style>