<template>
  <v-row>
    <v-col cols="12" class="text-center">
      <v-alert
        type="info"
        :text="getMessage()"
        variant="tonal"
        class="mx-2"
      ></v-alert>
    </v-col>
  </v-row>
</template>

<script setup lang="ts">
const props = defineProps({
  contentType: {
    type: String,
    default: 'all'
  }
});

function getMessage(): string {
  switch (props.contentType) {
    case 'pokemon':
      return 'Es wurden keine Pokémon gefunden, die deiner Suche entsprechen.';
    case 'fantasy':
      return 'Es wurden keine Fantasy-Charaktere gefunden, die deiner Suche entsprechen.';
    default:
      return 'Es wurden keine Ergebnisse gefunden, die deiner Suche entsprechen.';
  }
}
</script>