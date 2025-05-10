<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
import Navbar from './components/Navbar.vue'
</script>

<template>
  <v-app>
    <Navbar />
    <v-main>
      <!-- Wrapper div to prevent attribute inheritance issues -->
      <div class="router-view-wrapper">
        <RouterView />
      </div>
    </v-main>
  </v-app>
</template>

<style>
.router-view-wrapper {
  height: 100%;
  width: 100%;
}
</style>

