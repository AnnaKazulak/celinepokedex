<template>
  <!-- Use :id="$attrs.id" to properly handle inheritance of the id attribute -->
  <div :id="$attrs.id">
    <div v-if="isLoading" class="d-flex justify-center align-center" style="min-height: 80vh">
      <v-progress-circular indeterminate color="primary" size="64"></v-progress-circular>
    </div>

    <template v-else-if="character">
      <div 
        class="pa-8 pa-sm-10 px-md-12 transition-smooth w-100"
        :style="{
          background: isColorLoaded ? `linear-gradient(180deg, ${dominantColorLight} 0%, white 100%)` : 'transparent',
          opacity: isColorLoaded ? 1 : 0,
          '--dominant-color': dominantColor,
          '--dominant-color-light': dominantColorLight,
          minHeight: '100vh'
        }"
        :class="{ 'opacity-1': isColorLoaded }"
      >
        <!-- Back Button -->
        <div>
          <v-btn
            prepend-icon="mdi-arrow-left"
            variant="text"
            @click="goBack"
            class="mb-4 font-weight-bold transition-transform"
            :style="{ color: isColorLoaded ? dominantColor : 'rgba(0,0,0,0.7)' }"
          >
            Zurück
          </v-btn>
        </div>

        <!-- Fantasy Character Header -->
        <v-container fluid class="ma-0 pa-0 mb-8" style="max-width: 1400px">
          <v-row class="align-center" :class="{'flex-column-reverse': $vuetify.display.smAndDown}">
            <!-- Fantasy Character Info -->
            <v-col cols="12" md="6" :class="{'text-center': $vuetify.display.smAndDown}">
              <div class="mb-4">
                <div class="d-flex align-center" :class="{'justify-center': $vuetify.display.smAndDown}">
                  <h1 class="text-h2 font-weight-bold mr-2"
                      :style="{ color: isColorLoaded ? dominantColor : 'rgba(0,0,0,0.87)' }"
                      :class="{'text-h4': $vuetify.display.xs}"
                  >
                    {{ character.name || 'Fantasy Character' }}
                  </h1>
                </div>
   
              </div>

              <!-- Fantasy Character Description -->
              <div class="mb-6">
                <p class="text-body-1 mb-4 white-space-pre-line" 
                  :class="{'text-body-2': $vuetify.display.xs}"
                  style="line-height: 1.6; color: rgba(0, 0, 0, 0.8);">
                  {{ character.prompt }}
                </p>
                
                <div class="d-flex flex-wrap gap-2" :class="{'justify-center': $vuetify.display.smAndDown}">
                  <v-btn
                    color="primary"
                    variant="outlined"
                    prepend-icon="mdi-download"
                    @click="downloadImage"
                    class="mt-4"
                    :style="{ borderColor: dominantColor, color: dominantColor }"
                  >
                    Bild herunterladen
                  </v-btn>
                  
                  <v-btn
                    color="primary"
                    variant="outlined"
                    prepend-icon="mdi-pencil"
                    @click="openEditDialog"
                    class="mt-4"
                    :style="{ borderColor: dominantColor, color: dominantColor }"
                  >
                    Bearbeiten
                  </v-btn>
                  
                  <v-btn
                    color="error"
                    variant="outlined"
                    prepend-icon="mdi-delete"
                    @click="showDeleteConfirmation = true"
                    class="mt-4"
                  >
                    Löschen
                  </v-btn>
                </div>
              </div>
            </v-col>
            
            <!-- Fantasy Character Image Container - nur einfaches v-img ohne v-card -->
            <v-col cols="12" md="6" class="d-flex justify-center">
              <v-img
                :src="character.imageUrl"
                @load="extractColorFromImage"
                contain
                :height="$vuetify.display.xs ? '250' : $vuetify.display.smAndDown ? '320' : $vuetify.display.mdAndDown ? '350' : '450'"
                :width="$vuetify.display.xs ? '250' : $vuetify.display.smAndDown ? '320' : $vuetify.display.mdAndDown ? '350' : '450'"
                class="transform-scale transition-transform rounded-lg"
                :style="{ 
                  transform: 'scale(1.05)',
                  filter: 'drop-shadow(0 8px 16px rgba(0, 0, 0, 0.2))'
                }"
              ></v-img>
            </v-col>
          </v-row>
        </v-container>

        <!-- Generated Properties Card -->
        <v-card 
          class="mt-6 mx-auto rounded-lg overflow-hidden" 
          max-width="1400px"
          :style="{ 
            borderLeft: '4px solid',
            borderLeftColor: dominantColor 
          }"
          elevation="4"
        >
          <v-card-title 
            class="py-4 px-6"
            :style="{ backgroundColor: dominantColor, color: 'white' }"
          >
            <span class="text-h5 font-weight-medium">Eigenschaften</span>
          </v-card-title>
          <v-card-text class="pa-4">
            <v-row>
              <v-col cols="12" sm="6">
                <div class="d-flex flex-column mb-4">
                  <span class="text-caption font-weight-bold text-medium-emphasis mb-1">ID:</span>
                  <span class="text-body-1">{{ character.id || 'Nicht verfügbar' }}</span>
                </div>
              </v-col>
              <v-col cols="12" sm="6">
                <div class="d-flex flex-column mb-4">
                  <span class="text-caption font-weight-bold text-medium-emphasis mb-1">Erstellt am:</span>
                  <span class="text-body-1">{{ formatDate(character.createdAt) }}</span>
                </div>
              </v-col>
            </v-row>
          </v-card-text>
        </v-card>
      </div>
    </template>

    <div v-else class="d-flex flex-column align-center justify-center pa-8" style="min-height: 50vh">
      <v-alert
        type="error"
        variant="tonal"
        width="100%"
        max-width="500px"
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
  
  <!-- Bestätigungsdialog für das Löschen -->
  <v-dialog v-model="showDeleteConfirmation" max-width="400">
    <v-card>
      <v-card-title class="text-h5">
        Fantasy Character löschen?
      </v-card-title>
      <v-card-text>
        Möchten Sie diesen Fantasy Character wirklich löschen? Diese Aktion kann nicht rückgängig gemacht werden.
      </v-card-text>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn color="blue-darken-1" variant="text" @click="showDeleteConfirmation = false">
          Abbrechen
        </v-btn>
        <v-btn 
          color="error" 
          variant="flat" 
          @click="deleteCharacter" 
          :loading="isDeleting"
        >
          Löschen
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>

  <!-- Bearbeitungsdialog -->
  <fantasy-character-edit-dialog
    v-if="character"
    v-model:dialog="showEditDialog"
    :character="character"
    @character-updated="handleCharacterUpdated"
  />
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import axios from 'axios';
import { type FantasyCharacter } from '@/types/pokemon';
import { eventBus } from '@/utils/eventBus';
import { extractDominantColor } from '@/utils/colorUtils';
import FantasyCharacterEditDialog from '@/components/FantasyCharacterEditDialog.vue';

// Disable automatic attribute inheritance
defineOptions({
  inheritAttrs: false
});

const router = useRouter();
const route = useRoute();
const character = ref<FantasyCharacter | null>(null);
const isLoading = ref(true);
const error = ref<string | null>(null);
const dominantColor = ref('#6890F0'); // Default fantasy color (blue)
const dominantColorLight = ref('rgba(104, 144, 240, 0.15)'); // Hellere Version für Hintergründe
const isColorLoaded = ref(false); // State-Variable für den Übergang
const showDeleteConfirmation = ref(false); // State-Variable für den Bestätigungsdialog
const showEditDialog = ref(false); // State-Variable für den Bearbeitungsdialog
const isDeleting = ref(false); // State-Variable für den Löschvorgang

// Character ID aus der URL lesen
const characterId = computed(() => {
  return route.params.id as string;
});

onMounted(async () => {
  await fetchCharacterDetails();
});

// Watch für Änderungen der Character ID (wenn ein neuer Character erstellt wird)
watch(() => route.params.id, async (newId, oldId) => {
  if (newId !== oldId) {
    // Lade Daten des neuen Characters
    await fetchCharacterDetails();
  }
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

// Delete the character
async function deleteCharacter() {
  if (!character.value?.id) return;
  
  isDeleting.value = true;
  
  try {
    await axios.delete(`http://localhost:8080/api/characters/${character.value.id}`);
    showDeleteConfirmation.value = false;
    
    // Event emittieren, dass ein Fantasy Character gelöscht wurde
    eventBus.emit('fantasy-character-deleted', character.value.id);
    
    router.push({ name: 'home' }); // Zurück zur Übersicht nach dem Löschen
  } catch (err) {
    console.error('Fehler beim Löschen des Fantasy Characters:', err);
  } finally {
    isDeleting.value = false;
  }
}

// Open edit dialog
function openEditDialog() {
  showEditDialog.value = true;
}

// Handle character update
function handleCharacterUpdated(updatedCharacter: FantasyCharacter) {
  // Update the character data in the view
  if (character.value && updatedCharacter) {
    character.value = updatedCharacter;
  }
}

// Listen for events from eventBus
onMounted(() => {
  eventBus.on('fantasy-character-updated', (updatedCharacter: FantasyCharacter) => {
    if (character.value && updatedCharacter.id === character.value.id) {
      character.value = updatedCharacter;
    }
  });
});
</script>

<style scoped>
/* Nur Hilfsstile behalten, die nicht direkt über Vuetify zugänglich sind */
.transition-smooth {
  transition: all 0.8s ease;
}

.white-space-pre-line {
  white-space: pre-line;
}

.transition-transform {
  transition: transform 0.3s ease;
}

.transition-transform:hover {
  transform: translateX(-5px);
}

.transform-scale {
  transition: transform 0.5s ease;
}

.transform-scale:hover {
  transform: scale(1.1) !important;
}
</style>