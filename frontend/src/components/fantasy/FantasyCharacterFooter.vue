<template>
  <div class="footer-container d-flex justify-center py-3 px-4 py-md-4 px-md-6">
    <v-spacer></v-spacer>
    
    <v-btn 
      color="white" 
      variant="text" 
      @click="$emit('reset')"
      class="mr-2"
    >
      <v-icon>mdi-refresh</v-icon>
      <span class="d-none d-md-inline ml-1">{{ resetLabel }}</span>
    </v-btn>
    
    <v-btn 
      color="white" 
      variant="text" 
      @click="$emit('save')"
      :loading="isSaving"
      :disabled="saveDisabled"
      class="mr-2"
    >
      <v-icon>mdi-content-save</v-icon>
      <span class="d-none d-md-inline ml-1">Speichern</span>
    </v-btn>
    
    <v-btn 
      v-if="showDownload"
      color="white" 
      variant="elevated" 
      @click="$emit('download')"
    >
      <v-icon>mdi-download</v-icon>
      <span class="d-none d-md-inline ml-1">Download</span>
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
  saveDisabled: {
    type: Boolean,
    default: false
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

<style scoped>
.footer-container {
  background: linear-gradient(135deg, #6890F0 0%, #705898 100%);
}
</style>