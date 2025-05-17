<template>
  <v-btn
    v-show="showButton"
    fab
    :color="buttonColor"
    :class="['back-to-top-btn', {'drawer-open': isDrawerOpen}]"
    icon="mdi-arrow-up"
    @click="scrollToTop"
    :elevation="6"
    size="large"
    :style="buttonStyle"
    aria-label="Nach oben scrollen"
  ></v-btn>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, inject, watch } from 'vue';
import { useRoute } from 'vue-router';
import { eventBus } from '../utils/eventBus';

const showButton = ref(false);
const activeColor = ref('');
const isDetailPage = ref(false);
const detailPageColor = ref('');
const route = useRoute();
const isDrawerOpen = ref(false);

// Standard-Farbverlauf für Pokémon-Typen
const psychicColor = '#FF6FAD';
const ghostColor = '#8C50C7';

// Hilfsfunktion, um dunklere Farbversionen zu erstellen
const createDarkerShade = (color: string): string => {
  // Bestehende Farbe in RGB konvertieren
  if (!color || !color.startsWith('#')) return '#343434';
  
  // Extrahiere R, G, B Werte aus Hex-Farbe
  const r = parseInt(color.substring(1, 3), 16);
  const g = parseInt(color.substring(3, 5), 16);
  const b = parseInt(color.substring(5, 7), 16);
  
  // Dunklere Version erstellen (70% der Originalfarbe)
  const darkerR = Math.floor(r * 0.7).toString(16).padStart(2, '0');
  const darkerG = Math.floor(g * 0.7).toString(16).padStart(2, '0');
  const darkerB = Math.floor(b * 0.7).toString(16).padStart(2, '0');
  
  return `#${darkerR}${darkerG}${darkerB}`;
};

// Button-Hintergrund berechnen
const buttonStyle = computed(() => {
  // Wenn wir auf einer Detailseite sind
  if (isDetailPage.value && detailPageColor.value) {
    const darkColor = createDarkerShade(detailPageColor.value);
    return {
      background: `linear-gradient(135deg, ${detailPageColor.value} 0%, ${darkColor} 100%)`,
      color: 'white'
    };
  } 
  // Ansonsten die aktive Farbe oder den Standard verwenden
  else if (activeColor.value) {
    return { 
      background: activeColor.value,
      color: 'white'
    };
  } else {
    return { 
      background: `linear-gradient(135deg, ${psychicColor} 0%, ${ghostColor} 100%)`,
      color: 'white'
    };
  }
});

// Dummy-Eigenschaft für Vuetify (da wir :style verwenden)
const buttonColor = computed(() => 'transparent');

// Scroll-Event-Handler, der den Button anzeigt/versteckt
const handleScroll = () => {
  // Button anzeigen, wenn mehr als 300px gescrollt wurde
  showButton.value = window.scrollY > 300;
};

// Funktion zum Scrollen nach oben
const scrollToTop = () => {
  window.scrollTo({
    top: 0,
    behavior: 'smooth'
  });
};

// Event-Listener beim Komponenten-Mount hinzufügen
onMounted(() => {
  window.addEventListener('scroll', handleScroll);
  
  // Event-Listener für Farbänderungen in der App
  eventBus.on('color-changed', (color: string) => {
    activeColor.value = color;
  });
  
  // Prüfen, ob wir auf einer Detailseite sind
  isDetailPage.value = route.path.includes('/pokemon/') || route.path.includes('/character/');
  
  // Event-Listener für Detailseitenfarbe
  eventBus.on('detail-page-color', (color: string) => {
    detailPageColor.value = color;
  });
  
  // Event-Listener für Drawer-Status
  eventBus.on('drawer-state-changed', (open: boolean) => {
    isDrawerOpen.value = open;
  });
});

// Event-Listener beim Komponenten-Unmount entfernen
onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll);
  eventBus.off('color-changed');
  eventBus.off('detail-page-color');
  eventBus.off('drawer-state-changed');
});
</script>

<style scoped>
.back-to-top-btn {
  position: fixed;
  bottom: 20px;
  right: 20px;
  z-index: 99;
  transition: all 0.3s ease;
  color: white !important;
}

/* Anpassung für geöffneten Drawer */
.back-to-top-btn.drawer-open {
  right: 320px; /* 300px (Drawer-Breite) + 20px (Abstand) */
}

/* Media Query für kleine Bildschirme */
@media (max-width: 599px) {
  .back-to-top-btn.drawer-open {
    right: 20px; /* Auf kleinen Bildschirmen nicht verschieben, da Drawer als Overlay */
  }
}
</style>
