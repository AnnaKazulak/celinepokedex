<template>
  <v-dialog v-model="dialogVisible" max-width="700">
    <v-card class="fantasy-dialog">
      <v-card-title class="dialog-title">
        Create Fantasy Character
        <v-spacer></v-spacer>
        <v-btn icon @click="closeDialog">
          <v-icon>mdi-close</v-icon>
        </v-btn>
      </v-card-title>
      
      <v-card-text>
        <v-form @submit.prevent="generateImage">
          <v-textarea
            v-model="prompt"
            label="Describe your fantasy character"
            hint="Be as descriptive as possible for best results"
            rows="3"
            auto-grow
            counter
            :disabled="isGenerating"
          ></v-textarea>
          
          <div class="text-center mt-4">
            <v-btn 
              color="primary" 
              :loading="isGenerating"
              :disabled="!prompt || isGenerating"
              @click="generateImage"
            >
              {{ isGenerating ? 'Generating...' : 'Generate Character' }}
            </v-btn>
          </div>
          
          <div v-if="isGenerating" class="text-center my-4">
            <v-progress-circular indeterminate color="primary"></v-progress-circular>
            <p class="mt-2">Creating your fantasy character... This may take 15-30 seconds.</p>
          </div>
          
          <div v-if="generatedImageUrl" class="image-result mt-4">
            <v-card flat>
              <v-card-title class="pb-0">Your Fantasy Character</v-card-title>
              <v-card-subtitle>{{ prompt }}</v-card-subtitle>
              <v-card-text class="text-center">
                <v-img
                  :src="generatedImageUrl"
                  max-height="400"
                  contain
                  class="generated-image"
                ></v-img>
              </v-card-text>
              <v-card-actions>
                <v-spacer></v-spacer>
                <v-btn
                  color="primary"
                  variant="text"
                  @click="downloadImage"
                >
                  Download
                </v-btn>
                <v-btn
                  color="secondary" 
                  variant="text"
                  @click="resetForm"
                >
                  Create Another
                </v-btn>
                <v-spacer></v-spacer>
              </v-card-actions>
            </v-card>
          </div>
          
          <div v-if="error" class="error-message mt-4">
            <v-alert type="error" dismissible>
              {{ error }}
            </v-alert>
          </div>
        </v-form>
      </v-card-text>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import axios from 'axios';

// Props and emits
const props = defineProps<{
  dialog: boolean;
}>();

const emit = defineEmits<{
  'update:dialog': [value: boolean];
}>();

// Reactive variables
const dialogVisible = ref(props.dialog);
const prompt = ref('');
const isGenerating = ref(false);
const generatedImageUrl = ref('');
const error = ref('');

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
};

// Generate image
const generateImage = async () => {
  if (!prompt.value || isGenerating.value) return;
  
  isGenerating.value = true;
  error.value = '';
  generatedImageUrl.value = '';
  
  try {
    const response = await axios.post('/api/images/generate', {
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
</script>

<style scoped>
.fantasy-dialog {
  border-radius: 16px;
  overflow: hidden;
  background-color: #f8f9fa;
}

.dialog-title {
  background: linear-gradient(135deg, var(--v-primary-base) 0%, var(--v-secondary-base) 100%);
  color: white;
  padding: 16px 20px;
}

.generated-image {
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  margin: 12px 0;
}

.error-message {
  margin-top: 16px;
}
</style>