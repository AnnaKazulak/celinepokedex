<template>
  <div class="pa-4 fantasy-tab-container">
    <v-row>
      <v-col cols="12" md="6">
        <v-select
          v-model="baseAnimal"
          :items="baseAnimalOptions"
          label="Base Animal"
          required
        ></v-select>
        
        <v-select
          v-model="elementType"
          :items="elementTypeOptions"
          label="Element Type"
          required
        ></v-select>
        
        <v-select
          v-model="dominantColor"
          :items="colorOptions"
          label="Dominant Color"
          required
        ></v-select>
        
        <v-select
          v-model="styleType"
          :items="styleTypeOptions"
          label="Art Style"
          required
        ></v-select>
        
        <v-select
          v-model="traits"
          :items="characterTraitOptions"
          label="Character Traits"
          multiple
          chips
        ></v-select>
        
        <div class="d-flex justify-center mt-4 mb-4">
          <v-btn 
            color="primary" 
            :loading="isGenerating"
            :disabled="!isFormValid || isGenerating"
            @click="onGenerateCharacter"
            min-width="200"
            height="44"
            class="text-none font-weight-medium elevation-2"
          >
            {{ isGenerating ? 'Generiere...' : 'Charakter erstellen' }}
          </v-btn>
        </div>
      </v-col>
      
      <v-col cols="12" md="6" class="d-flex align-center">
        <div v-if="isGenerating" class="text-center w-100">
          <v-progress-circular 
            indeterminate 
            color="primary"
            size="64"
          ></v-progress-circular>
          <div class="mt-3">Generating character...</div>
        </div>
        
        <div v-else-if="imageData" class="text-center w-100">
          <v-img 
            :src="imageData" 
            alt="Generated character" 
            max-width="240"
            max-height="240"
            class="rounded-lg elevation-8 mx-auto mb-3"
            contain
          ></v-img>
          <div class="mt-2 text-caption">{{ generatedPrompt }}</div>
        </div>
        
        <div v-else class="text-center w-100">
          <v-icon size="64" color="grey-lighten-1">mdi-image-outline</v-icon>
          <div class="mt-2 text-medium-emphasis">Select options and generate a character</div>
        </div>
      </v-col>
    </v-row>

    <!-- Spacer to maintain minimum height -->
    <div class="flex-grow-1 min-height-spacer"></div>
  </div>
</template>

<script setup lang="ts">
import { ref, defineProps, defineEmits, computed, watch } from 'vue';

// Props
const props = defineProps<{
  baseAnimal: string;
  elementType: string;
  dominantColor: string;
  styleType: string;
  traits: string[];
  imageData: string;
  generatedPrompt: string;
  isGenerating: boolean;
  baseAnimalOptions: string[];
  elementTypeOptions: string[];
  colorOptions: string[];
  styleTypeOptions: string[];
  characterTraitOptions: string[];
}>();

// Emits
const emit = defineEmits<{
  'generate-character': [];
  'update:baseAnimal': [value: string];
  'update:elementType': [value: string];
  'update:dominantColor': [value: string];
  'update:styleType': [value: string];
  'update:traits': [value: string[]];
}>();

// Local state with two-way binding
const baseAnimal = ref(props.baseAnimal);
const elementType = ref(props.elementType);
const dominantColor = ref(props.dominantColor);
const styleType = ref(props.styleType);
const traits = ref<string[]>(props.traits || []);

// Watch for prop changes
watch(() => props.baseAnimal, (newVal) => { baseAnimal.value = newVal; });
watch(() => props.elementType, (newVal) => { elementType.value = newVal; });
watch(() => props.dominantColor, (newVal) => { dominantColor.value = newVal; });
watch(() => props.styleType, (newVal) => { styleType.value = newVal; });
watch(() => props.traits, (newVal) => { traits.value = newVal; });

// Watch for local changes and update parent
watch(baseAnimal, (newVal) => { emit('update:baseAnimal', newVal); });
watch(elementType, (newVal) => { emit('update:elementType', newVal); });
watch(dominantColor, (newVal) => { emit('update:dominantColor', newVal); });
watch(styleType, (newVal) => { emit('update:styleType', newVal); });
watch(traits, (newVal) => { emit('update:traits', newVal); });

// Computed properties
const isFormValid = computed(() => {
  return !!baseAnimal.value && 
         !!elementType.value && 
         !!dominantColor.value && 
         !!styleType.value;
});

// Methods
const onGenerateCharacter = () => {
  if (!isFormValid.value || props.isGenerating) return;
  emit('generate-character');
};
</script>

<style scoped>
.fantasy-tab-container {
  display: flex;
  flex-direction: column;
  min-height: 500px;
}

.min-height-spacer {
  min-height: 20px;
}
</style>