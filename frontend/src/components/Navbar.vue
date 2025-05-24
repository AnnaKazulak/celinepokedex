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
      
      <!-- Desktop buttons with role-based permissions -->
      <div class="d-none d-sm-flex">
        <template v-if="isLoggedIn">
          <!-- Only admin can create fantasy characters -->
          <v-btn
            v-if="isAdmin"
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
          
          <!-- All users can create Pokémon -->
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
          
          <!-- Show username badge for logged in users -->
          <v-chip
            class="ml-2"
            :color="isAdmin ? 'red-darken-2' : 'green-darken-2'"
            text-color="white"
            size="small"
            label
          >
            <v-icon start size="small">{{ isAdmin ? 'mdi-shield-account' : 'mdi-account' }}</v-icon>
            {{ currentUsername }}
          </v-chip>
        </template>
        
        <v-btn
          v-if="!isLoggedIn"
          :class="{ 'text-white': isScrolled || isDetailPage }"
          color="primary"
          :elevation="isScrolled || isDetailPage ? 2 : 1"
          rounded="pill"
          prepend-icon="mdi-login"
          @click="handleLogin"
        >
          Login
        </v-btn>
        <v-btn
          v-else
          :class="{ 'text-white': isScrolled || isDetailPage }"
          color="secondary"
          :elevation="isScrolled || isDetailPage ? 2 : 1"
          rounded="pill"
          prepend-icon="mdi-logout"
          @click="handleLogout"
        >
          Logout
        </v-btn>
      </div>
      
      <!-- Mobile hamburger menu with role-based permissions -->
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
            <template v-if="isLoggedIn">
              <!-- User info in mobile menu -->
              <v-list-item>
                <v-list-item-title>
                  <v-chip
                    :color="isAdmin ? 'red-darken-2' : 'green-darken-2'"
                    text-color="white"
                    size="small"
                    label
                  >
                    <v-icon start size="small">{{ isAdmin ? 'mdi-shield-account' : 'mdi-account' }}</v-icon>
                    {{ currentUsername }}
                  </v-chip>
                </v-list-item-title>
              </v-list-item>
              
              <!-- Only admin can create fantasy characters -->
              <v-list-item v-if="isAdmin" @click="showFantasyDialog = true">
                <v-list-item-title>
                  <v-icon start>mdi-magic-staff</v-icon>
                  Fantasy Character
                </v-list-item-title>
              </v-list-item>
              
              <!-- All users can create Pokémon -->
              <v-list-item @click="showDialog = true">
                <v-list-item-title>
                  <v-icon start>mdi-plus</v-icon>
                  Pokémon
                </v-list-item-title>
              </v-list-item>
              <v-divider></v-divider>
            </template>
            
            <v-list-item v-if="!isLoggedIn" @click="handleLogin">
              <v-list-item-title>
                <v-icon start>mdi-login</v-icon>
                Login
              </v-list-item-title>
            </v-list-item>
            <v-list-item v-else @click="handleLogout">
              <v-list-item-title>
                <v-icon start>mdi-logout</v-icon>
                Logout
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
    
    <!-- Login Dialog -->
    <v-dialog v-model="showLoginDialog" max-width="450">
      <v-card class="login-card">
        <v-img
          height="80"
          class="login-header-image"
          :src="'/src/assets/celinepokedex-logo-1.jpg'"
          gradient="to top, rgba(0,0,0,.4), rgba(0,0,0,.1)"
          cover
        >
          <div class="d-flex fill-height align-center justify-center">
            <v-card-title class="text-white text-h4 font-weight-bold">
              Login
            </v-card-title>
          </div>
        </v-img>
        
        <v-card-text class="pt-4">
          <v-form @submit.prevent="submitLogin()" ref="formValidator">
            <v-text-field
              v-model="loginForm.username"
              label="Username"
              name="username"
              autocomplete="username"
              variant="outlined"
              required
              :rules="[v => !!v || 'Username is required']"
              prepend-inner-icon="mdi-account"
              class="mb-2"
            ></v-text-field>
            
            <v-text-field
              v-model="loginForm.password"
              label="Password"
              name="password"
              autocomplete="current-password"
              variant="outlined"
              :append-inner-icon="showPassword ? 'mdi-eye-off' : 'mdi-eye'"
              @click:append-inner="showPassword = !showPassword"
              :type="showPassword ? 'text' : 'password'"
              required
              :rules="[v => !!v || 'Password is required']"
              prepend-inner-icon="mdi-lock"
              class="mb-2"
            ></v-text-field>
            
            <div class="d-flex align-center justify-space-between mt-2">
              <v-checkbox 
                v-model="rememberMe" 
                label="Remember me" 
                color="primary"
                hide-details
                density="compact"
              ></v-checkbox>
            </div>
            
            <v-alert
              v-if="loginError"
              type="error"
              class="mt-4 mb-2"
              variant="tonal"
              density="compact"
            >
              {{ loginError }}
            </v-alert>
          </v-form>
        </v-card-text>
        
        <v-card-actions class="pb-4 px-4">
          <v-spacer></v-spacer>
          <v-btn 
            color="grey-darken-1" 
            variant="text" 
            @click="cancelLogin"
            :disabled="isLoading"
          >
            Cancel
          </v-btn>
          <v-btn 
            color="primary" 
            @click="submitLogin()" 
            :loading="isLoading"
            variant="elevated"
          >
            Login
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
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
import axiosInstance from '../utils/axiosConfig';


// Define fantasy character interface
interface FantasyCharacter {
  id: number;
  name: string;
  image: string;
  description: string;
  type: string;
}

// Extend Window interface to include our custom properties
declare global {
  interface Window {
    mockFantasyData?: FantasyCharacter[];
    originalFetch?: typeof fetch;
  }
}

// Constants for storage keys to avoid typos and make changes easier
const STORAGE_KEYS = {
  USER_SESSION: 'celinepokedex_user_session',
  REGISTERED_USERS: 'celinepokedex_registered_users', 
}


// Auth state
const isLoggedIn = ref(false);
const isAdmin = ref(false);
const currentUsername = ref('');
const showLoginDialog = ref(false);
const loginForm = ref({
  username: '',
  password: ''
});
const loginError = ref('');
const showPassword = ref(false);
const rememberMe = ref(false);
const isLoading = ref(false);
const formValidator = ref<null | any>(null);

// Zustand für die Dialoge
const showDialog = ref(false);
const showFantasyDialog = ref(false);

// Route für Prüfung, ob wir auf der Detailseite sind
const route = useRoute();

// Clean form and close dialog
function cancelLogin() {
  showLoginDialog.value = false;
  loginForm.value = { username: '', password: '' };
  loginError.value = '';
}

// Login/Logout handlers
function handleLogin() {
  showLoginDialog.value = true;
  loginError.value = '';
}

function handleLogout() {
  isLoggedIn.value = false;
  isAdmin.value = false;
  currentUsername.value = '';
  
  // Clear all storage
  localStorage.removeItem(STORAGE_KEYS.USER_SESSION);
  sessionStorage.removeItem(STORAGE_KEYS.USER_SESSION);
  
  console.log('User logged out');
  eventBus.emit('user-logged-out');
}

// Login handler with backend API request
async function submitLogin() {
  if (!loginForm.value.username || !loginForm.value.password) {
    loginError.value = 'Please enter both username and password';
    return;
  }
  
  isLoading.value = true;
  
  try {
    // Send login request to backend
    const response = await axiosInstance.post('/auth/login', {
      username: loginForm.value.username,
      password: loginForm.value.password
    });
    
    // Process response from backend
    if (response.data && response.data.token) {
      // Create session data
      const sessionData = {
        username: response.data.username || loginForm.value.username,
        role: response.data.role || 'admin', // Default to admin as per requirements
        token: response.data.token,
        timestamp: Date.now()
      };
      
      // Store in localStorage or sessionStorage based on rememberMe
      if (rememberMe.value) {
        localStorage.setItem(STORAGE_KEYS.USER_SESSION, JSON.stringify(sessionData));
      } else {
        sessionStorage.setItem(STORAGE_KEYS.USER_SESSION, JSON.stringify(sessionData));
      }
      
      // Update reactive state
      isLoggedIn.value = true;
      isAdmin.value = sessionData.role === 'admin';
      currentUsername.value = sessionData.username;
      
      // Close dialog and reset form
      showLoginDialog.value = false;
      loginForm.value = { username: '', password: '' };
      console.log('Login successful');
    } else {
      throw new Error('Invalid response from server');
    }
  } catch (error: any) {
    // Handle login errors
    console.error('Login error:', error);
    if (error.response && error.response.data && error.response.data.message) {
      loginError.value = error.response.data.message;
    } else {
      loginError.value = error.message || 'Login failed. Please try again.';
    }
  } finally {
    isLoading.value = false;
  }
}

// Check authentication status on component mount
function checkLoginStatus() {
  // Get session data from storage
  const sessionData = JSON.parse(
    localStorage.getItem(STORAGE_KEYS.USER_SESSION) || 
    sessionStorage.getItem(STORAGE_KEYS.USER_SESSION) || 
    'null'
  );
  
  if (sessionData && sessionData.token) {
    isLoggedIn.value = true;
    isAdmin.value = sessionData.role === 'admin';
    currentUsername.value = sessionData.username;
    console.log(`User session restored: ${sessionData.username} (${sessionData.role})`);
  }
}

// Initialize user storage if needed
function initializeUserStorage() {
  if (!localStorage.getItem('celinepokedex_registered_users')) {
    localStorage.setItem('celinepokedex_registered_users', '{}');
    console.log('User storage initialized');
  }
}

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

// Extra handler for home view specific full reset
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

// Handle session expired event
function handleSessionExpired(data: { message: string }) {
  // Reset login state
  isLoggedIn.value = false;
  isAdmin.value = false;
  currentUsername.value = '';
  
  // Set error message for login dialog
  loginError.value = data.message;
  
  // Show login dialog
  showLoginDialog.value = true;
  
  console.log('Session expired, showing login dialog');
}

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

// Event-Listener beim Mounten hinzufügen
onMounted(() => {
  // Initialize authentication system
  initializeUserStorage();
  checkLoginStatus();
  
  // Initialize registered users storage if not exists
  if (!localStorage.getItem('celinepokedex_registered_users')) {
    localStorage.setItem('celinepokedex_registered_users', '{}');
  }
  
  window.addEventListener('scroll', handleScroll);
  window.addEventListener('resize', checkScreenSize);
  eventBus.on('register-pokemon-color', handleRegisterPokemonColor);
  eventBus.on('register-fantasy-color', handleRegisterFantasyColor);
  eventBus.on('detail-page-color-change', handleDetailPageColorChange);
  
  // New direct reset handler that forces a complete navbar state reset
  eventBus.on('force-navbar-reset', forceNavbarReset);
  
  // Listen for session expired events from axios interceptor
  eventBus.on('session-expired', handleSessionExpired);

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
  eventBus.off('session-expired', handleSessionExpired);
});
</script>

<style scoped>
/* Keep only the navbar container with z-index for proper layering */
.navbar-container {
  position: relative;
  z-index: 100;
}

/* Login form styling */
.login-card {
  border-radius: 16px;
  overflow: hidden;
}

.login-header-image {
  position: relative;
}

/* Mobile responsive adjustments */
@media (max-width: 600px) {
  /* Removed all custom styles as they're now handled by Vuetify classes */
}
</style>