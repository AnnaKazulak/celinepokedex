<template>
  <div class="bg-primary d-flex justify-center py-4 px-6">
    <v-spacer></v-spacer>
    
    <v-btn 
      color="white" 
      variant="text" 
      @click="$emit('reset')"
      class="mr-2"
    >
      {{ resetLabel }}
    </v-btn>
    
    <v-btn 
      v-if="showSave"
      color="white" 
      variant="text" 
      @click="$emit('save')"
      :loading="isSaving"
      class="mr-2"
    >
      <v-icon start>mdi-content-save</v-icon>
      Speichern
    </v-btn>
    
    <v-btn 
      v-if="showDownload"
      color="white" 
      variant="elevated" 
      @click="$emit('download')"
    >
      <v-icon start>mdi-download</v-icon>
      Download
    </v-btn>
    
    <v-spacer></v-spacer>
  </div>
</template>

<script setup lang="ts">
defineProps({
  resetLabel: {
    type: String,
    default: 'Neuer Charakter'
  },
  showSave: {
    type: Boolean,
    default: true
  },
  showDownload: {
    type: Boolean,
    default: false
  },
  isSaving: {
    type: Boolean,
    default: false
  }
});

defineEmits(['reset', 'save', 'download']);
</script>