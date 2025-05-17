<template>
  <v-navigation-drawer
    v-model="drawerModel"
    location="end"
    :temporary="!isLargeScreen"
    width="300"
    class="responsive-drawer"
  >
    <v-card flat class="h-100 d-flex flex-column">
      <!-- Drawer Header -->
      <v-card-title class="bg-primary text-white py-4 px-6 d-flex align-center">
        <v-icon class="mr-2" color="white">mdi-tune</v-icon>
        Filter & Sortierung
        <v-spacer></v-spacer>
        <v-btn
          icon="mdi-close"
          variant="text"
          color="white"
          size="small"
          @click="closeDrawer"
        ></v-btn>
      </v-card-title>
      
      <v-divider></v-divider>
      
      <!-- Drawer Content -->
      <v-card-text class="py-4 px-4 flex-grow-1 overflow-y-auto">
        <slot>
          <!-- Drawer content will be inserted here -->
        </slot>
      </v-card-text>
      
      <!-- Footer (optional) -->
      <div v-if="$slots.footer" class="drawer-footer">
        <slot name="footer"></slot>
      </div>
    </v-card>
  </v-navigation-drawer>
</template>

<script setup lang="ts">
import { computed, ref, watch, onMounted, onUnmounted } from 'vue';
import { useDisplay } from 'vuetify';

const props = defineProps({
  // v-model for drawer open/close state
  modelValue: {
    type: Boolean,
    required: true
  }
});

const emit = defineEmits(['update:modelValue']);

// Use Vuetify's display composable for responsive breakpoints
const { mdAndUp } = useDisplay();

// Computed property for large screen detection
const isLargeScreen = computed(() => mdAndUp.value);

// Two-way binding for drawer state
const drawerModel = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
});

// Helper function to close the drawer
function closeDrawer() {
  drawerModel.value = false;
}

// When switching from mobile to desktop, adjust content padding
watch(isLargeScreen, (newValue) => {
  // Let the layout update before notifying about the change
  setTimeout(() => {
    // Emit an event that can be used to adjust layout
    emit('screen-size-changed', newValue);
  }, 0);
});
</script>

<style scoped>
.responsive-drawer {
  z-index: 999; /* Ensure drawer is above other content */
}

/* On larger screens, adjust the drawer and content */
@media (min-width: 960px) {
  .responsive-drawer {
    position: fixed;
  }
  
  /* Hide scrim on desktop as we're not in overlay mode */
  .responsive-drawer :deep(.v-navigation-drawer__scrim) {
    display: none !important;
  }
}

.drawer-footer {
  border-top: 1px solid rgba(0, 0, 0, 0.12);
  padding: 12px 16px;
}
</style>
