<script setup lang="ts">
import { RouterLink, RouterView, useRoute } from 'vue-router'
import { provide, ref, watch } from 'vue'
import Navbar from './components/Navbar.vue'
import BackToTop from './components/BackToTop.vue'

// Global state for drawer
const isDrawerOpen = ref(false);

// Provide drawer state to components
provide('isDrawerOpen', isDrawerOpen);
</script>

<template>
  <v-app>
    <Navbar />
    <v-main>
      <!-- Wrapper div to prevent attribute inheritance issues -->
      <div class="router-view-wrapper">
        <RouterView @drawer-state-changed="isDrawerOpen = $event" />
      </div>
    </v-main>
    <BackToTop :is-drawer-open="isDrawerOpen" />
  </v-app>
</template>

<style>
.router-view-wrapper {
  height: 100%;
  width: 100%;
}
</style>

