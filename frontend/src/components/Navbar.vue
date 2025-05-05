<template>
  <div class="navbar-container">
    <v-app-bar
      :class="{ 'bg-transparent': !isScrolled && !isDetailPage }"
      :style="isScrolled || isDetailPage ? { background: navbarBackground } : {}"
      :elevation="isScrolled || isDetailPage ? 1 : 0"
      app
      fixed
    >
      <div class="d-flex align-center">
        <router-link to="/" class="text-decoration-none d-flex align-center">
          <v-avatar
            :class="[
              'mr-3',
              { 'bg-white-darken-1': !isScrolled && !isDetailPage, 'bg-white-darken-2': isScrolled || isDetailPage }
            ]"
            :elevation="isScrolled || isDetailPage ? 2 : 0"
            size="48"
          >
            <v-img
              alt="Pokemon Logo"
              contain
              src="/src/assets/celinepokedex-logo-1.jpg"
              transition="scale-transition"
            />
          </v-avatar>
          <v-toolbar-title 
            :class="[
              'text-h6 font-weight-medium',
              { 'text-white-darken-1': !isScrolled && !isDetailPage, 'text-white font-weight-bold': isScrolled || isDetailPage }
            ]"
          >
            Céline Pokédex
          </v-toolbar-title>
        </router-link>
      </div>

      <v-spacer></v-spacer>
      <v-btn
        class="mr-2"
        :class="{ 'text-white': isScrolled || isDetailPage }"
        color="white"
        :elevation="isScrolled || isDetailPage ? 2 : 1"
        rounded="pill"
        prepend-icon="mdi-magic-staff"
        @click="showFantasyDialog = true"
      >
        Fantasy Character
      </v-btn>
      <v-btn
        :class="{ 'text-white': isScrolled || isDetailPage }"
        color="white"
        :elevation="isScrolled || isDetailPage ? 2 : 1"
        rounded="pill"
        prepend-icon="mdi-plus"
        @click="showDialog = true"
      >
        Pokémon
      </v-btn>
    </v-app-bar>
    
    <PokemonFormDialog 
      v-model:dialog="showDialog"
      @pokemon-created="handlePokemonCreated"
    />
    
    <FantasyCharacterDialog
      v-model:dialog="showFantasyDialog"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, computed } from 'vue';
import { useRoute } from 'vue-router';
import PokemonFormDialog from './PokemonFormDialog.vue';
import FantasyCharacterDialog from './FantasyCharacterDialog.vue';
import type { Pokemon } from '../types/pokemon';
import { POKEMON_COLORS } from '../utils/constants';
import { eventBus } from '../utils/eventBus';

// Zustand für die Dialoge
const showDialog = ref(false);
const showFantasyDialog = ref(false);

// Route für Prüfung, ob wir auf der Detailseite sind
const route = useRoute();

// Farben für die Navbar
const psychicColor = POKEMON_COLORS.PSYCHIC_VIOLET;
const ghostColor = POKEMON_COLORS.GHOST_PURPLE;

// Scroll-Status
const isScrolled = ref(false);
// Aktuelle Farbe der Navbar
const activeColor = ref('');
// Soll dynamische Farbänderung verwendet werden?
const useDynamicColor = ref(false);
// Nur für Mobilgeräte aktivieren
const isMobileView = ref(false);
// Detailseiten-Farbe (für PokemonDetailView)
const detailPageColor = ref('');
// Sind wir auf der Detailseite?
const isDetailPage = computed(() => {
  return (route.name === 'pokemonDetail' || route.name === 'fantasyCharacterDetail') && !!detailPageColor.value;
});

// Liste aller Pokémon-Farben und ihre DOM-Elemente
const pokemonColors = ref<Map<string | number, { color: string; element: HTMLElement | null }>>(new Map());

// Berechneter Navbar-Hintergrund
const navbarBackground = computed(() => {
  // Wenn wir auf der Detailseite sind und eine Farbe haben
  if (isDetailPage.value) {
    // Dunklere Version für Farbverlauf erstellen
    const darkColor = createDarkerShade(detailPageColor.value);
    return `linear-gradient(135deg, ${detailPageColor.value} 0%, ${darkColor} 100%)`;
  }
  // Normale Logik für die HomeView
  else if (isScrolled.value && useDynamicColor.value && activeColor.value && isMobileView.value) {
    return activeColor.value;
  } else if (isScrolled.value) {
    return `linear-gradient(135deg, ${psychicColor} 0%, ${ghostColor} 100%)`;
  }
  // Transparenter Hintergrund, wenn nicht gescrollt
  return 'transparent';
});

// Event-Handler für die Registrierung von Pokémon-Farben
const handleRegisterPokemonColor = (data: {id: number | string; color: string; element: HTMLElement | null}) => {
  if (data.element) {
    // Sicherstellen, dass beim Hinzufügen neuer Farben kein Element überschrieben wird
    if (!pokemonColors.value.has(data.id)) {
      pokemonColors.value.set(data.id, {
        color: data.color,
        element: data.element
      });
    } else {
      // Aktualisiere nur die Farbe, nicht das Element, falls bereits vorhanden
      const existingData = pokemonColors.value.get(data.id);
      if (existingData) {
        pokemonColors.value.set(data.id, {
          color: data.color,
          element: existingData.element || data.element
        });
      }
    }
  }
};

// Event-Handler für die Registrierung von Fantasy-Charakter-Farben
const handleRegisterFantasyColor = (data: {id: number | string; color: string; element: HTMLElement | null}) => {
  if (data.element) {
    // Fantasy-Charaktere immer mit einem Präfix speichern, um Kollisionen zu vermeiden
    const fantasyId = `fantasy-${data.id}`;
    
    // Sicherstellen, dass beim Hinzufügen neuer Farben kein Element überschrieben wird
    if (!pokemonColors.value.has(fantasyId)) {
      pokemonColors.value.set(fantasyId, {
        color: data.color,
        element: data.element
      });
    } else {
      // Aktualisiere nur die Farbe, nicht das Element, falls bereits vorhanden
      const existingData = pokemonColors.value.get(fantasyId);
      if (existingData) {
        pokemonColors.value.set(fantasyId, {
          color: data.color,
          element: existingData.element || data.element
        });
      }
    }
  }
};

// Handler für die Detailseiten-Farbe
const handleDetailPageColorChange = (color: string) => {
  detailPageColor.value = color;
};

// Scroll-Event-Handler mit verbessertem Matching-Algorithmus
const handleScroll = () => {
  // Ab 50px Scroll-Position wird die Navbar als gescrollt betrachtet
  const wasScrolled = isScrolled.value;
  isScrolled.value = window.scrollY > 50;
  
  // Nur wenn wir auf Mobilgeräten sind und gescrollt haben
  if (isMobileView.value && isScrolled.value) {
    // Viewport-Mitte berechnen
    const viewportCenter = window.innerHeight / 2;
    
    // Variablen für die beste Übereinstimmung
    let bestMatch: HTMLElement | null = null;
    let smallestDistance = Infinity;
    let matchColor = '';
    
    // Alle Pokémon-Elemente durchgehen und prüfen, welches am nächsten
    // zur Mitte des Bildschirms ist
    pokemonColors.value.forEach((data, id) => {
      const element = data.element;
      if (!element) return;
      
      const rect = element.getBoundingClientRect();
      // Mittelpunkt des Elements berechnen
      const elementCenter = rect.top + rect.height / 2;
      // Entfernung zur Viewport-Mitte
      const distance = Math.abs(elementCenter - viewportCenter);
      
      // Wenn dieses Element näher an der Mitte ist als bisherige
      if (distance < smallestDistance && elementCenter > 0 && elementCenter < window.innerHeight) {
        bestMatch = element;
        smallestDistance = distance;
        matchColor = data.color;
      }
    });
    
    // Wenn wir eine gute Übereinstimmung gefunden haben, färben wir die Navbar
    if (bestMatch && matchColor) {
      // Farbverlauf von gefundener Farbe zu einer dunkleren Version
      const darkColor = createDarkerShade(matchColor);
      activeColor.value = `linear-gradient(135deg, ${matchColor} 0%, ${darkColor} 100%)`;
      useDynamicColor.value = true;
    } else {
      useDynamicColor.value = false;
    }
  } else if (!isScrolled.value) {
    // Wenn nicht gescrollt, keine dynamische Farbe verwenden
    useDynamicColor.value = false;
  }
};

// Erzeugt einen dunkleren Farbton für den Farbverlauf
function createDarkerShade(color: string): string {
  try {
    // Extrahiere RGB-Werte
    const rgbMatch = color.match(/rgb\((\d+),\s*(\d+),\s*(\d+)\)/);
    if (rgbMatch) {
      const r = Math.max(0, parseInt(rgbMatch[1]) - 40);
      const g = Math.max(0, parseInt(rgbMatch[2]) - 40);
      const b = Math.max(0, parseInt(rgbMatch[3]) - 40);
      return `rgb(${r}, ${g}, ${b})`;
    }
    // Falls es kein RGB-Format ist
    return color;
  } catch (e) {
    console.error('Fehler beim Erstellen des dunkleren Farbtons:', e);
    return color;
  }
}

// Bildschirmgröße prüfen
const checkScreenSize = () => {
  isMobileView.value = window.innerWidth <= 768;
  if (!isMobileView.value) {
    useDynamicColor.value = false;
  }
};

// Event-Listener beim Mounten hinzufügen
onMounted(() => {
  window.addEventListener('scroll', handleScroll);
  window.addEventListener('resize', checkScreenSize);
  eventBus.on('register-pokemon-color', handleRegisterPokemonColor);
  eventBus.on('register-fantasy-color', handleRegisterFantasyColor);
  eventBus.on('detail-page-color-change', handleDetailPageColorChange);

  // Initial prüfen
  checkScreenSize();
  handleScroll();
});

// Event-Listener beim Unmounten entfernen
onBeforeUnmount(() => {
  window.removeEventListener('scroll', handleScroll);
  window.removeEventListener('resize', checkScreenSize);
  eventBus.off('register-pokemon-color', handleRegisterPokemonColor);
  eventBus.off('register-fantasy-color', handleRegisterFantasyColor);
  eventBus.off('detail-page-color-change', handleDetailPageColorChange);
});

// Event, wenn ein Pokémon erstellt wurde
const handlePokemonCreated = (newPokemon: Pokemon) => {
  // Optional: Event an übergeordnete Komponenten weitergeben
  // oder einen Toast/Notification anzeigen
  console.log('Neues Pokémon erstellt:', newPokemon);
  
  // Eine Erfolgsmeldung könnte hier angezeigt werden
}
</script>

<style scoped>
/* Keep only the navbar container with z-index for proper layering */
.navbar-container {
  position: relative;
  z-index: 100;
}

/* Mobile responsive adjustments */
@media (max-width: 600px) {
  /* Removed all custom styles as they're now handled by Vuetify classes */
}
</style>