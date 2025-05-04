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
          opacity: isColorLoaded ? 1 : 0,
          '--dominant-color': dominantColor,
          '--dominant-color-light': dominantColorLight
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
          <!-- Fantasy Character Info - Links -->
          <div class="fantasy-info">
            <div class="mb-4">
              <div class="d-flex align-center">
                <h1 class="text-h2 font-weight-bold mr-2"
                    :style="{ color: isColorLoaded ? dominantColor : 'rgba(0,0,0,0.87)' }"
                >
                  Fantasy Character
                </h1>
              </div>
              <p class="creation-date text-subtitle-1">
                Erstellt am {{ formatDate(character.createdAt) }}
              </p>
            </div>

            <!-- Fantasy Character Description -->
            <div class="description-container">
              <p class="prompt-text">{{ character.prompt }}</p>
              
              <!-- Download Button direkt unter der Beschreibung -->
              <v-btn
                color="primary"
                variant="outlined"
                prepend-icon="mdi-download"
                @click="downloadImage"
                class="download-btn mt-4"
              >
                Bild herunterladen
              </v-btn>
            </div>
          </div>
          
          <!-- Fantasy Character Image Container - Rechts -->
          <div class="fantasy-image-container">
            <v-img
              :src="character.imageUrl"
              class="fantasy-image"
              contain
              @load="extractColorFromImage"
            ></v-img>
          </div>
        </div>

        <!-- Generated Properties Card -->
        <v-card 
          class="details-card mt-6" 
          :style="{ '--dominant-color': dominantColor }"
        >
          <v-card-title 
            class="header" 
            :style="{ backgroundColor: dominantColor }"
          >
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
      
      // CSS-Variablen aktualisieren und Animation starten
      document.documentElement.style.setProperty('--dominant-color', dominantColor.value);
      document.documentElement.style.setProperty('--dominant-color-light', dominantColorLight.value);
      
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
  color: var(--dominant-color);
}

.back-button:hover {
  transform: translateX(-5px);
}

.fantasy-header {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 2rem;
  max-width: 1400px;
  margin: 0 auto 2rem;
}

.fantasy-info {
  flex: 1;
  min-width: 280px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.fantasy-image-container {
  flex: 1;
  min-width: 250px;
  display: flex;
  justify-content: center;
  padding: 0;
}

.fantasy-image {
  filter: drop-shadow(0 8px 16px rgba(0, 0, 0, 0.2));
  transform: scale(1.05);
  transition: transform 0.5s ease;
  max-height: 450px;
  max-width: 100%;
}

.fantasy-image:hover {
  transform: scale(1.1);
}

.title {
  color: var(--dominant-color, #6890F0);
}

.color-transition {
  color: var(--dominant-color, rgba(0,0,0,0.7));
  transition: color 0.5s ease;
}

.description-container {
  margin-bottom: 1.5rem;
}

.prompt-text {
  white-space: pre-line;
  font-size: 1.1rem;
  line-height: 1.6;
  color: rgba(0, 0, 0, 0.8);
  margin-bottom: 1rem;
}

.download-btn {
  border-color: var(--dominant-color);
  color: var(--dominant-color);
}

.details-card {
  border-radius: 12px;
  margin: 0 auto 2rem;
  border-left: 4px solid var(--dominant-color);
  overflow: hidden;
  max-width: 1400px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.details-card .header {
  background-color: var(--dominant-color);
  color: white;
  padding: 1rem 1.5rem;
}

.details-card .header h3 {
  font-size: 1.5rem;
  margin: 0;
  font-weight: 500;
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

.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 50vh;
  padding: 2rem;
}

/* Größere Desktop-Bildschirme */
@media (min-width: 1200px) {
  .fantasy-header {
    padding: 0 2rem;
  }
  
  .fantasy-info {
    padding-right: 50px;
  }
  
  .fantasy-image {
    height: 450px;
    width: 450px;
  }
}

/* Mittlere Desktops und Tablets im Querformat */
@media (min-width: 769px) and (max-width: 1199px) {
  .fantasy-header {
    gap: 0;
  }
  
  .fantasy-info {
    padding-right: 20px;
  }
  
  .fantasy-image {
    height: 350px;
    width: 350px;
  }
}

/* Tablets im Hochformat und mobile Geräte */
@media (max-width: 768px) {
  .fantasy-header {
    flex-direction: column-reverse;
    gap: 1.5rem;
  }
  
  .fantasy-image {
    height: 320px;
    width: 320px;
  }
  
  .fantasy-info {
    text-align: center;
    padding: 0 1rem;
  }
  
  .fantasy-info .d-flex {
    justify-content: center;
  }
  
  .download-btn {
    margin: 0 auto;
    display: block;
  }
}

/* Mobile Geräte */
@media (max-width: 600px) {
  .fantasy-detail-container {
    padding: 1rem 3%;
  }
  
  .fantasy-image {
    height: 250px;
    width: 250px;
  }
  
  .fantasy-info h1 {
    font-size: 2rem !important;
    line-height: 1.2;
    margin-right: 0.5rem;
  }
  
  .prompt-text {
    font-size: 1rem;
    line-height: 1.5;
  }
}
</style>