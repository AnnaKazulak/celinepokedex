<template>
  <v-app>
    <v-app-bar elevation="2">
      <v-app-bar-title>Responsive Drawer Example</v-app-bar-title>
      <v-spacer></v-spacer>
      
      <!-- Toggle Button for Drawer -->
      <v-btn
        icon
        @click="toggleDrawer"
        :variant="drawer ? 'elevated' : 'text'"
        color="primary"
        class="mr-2"
      >
        <v-icon>{{ drawer ? 'mdi-filter-off' : 'mdi-filter' }}</v-icon>
      </v-btn>
    </v-app-bar>
    
    <!-- Main Content Area -->
    <v-main :class="contentClass">
      <v-container fluid>
        <h1 class="mb-6">Main Content</h1>
        
        <v-row>
          <v-col cols="12">
            <v-card elevation="2" class="pa-4">
              <v-card-title>Content Area</v-card-title>
              <v-card-text>
                <p>This content will be pushed to the side when the drawer opens on desktop (md and larger screens).</p>
                <p>On mobile (sm and smaller), the drawer will overlay this content.</p>
                
                <v-btn
                  color="primary"
                  class="mt-4"
                  @click="toggleDrawer"
                >
                  {{ drawer ? 'Close Filter Panel' : 'Open Filter Panel' }}
                </v-btn>
              </v-card-text>
            </v-card>
          </v-col>
        </v-row>
      </v-container>
    </v-main>
    
    <!-- Responsive Drawer Component -->
    <ResponsiveDrawer
      v-model="drawer"
      @screen-size-changed="handleScreenSizeChange"
    >
      <!-- Drawer Content Here -->
      <div>
        <h3 class="text-h6 mb-4">Filter Options</h3>
        
        <v-list>
          <v-list-subheader>Categories</v-list-subheader>
          
          <v-list-item
            v-for="(item, i) in filterItems"
            :key="i"
            :value="item"
            active-color="primary"
          >
            <template v-slot:prepend>
              <v-icon :icon="item.icon"></v-icon>
            </template>
            <v-list-item-title v-text="item.title"></v-list-item-title>
          </v-list-item>
        </v-list>
        
        <v-divider class="my-4"></v-divider>
        
        <v-switch
          v-model="filterOptions.showActive"
          color="primary"
          label="Show active only"
        ></v-switch>
        
        <v-slider
          v-model="filterOptions.priceRange"
          label="Price Range"
          min="0"
          max="100"
          thumb-label
          class="mt-5"
        ></v-slider>
      </div>
      
      <!-- Optional Footer -->
      <template #footer>
        <div class="d-flex justify-space-between">
          <v-btn
            variant="text"
            color="error"
            @click="resetFilters"
          >
            Reset
          </v-btn>
          
          <v-btn
            color="primary"
            @click="applyFilters"
          >
            Apply
          </v-btn>
        </div>
      </template>
    </ResponsiveDrawer>
  </v-app>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useDisplay } from 'vuetify';
import ResponsiveDrawer from './ResponsiveDrawer.vue';

// Drawer open/close state
const drawer = ref(false);

// Use Vuetify's display composable
const { mdAndUp } = useDisplay();

// Sample filter items for the drawer
const filterItems = [
  { title: 'All Items', icon: 'mdi-view-grid' },
  { title: 'Category 1', icon: 'mdi-shape' },
  { title: 'Category 2', icon: 'mdi-star' },
  { title: 'Category 3', icon: 'mdi-heart' },
];

// Sample filter options
const filterOptions = ref({
  showActive: true,
  priceRange: 50,
});

// Toggle the drawer
function toggleDrawer() {
  drawer.value = !drawer.value;
}

// Reset all filters
function resetFilters() {
  filterOptions.value = {
    showActive: false,
    priceRange: 0,
  };
}

// Apply filters and close on mobile
function applyFilters() {
  console.log('Applying filters:', filterOptions.value);
  // On mobile, we might want to close the drawer after applying
  if (!mdAndUp.value) {
    drawer.value = false;
  }
}

// Handle screen size changes
function handleScreenSizeChange(isLargeScreen: boolean) {
  console.log('Screen size changed, is large screen:', isLargeScreen);
}

// Dynamic class for content to adjust padding when drawer is open on desktop
const contentClass = computed(() => {
  return {
    'content-with-drawer': drawer.value && mdAndUp.value
  };
});

// Open drawer by default on desktop for demo purposes
onMounted(() => {
  if (mdAndUp.value) {
    drawer.value = true;
  }
});
</script>

<style>
/* Apply padding to main content when drawer is open on desktop */
.content-with-drawer {
  padding-right: 300px !important; /* Match drawer width */
  transition: padding-right 0.3s ease;
}

/* When drawer is closed, remove the padding */
.v-main {
  transition: padding-right 0.3s ease;
  padding-right: 0;
}
</style>
