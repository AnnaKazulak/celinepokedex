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
      
      <!-- Desktop buttons -->
      <div class="d-none d-sm-flex">
        <v-btn
          class="mr-2"
          :class="{ 'text-white': isScrolled || isDetailPage }"
          color="black"
          :elevation="isScrolled || isDetailPage ? 2 : 1"
          rounded="pill"
          prepend-icon="mdi-magic-staff"
          @click="showFantasyDialog = true"
        >
          Fantasy Character
        </v-btn>
        <v-btn
          :class="{ 'text-white': isScrolled || isDetailPage }"
          color="black"
          :elevation="isScrolled || isDetailPage ? 2 : 1"
          rounded="pill"
          prepend-icon="mdi-plus"
          @click="showDialog = true"
        >
          Pokémon
        </v-btn>
      </div>
      
      <!-- Mobile hamburger menu -->
      <div class="d-sm-none">
        <v-menu 
          location="bottom end"
          transition="slide-y-transition"
        >
          <template v-slot:activator="{ props }">
            <v-btn
              v-bind="props"
              icon
              :class="{ 'text-white': isScrolled || isDetailPage }"
              :elevation="isScrolled || isDetailPage ? 2 : 1"
            >
              <v-icon>mdi-menu</v-icon>
            </v-btn>
          </template>
          <v-list>
            <v-list-item @click="showFantasyDialog = true">
              <v-list-item-title>
                <v-icon start>mdi-magic-staff</v-icon>
                Fantasy Character
              </v-list-item-title>
            </v-list-item>
            <v-list-item @click="showDialog = true">
              <v-list-item-title>
                <v-icon start>mdi-plus</v-icon>
                Pokémon
              </v-list-item-title>
            </v-list-item>
          </v-list>
        </v-menu>
      </div>
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
import { ref, onMounted, onBeforeUnmount, computed, watch } from 'vue';
import { useRoute } from 'vue-router';
import PokemonFormDialog from './pokemon/PokemonFormDialog.vue';
import FantasyCharacterDialog from './fantasy/FantasyCharacterDialog.vue';
import type { Pokemon } from '../types/pokemon';
import { POKEMON_COLORS } from '../utils/constants';
import { eventBus } from '../utils/eventBus';

// Zustand für die Dialoge
const showDialog = ref(false);
const showFantasyDialog = ref(false);

// Route für Prüfung, ob wir auf der Detailseite sind
const route = useRoute();

// Farben für die Navbar
const psychicColor = POKEMON_COLORS.PRIMARY_BLUE;
const ghostColor = POKEMON_COLORS.PRIMARY_LILAC;

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

// Watch for route changes
watch(
  () => route.name,
  (newRouteName) => {
    // If we're navigating to the home view, reset the detail page color
    if (newRouteName === 'home') {
      detailPageColor.value = '';
      // Reset active color as well to avoid sticking with old detail color
      activeColor.value = '';
      useDynamicColor.value = false;
      
      // After a short delay, trigger scroll handler to ensure DOM is ready
      setTimeout(() => {
        handleScroll();
      }, 200);
    }
  }
);

// Extra handler for home view specific full reset
eventBus.on('home-view-mounted', () => {
  // Reset all dynamic colors
  detailPageColor.value = '';
  activeColor.value = '';
  
  // Force refresh of scroll handler after DOM is fully ready
  setTimeout(() => {
    handleScroll();
  }, 300);
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
  else if (isScrolled.value && useDynamicColor.value && activeColor.value) {
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

// Complete reset of all navbar color-related states
function resetNavbarColors() {
  // Reset all color variables
  detailPageColor.value = '';
  activeColor.value = '';
  useDynamicColor.value = false;
  
  // Re-evaluate cards in view after a delay to ensure DOM is ready
  setTimeout(() => {
    handleScroll();
  }, 300);
}

// Watch for route changes with immediate effect for reliable detection
watch(() => route.name, 
  (newRouteName) => {
    if (newRouteName === 'home') {
      resetNavbarColors();
    }
  },
  { immediate: true }
);

// Event handler for direct reset from home view
eventBus.on('home-view-mounted', () => {
  resetNavbarColors();
});

// Scroll-Event-Handler mit verbessertem Matching-Algorithmus
const handleScroll = () => {
  // Ab 50px Scroll-Position wird die Navbar als gescrollt betrachtet
  const wasScrolled = isScrolled.value;
  isScrolled.value = window.scrollY > 50;
  
  // Nur fortfahren, wenn wir gescrollt haben
  if (isScrolled.value) {
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
};

// Event-Listener beim Mounten hinzufügen
onMounted(() => {
  window.addEventListener('scroll', handleScroll);
  window.addEventListener('resize', checkScreenSize);
  eventBus.on('register-pokemon-color', handleRegisterPokemonColor);
  eventBus.on('register-fantasy-color', handleRegisterFantasyColor);
  eventBus.on('detail-page-color-change', handleDetailPageColorChange);
  
  // New direct reset handler that forces a complete navbar state reset
  eventBus.on('force-navbar-reset', forceNavbarReset);

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
  eventBus.off('force-navbar-reset', forceNavbarReset);
});

// Direct forceful reset of navbar that guarantees color updates
function forceNavbarReset() {
  console.log('Forcing navbar reset...');
  // Clear all state variables
  detailPageColor.value = '';
  activeColor.value = '';
  useDynamicColor.value = false;
  
  // Force immediate update of the scroll status to trigger navbar background update
  isScrolled.value = window.scrollY > 50;
  
  // Clear and rebuild the color map
  pokemonColors.value.clear();
  
  // Multiple delayed calls to handle scroll with increasing delays
  // This ensures we catch cards as they appear in the DOM
  const delays = [100, 300, 600, 1000];
  delays.forEach(delay => {
    setTimeout(() => {
      handleScroll();
    }, delay);
  });
}

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