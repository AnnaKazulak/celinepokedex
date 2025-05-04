<template>
  <div class="navbar-container">
    <v-app-bar
      class="pokemon-navbar"
      :class="{ 'scrolled': isScrolled || isDetailPage }"
      :style="{ background: navbarBackground }"
      elevation="0"
      fixed
    >
      <div class="d-flex align-center">
        <router-link to="/" class="home-link">
          <div class="logo-container" :class="{ 'logo-scrolled': isScrolled || isDetailPage }">
            <v-img
              alt="Pokemon Logo"
              class="pokemon-logo"
              contain
              src="/src/assets/celinepokedex-logo-1.jpg"
              transition="scale-transition"
              width="48"
            />
          </div>
          <v-toolbar-title class="app-title" :class="{ 'title-scrolled': isScrolled || isDetailPage }">
            Céline Pokédex
          </v-toolbar-title>
        </router-link>
      </div>

      <v-spacer></v-spacer>
      <v-btn
        class="fantasy-button mr-2"
        :class="{ 'button-scrolled': isScrolled || isDetailPage }"
        elevation="0"
        prepend-icon="mdi-magic-staff"
        @click="showFantasyDialog = true"
      >
        Fantasy Character
      </v-btn>
      <v-btn
        class="create-button"
        :class="{ 'button-scrolled': isScrolled || isDetailPage }"
        elevation="0"
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
  return route.name === 'pokemonDetail' && !!detailPageColor.value;
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
    pokemonColors.value.set(data.id, {
      color: data.color,
      element: data.element
    });
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
.navbar-container {
  position: relative;
  z-index: 100; /* Erhöht von 5 auf 100, damit die Navbar über allen Elementen liegt */
}

.pokemon-navbar {
  padding: 0 24px;
  box-shadow: none !important;
  height: 68px !important;
  transition: all 0.5s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.pokemon-navbar.scrolled {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2) !important;
}

.logo-container {
  position: relative;
  width: 48px;
  height: 48px;
  margin-right: 12px;
  overflow: hidden;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.1);
  box-shadow: none;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.5s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.logo-container.logo-scrolled {
  background-color: rgba(255, 255, 255, 0.2);
  box-shadow: 0 3px 8px rgba(0, 0, 0, 0.15);
}

.logo-container:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.25);
}

.pokemon-logo {
  transition: all 0.3s ease;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
}

.pokemon-logo:hover {
  transform: scale(1.1);
}

.app-title {
  color: rgba(255, 255, 255, 0.8);
  font-size: 1.5rem;
  font-weight: 600;
  margin-left: 8px;
  letter-spacing: 0.5px;
  text-shadow: none;
  white-space: nowrap;
  overflow: visible;
  transition: all 0.5s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.app-title.title-scrolled {
  color: white;
  font-weight: 700;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.15);
}

.create-button {
  background-color: white !important;
  color: v-bind(psychicColor) !important;
  border-radius: 28px;
  font-weight: 600;
  padding: 0 20px;
  height: 42px;
  letter-spacing: 0.5px;
  text-transform: none;
  box-shadow: 0 3px 6px rgba(0, 0, 0, 0.1) !important;
  transition: all 0.5s cubic-bezier(0.25, 0.46, 0.45, 0.94) !important;
  opacity: 0.9;
}

.create-button.button-scrolled {
  background-color: white !important;
  color: v-bind(psychicColor) !important;
  font-weight: 600;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15) !important;
  opacity: 1;
}

.create-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15) !important;
}

.create-button:active {
  transform: translateY(0);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1) !important;
}

.fantasy-button {
  background-color: white !important;
  color: v-bind(ghostColor) !important;
  border-radius: 28px;
  font-weight: 600;
  padding: 0 20px;
  height: 42px;
  letter-spacing: 0.5px;
  text-transform: none;
  box-shadow: 0 3px 6px rgba(0, 0, 0, 0.1) !important;
  transition: all 0.5s cubic-bezier(0.25, 0.46, 0.45, 0.94) !important;
  opacity: 0.9;
}

.fantasy-button.button-scrolled {
  background-color: white !important;
  color: v-bind(ghostColor) !important;
  font-weight: 600;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15) !important;
  opacity: 1;
}

.fantasy-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15) !important;
}

.fantasy-button:active {
  transform: translateY(0);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1) !important;
}

.home-link {
  display: flex;
  align-items: center;
  text-decoration: none;
  cursor: pointer;
}

.home-link:hover {
  text-decoration: none;
}

@media (max-width: 600px) {
  .pokemon-navbar {
    padding: 0 16px;
    height: 60px !important;
  }
  
  .app-title {
    font-size: 1.2rem;
  }
  
  .create-button {
    font-size: 0.85rem;
    padding: 0 12px;
    height: 36px;
  }
  
  .logo-container {
    width: 38px;
    height: 38px;
    margin-right: 8px;
  }

  .pokemon-logo {
    width: 38px;
  }

  .fantasy-button {
    font-size: 0.85rem;
    padding: 0 12px;
    height: 36px;
    margin-right: 8px;
  }
}
</style>