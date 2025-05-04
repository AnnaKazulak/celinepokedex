<template>
  <div>
    <div v-if="isLoading" class="loading-container">
      <v-progress-circular indeterminate color="primary" size="64"></v-progress-circular>
    </div>

    <template v-else-if="character">
      <div 
        class="fantasy-detail-container" 
        :style="{
          background: isColorLoaded ? `linear-gradient(180deg, ${dominantColorLight} 0%, white 100%)` : 'transparent',
          opacity: isColorLoaded ? 1 : 0
        }"
        :class="{ 'color-loaded': isColorLoaded }"
      >
        <!-- Back Button -->
        <div class="back-button-container">
          <v-btn
            prepend-icon="mdi-arrow-left"
            variant="text"
            @click="goBack"
            class="mb-4 back-button"
            :style="{ color: isColorLoaded ? dominantColor : 'rgba(0,0,0,0.7)' }"
          >
            Zurück
          </v-btn>
        </div>

        <!-- Fantasy Character Header -->
        <div class="fantasy-header">
          <div class="fantasy-image-wrapper">
            <v-img
              :src="character.imageUrl"
              class="fantasy-image"
              contain
              @load="extractColorFromImage"
              max-height="350px"
            ></v-img>
          </div>
          
          <div class="fantasy-title-section">
            <h1 class="title">Fantasy Character</h1>
            <div class="creation-date">
              Erstellt am {{ formatDate(character.createdAt) }}
            </div>
          </div>
        </div>

        <!-- Fantasy Character Details Card -->
        <v-card class="details-card mt-6" :style="{ borderColor: dominantColor }">
          <v-card-title class="header" :style="{ backgroundColor: dominantColor }">
            <h3>Beschreibung</h3>
          </v-card-title>
          <v-card-text class="pa-4">
            <p class="prompt-text">{{ character.prompt }}</p>
          </v-card-text>
          <v-card-actions class="pa-4">
            <v-btn
              color="primary"
              variant="outlined"
              prepend-icon="mdi-download"
              @click="downloadImage"
              :style="{ borderColor: dominantColor, color: dominantColor }"
            >
              Bild herunterladen
            </v-btn>
          </v-card-actions>
        </v-card>

        <!-- Generated Properties Card -->
        <v-card class="details-card mt-6" :style="{ borderColor: dominantColor }">
          <v-card-title class="header" :style="{ backgroundColor: dominantColor }">
            <h3>Eigenschaften</h3>
          </v-card-title>
          <v-card-text class="pa-4">
            <v-row>
              <v-col cols="12" sm="6">
                <div class="property">
                  <span class="property-label">ID:</span>
                  <span class="property-value">{{ character.id || 'Nicht verfügbar' }}</span>
                </div>
              </v-col>
              <v-col cols="12" sm="6">
                <div class="property">
                  <span class="property-label">Erstellt am:</span>
                  <span class="property-value">{{ formatDate(character.createdAt) }}</span>
                </div>
              </v-col>
            </v-row>
            <!-- Removed the Generierte URL section -->
          </v-card-text>
        </v-card>
      </div>
    </template>

    <div v-else class="error-container">
      <v-alert
        type="error"
        variant="tonal"
      >
        Fantasy Character wurde nicht gefunden.
      </v-alert>
      <v-btn
        class="mt-4"
        color="primary"
        @click="goBack"
      >
        Zurück zur Übersicht
      </v-btn>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import axios from 'axios';
import { type FantasyCharacter } from '@/types/pokemon';
import { eventBus } from '@/utils/eventBus';
import { extractDominantColor } from '@/utils/colorUtils';

const router = useRouter();
const route = useRoute();
const character = ref<FantasyCharacter | null>(null);
const isLoading = ref(true);
const error = ref<string | null>(null);
const dominantColor = ref('#6890F0'); // Default fantasy color (blue)
const dominantColorLight = ref('rgba(104, 144, 240, 0.15)'); // Hellere Version für Hintergründe
const isColorLoaded = ref(false); // State-Variable für den Übergang

// Character ID aus der URL lesen
const characterId = computed(() => {
  return route.params.id as string;
});

onMounted(async () => {
  await fetchCharacterDetails();
});

async function fetchCharacterDetails() {
  isLoading.value = true;
  isColorLoaded.value = false;
  error.value = null;

  try {
    // Ändern des API-Pfades zum korrekten Backend-Endpunkt (/api/characters anstatt /api/fantasy-characters)
    const response = await axios.get(`http://localhost:8080/api/characters/${characterId.value}`);
    character.value = response.data;
  } catch (err) {
    console.error('Fehler beim Laden der Fantasy-Character-Details:', err);
    error.value = 'Fantasy Character konnte nicht geladen werden';
  } finally {
    isLoading.value = false;
  }
}

function goBack() {
  router.push({ name: 'home' }); // Oder zur Fantasy-Übersicht, falls vorhanden
}

// Extract the dominant color from the fantasy character image
async function extractColorFromImage() {
  try {
    if (character.value?.imageUrl) {
      dominantColor.value = await extractDominantColor(character.value.imageUrl);
      
      // Hellere Version der Farbe für Hintergründe erstellen
      const rgbMatch = dominantColor.value.match(/rgb\((\d+),\s*(\d+),\s*(\d+)\)/);
      if (rgbMatch) {
        const r = parseInt(rgbMatch[1], 10);
        const g = parseInt(rgbMatch[2], 10);
        const b = parseInt(rgbMatch[3], 10);
        dominantColorLight.value = `rgba(${r}, ${g}, ${b}, 0.15)`;
      } else if (dominantColor.value.startsWith('#')) {
        // Hex-Farbe umwandeln
        const r = parseInt(dominantColor.value.slice(1, 3), 16);
        const g = parseInt(dominantColor.value.slice(3, 5), 16);
        const b = parseInt(dominantColor.value.slice(5, 7), 16);
        dominantColorLight.value = `rgba(${r}, ${g}, ${b}, 0.15)`;
      }

      // Farbe an die Navbar senden
      eventBus.emit('detail-page-color-change', dominantColor.value);
      
      // Animation starten
      setTimeout(() => {
        isColorLoaded.value = true;
      }, 100);
    }
  } catch (error) {
    console.error('Error extracting color:', error);
    isColorLoaded.value = true; // Trotzdem fortfahren bei Fehlern
  }
}

// Format date from ISO string to readable format
function formatDate(dateStr?: string): string {
  if (!dateStr) return 'Unbekannt';
  
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
    return 'Ungültiges Datum';
  }
}

// Download the generated image
function downloadImage() {
  if (!character.value?.imageUrl) return;
  
  // Für base64 Bilder
  if (character.value.imageUrl.startsWith('data:')) {
    const link = document.createElement('a');
    link.href = character.value.imageUrl;
    link.download = `fantasy-character-${Date.now()}.png`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    return;
  }
  
  // Für URL Bilder
  const link = document.createElement('a');
  link.href = character.value.imageUrl;
  link.download = `fantasy-character-${Date.now()}.png`;
  link.target = '_blank';
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
}
</script>

<style scoped>
.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80vh;
}

.fantasy-detail-container {
  border-radius: 0;
  padding: 2rem 5%;
  min-height: 100vh;
  width: 100%;
  transition: all 0.8s ease;
}

.color-loaded {
  opacity: 1 !important;
}

.back-button-container {
  padding: 0 0 1rem 0;
}

.back-button {
  font-weight: bold;
  transition: all 0.5s ease;
}

.back-button:hover {
  transform: translateX(-5px);
}

.fantasy-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 2rem;
}

.fantasy-image-wrapper {
  margin-bottom: 1.5rem;
  width: 100%;
  max-width: 450px;
  display: flex;
  justify-content: center;
}

.fantasy-image {
  border-radius: 12px;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.2);
  transition: transform 0.3s ease;
}

.fantasy-title-section {
  text-align: center;
}

.fantasy-title-section .title {
  font-size: 2.5rem;
  font-weight: bold;
  margin-bottom: 0.5rem;
}

.creation-date {
  font-size: 1rem;
  color: rgba(0, 0, 0, 0.6);
}

.details-card {
  border-radius: 12px;
  margin-bottom: 2rem;
  border-left: 4px solid;
  overflow: hidden;
}

.details-card .header {
  color: white;
  padding: 1rem 1.5rem;
}

.details-card .header h3 {
  font-size: 1.5rem;
  margin: 0;
}

.prompt-text {
  white-space: pre-line;
  font-size: 1.1rem;
  line-height: 1.6;
}

.property {
  margin-bottom: 1rem;
  display: flex;
  flex-direction: column;
}

.property-label {
  font-weight: bold;
  font-size: 0.9rem;
  color: rgba(0, 0, 0, 0.6);
  margin-bottom: 0.25rem;
}

.property-value {
  font-size: 1.1rem;
}

.url-value {
  word-break: break-all;
  font-size: 0.9rem;
  color: rgba(0, 0, 0, 0.8);
}

.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 50vh;
  padding: 2rem;
}

@media (max-width: 960px) {
  .fantasy-detail-container {
    padding: 1.5rem 4%;
  }
  
  .fantasy-title-section .title {
    font-size: 2rem;
  }
}

@media (max-width: 600px) {
  .fantasy-detail-container {
    padding: 1rem 3%;
  }
  
  .fantasy-title-section .title {
    font-size: 1.8rem;
  }
  
  .prompt-text {
    font-size: 1rem;
  }
  
  .property-value {
    font-size: 1rem;
  }
}
</style>