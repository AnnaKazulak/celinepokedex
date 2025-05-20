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
        <template v-if="isLoggedIn">
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
            <template v-if="isLoggedIn">
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
              {{ isRegistering ? 'Register' : 'Login' }}
            </v-card-title>
          </div>
        </v-img>
        
        <v-card-text class="pt-4">
          <v-form @submit.prevent="isRegistering ? handleRegister() : submitLogin()" ref="formValidator">
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
            
            <v-expand-transition>
              <v-text-field
                v-if="isRegistering"
                v-model="loginForm.confirmPassword"
                label="Confirm Password"
                name="confirmPassword"
                variant="outlined"
                :append-inner-icon="showPassword ? 'mdi-eye-off' : 'mdi-eye'"
                @click:append-inner="showPassword = !showPassword"
                :type="showPassword ? 'text' : 'password'"
                required
                :rules="[
                  v => !!v || 'Please confirm your password',
                  v => v === loginForm.password || 'Passwords do not match'
                ]"
                prepend-inner-icon="mdi-lock-check"
                class="mb-2"
              ></v-text-field>
            </v-expand-transition>
            
            <div class="d-flex align-center justify-space-between mt-2">
              <v-checkbox 
                v-model="rememberMe" 
                label="Remember me" 
                color="primary"
                hide-details
                density="compact"
              ></v-checkbox>
              
              <v-btn 
                variant="text" 
                size="small" 
                color="primary" 
                @click="toggleRegistration"
                :disabled="isLoading"
                class="text-body-2"
              >
                {{ isRegistering ? 'Have an account?' : 'Need an account?' }}
              </v-btn>
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
            @click="isRegistering ? handleRegister() : submitLogin()" 
            :loading="isLoading"
            variant="elevated"
          >
            {{ isRegistering ? 'Register' : 'Login' }}
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

// Auth state
const isLoggedIn = ref(false);
const showLoginDialog = ref(false);
const loginForm = ref({
  username: '',
  password: '',
  confirmPassword: ''
});
const loginError = ref('');
const showPassword = ref(false);
const isRegistering = ref(false);
const rememberMe = ref(false);
const isLoading = ref(false);
const formValidator = ref<null | any>(null); // Renamed from loginForm to formValidator

// Toggle between login and registration forms
function toggleRegistration() {
  isRegistering.value = !isRegistering.value;
  loginError.value = '';
  loginForm.value = { username: '', password: '', confirmPassword: '' };
}

// Clean form and close dialog
function cancelLogin() {
  showLoginDialog.value = false;
  loginForm.value = { username: '', password: '', confirmPassword: '' };
  loginError.value = '';
  isRegistering.value = false;
}

// Login/Logout handlers
function handleLogin() {
  showLoginDialog.value = true;
  loginError.value = '';
  isRegistering.value = false;
}

function handleLogout() {
  isLoggedIn.value = false;
  // Handle secure logout
  if (rememberMe.value) {
    // Just remove the session marker but keep username for convenience
    localStorage.removeItem('userSession');
  } else {
    // Clear all auth data
    localStorage.removeItem('userSession');
    localStorage.removeItem('username');
  }
  console.log('User logged out');
  eventBus.emit('user-logged-out');
}

// Registration handler
async function handleRegister() {
  if (!loginForm.value.username || !loginForm.value.password) {
    loginError.value = 'Please fill in all required fields';
    return;
  }

  if (loginForm.value.password !== loginForm.value.confirmPassword) {
    loginError.value = 'Passwords do not match';
    return;
  }

  isLoading.value = true;
  
  try {
    // Simulate API call with delay
    await new Promise(resolve => setTimeout(resolve, 800));
    
    // Check if username is already taken (in a real app, this would be an API call)
    // For demo purposes, only 'admin' is considered taken if not the current user
    if (loginForm.value.username === 'admin' && localStorage.getItem('username') !== 'admin') {
      throw new Error('Username already exists');
    }
    
    // Store user info - in a real app this would be handled by a secure backend
    // We'll use a simplified secure approach with localStorage
    const hashedPassword = await hashPassword(loginForm.value.password);
    
    // Store registration
    localStorage.setItem('username', loginForm.value.username);
    localStorage.setItem('hashedPassword', hashedPassword);
    
    // Auto login after successful registration
    isLoggedIn.value = true;
    createSession(rememberMe.value);
    
    // Close dialog and reset form
    showLoginDialog.value = false;
    loginForm.value = { username: '', password: '', confirmPassword: '' };
    
    eventBus.emit('user-registered-and-logged-in');
    console.log('User registered and logged in successfully');
    
  } catch (error) {
    loginError.value = error instanceof Error ? error.message : 'Registration failed';
    console.error('Registration error:', error);
  } finally {
    isLoading.value = false;
  }
}

// Create a session token and store it
function createSession(remember = false) {
  // Create a simple session token (in a real app would be JWT or similar)
  const sessionToken = `${Date.now()}_${Math.random().toString(36).substring(2, 15)}`;
  
  if (remember) {
    // Long-lived session with more persistent storage
    localStorage.setItem('userSession', sessionToken);
    localStorage.setItem('username', loginForm.value.username);
  } else {
    // Session storage only (cleared when browser is closed)
    sessionStorage.setItem('userSession', sessionToken);
    localStorage.setItem('username', loginForm.value.username);
  }
}

// Simple password hashing simulation
// In a real app, this would be done server-side with proper algorithms
async function hashPassword(password: string): Promise<string> {
  // We're using Web Crypto API to create a simple hash
  // This is still not secure for production but better than plaintext
  const encoder = new TextEncoder();
  const data = encoder.encode(password + 'celinepokedex-salt');
  const hashBuffer = await crypto.subtle.digest('SHA-256', data);
  const hashArray = Array.from(new Uint8Array(hashBuffer));
  const hashHex = hashArray.map(b => b.toString(16).padStart(2, '0')).join('');
  return hashHex;
}

async function submitLogin() {
  // Simple validation
  if (!loginForm.value.username || !loginForm.value.password) {
    loginError.value = 'Please enter both username and password';
    return;
  }
  
  isLoading.value = true;
  
  try {
    // Add a slight delay to simulate server request
    await new Promise(resolve => setTimeout(resolve, 600));
    
    // For demo purposes, allow both the hardcoded admin login and any registered users
    const storedUsername = localStorage.getItem('username');
    
    if (loginForm.value.username === 'admin' && loginForm.value.password === 'admin123') {
      // Special case for demo admin account
      isLoggedIn.value = true;
      localStorage.setItem('username', 'admin');
      createSession(rememberMe.value);
      
      showLoginDialog.value = false;
      loginForm.value = { username: '', password: '', confirmPassword: '' };
      console.log('Admin logged in successfully');
      eventBus.emit('user-logged-in');
    } 
    else if (storedUsername === loginForm.value.username) {
      // Registered user login - verify password hash
      const storedHash = localStorage.getItem('hashedPassword');
      const inputHash = await hashPassword(loginForm.value.password);
      
      if (storedHash === inputHash) {
        isLoggedIn.value = true;
        createSession(rememberMe.value);
        
        showLoginDialog.value = false;
        loginForm.value = { username: '', password: '', confirmPassword: '' };
        console.log('User logged in successfully');
        eventBus.emit('user-logged-in');
      } else {
        throw new Error('Invalid password');
      }
    } 
    else {
      throw new Error('User not found');
    }
  } catch (error) {
    loginError.value = error instanceof Error ? error.message : 'Invalid username or password';
    console.log('Login failed:', loginError.value);
  } finally {
    isLoading.value = false;
  }
}

// Check if user is logged in on component mount (from localStorage)
function checkLoginStatus() {
  // Check for active session in either localStorage (remember me) or sessionStorage
  const sessionToken = localStorage.getItem('userSession') || sessionStorage.getItem('userSession');
  
  if (sessionToken) {
    // Session exists, user is considered logged in
    isLoggedIn.value = true;
    console.log('User already logged in from previous session');
  } else {
    isLoggedIn.value = false;
  }
}

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
  checkLoginStatus();
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