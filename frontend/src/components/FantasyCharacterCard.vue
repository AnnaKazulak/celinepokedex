<template>
  <div 
    class="fantasy-card-container" 
    :data-id="character.id" 
    ref="cardElement"
    :class="{ 'clickable': true }"
  >
    <div class="fantasy-image-wrapper">
      <v-img
        :src="character.imageUrl"
        class="fantasy-image"
        contain
        @load="extractColorFromImage"
      ></v-img>
    </div>

    <v-card class="fantasy-card" :style="{ backgroundColor: dominantColor + '22' }">
      <div class="card-content">
        <div class="fantasy-header">
          <div class="fantasy-title">Fantasy Character</div>
          
          <div class="description">
            {{ character.prompt }}
          </div>
        </div>

        <div class="fantasy-footer" :style="{ background: dominantColor }">
          <div class="footer-value">
            <span>Erstellt am</span>
            <strong>{{ formatDate(character.createdAt) }}</strong>
          </div>
          <v-btn
            icon
            variant="text"
            color="white"
            @click.stop="downloadImage"
            class="download-btn"
          >
            <v-icon>mdi-download</v-icon>
          </v-btn>
        </div>
      </div>
    </v-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { extractDominantColor } from '../utils/colorUtils';
import { eventBus } from '../utils/eventBus';
import { type FantasyCharacter } from '../types/pokemon';

const props = defineProps<{
  character: FantasyCharacter
}>();

const dominantColor = ref('#6890F0'); // Default fantasy color (water pokemon)
const cardElement = ref<HTMLElement | null>(null);

// Extract the dominant color from the fantasy character image
async function extractColorFromImage() {
  try {
    if (props.character.imageUrl) {
      // Short delay to ensure the image is loaded
      setTimeout(async () => {
        dominantColor.value = await extractDominantColor(props.character.imageUrl || '');
        
        // Add color to global color palette for dynamic navbar
        eventBus.emit('register-fantasy-color', {
          id: props.character.id,
          color: dominantColor.value,
          element: cardElement.value
        });
      }, 100);
    }
  } catch (error) {
    console.error('Error extracting color:', error);
  }
}

// Format date from ISO string to readable format
function formatDate(dateStr?: string): string {
  if (!dateStr) return 'Unknown';
  
  try {
    const date = new Date(dateStr);
    return new Intl.DateTimeFormat('de-DE', { 
      day: '2-digit',
      month: '2-digit', 
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    }).format(date);
  } catch (error) {
    console.error('Error formatting date:', error);
    return 'Invalid date';
  }
}

// Download the generated image
const downloadImage = () => {
  if (!props.character.imageUrl) return;
  
  // For base64 images
  if (props.character.imageUrl.startsWith('data:')) {
    const link = document.createElement('a');
    link.href = props.character.imageUrl;
    link.download = `fantasy-character-${Date.now()}.png`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    return;
  }
  
  // For URL images
  const link = document.createElement('a');
  link.href = props.character.imageUrl;
  link.download = `fantasy-character-${Date.now()}.png`;
  link.target = '_blank';
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
};

onMounted(() => {
  extractColorFromImage();
});
</script>

<style scoped>
.fantasy-card-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 25px auto;
  position: relative;
  width: 100%;
  height: 480px;
}

.clickable {
  cursor: pointer;
  transition: transform 0.3s ease;
}

.clickable:hover {
  transform: translateY(-8px);
}

.fantasy-image-wrapper {
  position: absolute;
  top: -25px;
  z-index: 10;
  width: 240px;
  height: 240px;
  display: flex;
  justify-content: center;
}

.fantasy-image {
  filter: drop-shadow(0 8px 16px rgba(0, 0, 0, 0.3));
  transform: scale(1.1);
  transition: transform 0.5s ease;
}

.clickable:hover .fantasy-image {
  transform: scale(1.15);
}

.fantasy-card {
  width: 100%;
  max-width: 300px;
  height: 400px;
  border-radius: 16px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
  margin: 0 auto;
  margin-top: 60px;
  overflow: hidden;
  position: relative;
}

.card-content {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
}

.fantasy-header {
  text-align: center;
  padding: 16px;
  background-color: white;
  flex-grow: 1;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  padding-top: 180px;
  position: relative;
}

.fantasy-title {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 8px;
}

.description {
  font-size: 14px;
  color: #666;
  margin-top: 8px;
  max-height: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 5;
  -webkit-box-orient: vertical;
}

.fantasy-footer {
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: linear-gradient(135deg, #6890F0 0%, #705898 100%);
  transition: background-color 0.5s ease;
}

.footer-value {
  text-align: left;
  display: flex;
  flex-direction: column;
}

.footer-value span {
  display: block;
  font-size: 12px;
  font-weight: 400;
  opacity: 0.9;
}

.footer-value strong {
  margin-top: 2px;
  font-size: 14px;
}

.download-btn {
  opacity: 0.9;
  transition: opacity 0.2s;
}

.download-btn:hover {
  opacity: 1;
}

/* Mobile Anpassungen */
@media (max-width: 600px) {
  .fantasy-card-container {
    margin: 20px auto;
    height: 420px;
  }
  
  .fantasy-image-wrapper {
    width: 200px;
    height: 200px;
    top: -20px;
  }
  
  .fantasy-card {
    max-width: 280px;
    height: 360px;
    margin-top: 40px;
  }
  
  .fantasy-header {
    padding: 12px;
    padding-top: 140px;
  }
  
  .fantasy-title {
    font-size: 20px;
    margin-bottom: 6px;
  }
  
  .description {
    font-size: 13px;
    max-height: 100px;
    -webkit-line-clamp: 4;
    margin-top: 6px;
  }
  
  .fantasy-footer {
    padding: 10px 12px;
  }
  
  .footer-value strong {
    font-size: 13px;
  }
  
  .footer-value span {
    font-size: 10px;
  }
}

/* Extra kleine Geräte */
@media (max-width: 320px) {
  .fantasy-card-container {
    height: 400px;
  }
  
  .fantasy-image-wrapper {
    width: 170px;
    height: 170px;
    top: -15px;
  }
  
  .fantasy-card {
    height: 340px;
    margin-top: 30px;
  }
  
  .fantasy-header {
    padding-top: 120px;
  }
  
  .description {
    -webkit-line-clamp: 3;
    max-height: 80px;
  }
}
</style>