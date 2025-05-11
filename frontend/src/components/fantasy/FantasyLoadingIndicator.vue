<template>
  <v-card class="pa-4" variant="outlined" rounded="lg">
    <div class="text-center">
      <v-icon size="x-large" color="primary" class="mb-3">mdi-creation</v-icon>
      <h3 class="text-h6 mb-2">Dein Fantasy-Charakter wird erstellt</h3>
      <p class="text-body-2 mb-4">Die KI arbeitet an deinem einzigartigen Charakter...</p>
      
      <v-progress-linear
        indeterminate
        color="primary"
        class="mb-3"
        height="6"
        rounded
      ></v-progress-linear>
      
      <!-- Step indicator -->
      <div class="d-flex justify-space-between mb-2">
        <v-chip
          size="small"
          :color="loadingStep >= 1 ? 'primary' : 'grey-lighten-1'" 
          :variant="loadingStep >= 1 ? 'elevated' : 'flat'"
        >
          <v-icon size="x-small" start>mdi-pencil</v-icon>Analysieren
        </v-chip>
        <v-chip
          size="small"
          :color="loadingStep >= 2 ? 'primary' : 'grey-lighten-1'"
          :variant="loadingStep >= 2 ? 'elevated' : 'flat'"
        >
          <v-icon size="x-small" start>mdi-brush</v-icon>Zeichnen
        </v-chip>
        <v-chip
          size="small"
          :color="loadingStep >= 3 ? 'primary' : 'grey-lighten-1'"
          :variant="loadingStep >= 3 ? 'elevated' : 'flat'"
        >
          <v-icon size="x-small" start>mdi-palette</v-icon>Details
        </v-chip>
        <v-chip
          size="small"
          :color="loadingStep >= 4 ? 'primary' : 'grey-lighten-1'"
          :variant="loadingStep >= 4 ? 'elevated' : 'flat'"
        >
          <v-icon size="x-small" start>mdi-image</v-icon>Fertigstellen
        </v-chip>
      </div>
      <p class="text-caption mt-3">Dieser Vorgang kann 15-30 Sekunden dauern</p>
    </div>
  </v-card>
</template>

<script setup lang="ts">
defineProps<{
  loadingStep: number;
}>();
</script>