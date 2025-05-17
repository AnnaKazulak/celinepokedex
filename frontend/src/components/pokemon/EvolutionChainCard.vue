<template>
  <div class="mt-8 mx-auto" style="max-width: 1500px;">
    <v-card>
      <v-card-title class="text-white" 
                   :style="{ background: dominantColor || 'rgba(0,0,0,0.5)' }">
        Evolution
      </v-card-title>
      <v-card-text>
        <!-- Vollständige Evolutionskette anzeigen -->
        <div v-if="evolutionChain && (evolutionChain.stages || hasBranches)" class="pa-4">
          <div v-if="evolutionChain.stages && evolutionChain.stages.length > 0" 
               class="d-flex flex-wrap justify-center align-center gap-2 pa-6 evolution-chain">
            <div v-for="(stage, index) in evolutionChain.stages" :key="index" 
                 class="d-flex align-center evolution-stage">
              <!-- Pokemon in this stage -->
              <div class="d-flex flex-column align-center text-center">
                <!-- Evolution stage label -->
                <div class="text-body-2 mb-2 py-1 px-2 rounded-pill bg-grey-lighten-4 d-flex align-center justify-center gap-1"
                     :class="{ 'evolution-custom-current': stage.pokemon.pokedexNumber === currentPokemonId }">
                  <template v-if="index === 0">
                    <v-icon size="small" color="primary" class="mr-1">mdi-egg-outline</v-icon>
                    <span>Grundform</span>
                  </template>
                  <template v-else-if="index === evolutionChain.stages.length - 1">
                    <v-icon size="small" color="success" class="mr-1">mdi-star-outline</v-icon>
                    <span>Finale Form</span>
                  </template>
                  <template v-else>
                    <v-icon size="small" color="info" class="mr-1">mdi-arrow-up-bold-circle-outline</v-icon>
                    <span>Entwicklung {{ index }}</span>
                  </template>
                </div>

                <router-link 
                  v-if="stage.pokemon.inUserCollection !== false"
                  :to="{ name: 'pokemonDetail', params: { id: stage.pokemon.pokedexNumber } }"
                  class="text-decoration-none text-inherit d-flex flex-column align-center pa-4 rounded-lg transition-transform evolution-card"
                  :class="{ 'evolution-custom-card': stage.pokemon.pokedexNumber === currentPokemonId }"
                >
                  <v-img
                    :src="stage.pokemon.imageUrl"
                    height="150"
                    width="150"
                    contain
                    class="evolution-custom-image"
                  ></v-img>
                  <div class="font-weight-bold mt-2">{{ stage.pokemon.name }}</div>
                  <div class="text-grey text-caption">#{{ stage.pokemon.pokedexNumber }}</div>
                  
                  <!-- Level Info (für Mobilgeräte) -->
                  <div class="d-none d-flex-xs mt-2 text-caption text-grey-darken-1 align-center justify-center">
                    <v-chip size="x-small" color="grey-lighten-3" class="text-caption">
                      <span>Level {{ stage.pokemon.level || '?' }}</span>
                    </v-chip>
                  </div>
                </router-link>

                <!-- Ausgegraut wenn nicht in der Sammlung -->
                <div v-else 
                     class="text-decoration-none text-inherit d-flex flex-column align-center pa-4 rounded-lg bg-grey-lighten-5 opacity-medium evolution-card" 
                     @click="handleMissingPokemonClick(stage.pokemon)">
                  <v-img
                    :src="stage.pokemon.imageUrl"
                    height="150"
                    width="150"
                    contain
                    class="evolution-custom-greyed"
                  ></v-img>
                  <div class="font-weight-bold mt-2 text-grey-darken-1">{{ stage.pokemon.name }}</div>
                  <div class="text-grey text-caption">#{{ stage.pokemon.pokedexNumber }}</div>
                  
                  <!-- Level Info (für Mobilgeräte) -->
                  <div class="d-none d-flex-xs mt-2 text-caption text-grey-darken-1 align-center justify-center">
                    <v-chip size="x-small" color="grey-lighten-3" class="text-caption">
                      <span>Level {{ stage.pokemon.level || '?' }}</span>
                    </v-chip>
                  </div>
                  <v-chip
                    size="small"
                    color="grey-lighten-2" 
                    class="mt-2 text-caption"
                  >
                    <v-icon start size="x-small">mdi-plus</v-icon>
                    Nicht in Sammlung
                  </v-chip>
                </div>
                
                <!-- Show from which Pokémon it evolves for stages > 0 -->
                <div v-if="index > 0" class="d-flex align-center mt-2 text-caption text-grey-darken-1">
                  <v-icon size="x-small" color="grey">mdi-arrow-up-thin</v-icon>
                  <span>Entwickelt aus {{ getPreviousStageName(stage.pokemon) }}</span>
                </div>
              </div>
              
              <!-- Evolution Arrow with condition -->
              <div v-if="index < evolutionChain.stages.length - 1" class="d-flex flex-column align-center px-4 evolution-arrow">
                <!-- Desktop Version (horizontaler Pfeil) -->
                <div class="d-none d-sm-flex flex-column align-center">
                  <div class="text-caption text-grey-darken-1 text-center mt-2 evolution-condition" style="max-width: 120px;">
                    {{ getEvolutionCondition(evolutionChain.stages[index+1]) }}
                  </div>
                  <v-icon 
                    :color="dominantColor || 'rgba(0,0,0,0.5)'"
                    size="large"
                    class="mt-2"
                  >
                    mdi-arrow-right-bold
                  </v-icon>
                </div>
                
                <!-- Mobile Version (vertikaler Pfeil nach unten) -->
                <div class="d-flex d-sm-none flex-column align-center my-2">
                  <div class="text-caption text-grey-darken-1 text-center mb-2" style="max-width: 150px;">
                    {{ getEvolutionCondition(evolutionChain.stages[index+1]) }}
                  </div>
                  <v-icon 
                    :color="dominantColor || 'rgba(0,0,0,0.5)'"
                    size="large"
                  >
                    mdi-arrow-down-bold
                  </v-icon>
                </div>
              </div>

              <!-- Verzweigungsentwicklungen anzeigen -->
              <div v-if="hasBranchesForStage(stage.pokemon.pokedexNumber)" 
                   class="mt-8 pt-4 w-100 position-relative border-t"
                   style="border-top: 1px dashed rgba(0, 0, 0, 0.2);">
                <div class="d-flex align-center mb-3 text-grey-darken-1 text-body-2">
                  <v-icon 
                    :color="dominantColor || 'rgba(0,0,0,0.5)'"
                    size="small"
                    class="mr-1"
                  >
                    mdi-call-split
                  </v-icon>
                  <span class="font-weight-medium">Weitere Entwicklungen</span>
                </div>
                
                <div class="d-flex flex-wrap gap-4 justify-center">
                  <div v-for="(branchStage, branchIdx) in getBranchesForStage(stage.pokemon.pokedexNumber)" 
                      :key="`branch-${branchIdx}`"
                      class="d-flex flex-column align-center">
                    
                    <!-- Evolution condition for branch -->
                    <div class="text-center mb-2">
                      <v-chip size="small" color="grey-lighten-3" class="text-caption">
                        <v-icon size="x-small" class="mr-1" color="grey-darken-1">mdi-arrow-decision-outline</v-icon>
                        {{ getEvolutionCondition(branchStage) }}
                      </v-chip>
                    </div>
                    
                    <!-- Branch Pokemon Link -->
                    <router-link 
                      v-if="branchStage.pokemon.inUserCollection !== false"
                      :to="{ name: 'pokemonDetail', params: { id: branchStage.pokemon.pokedexNumber } }"
                      class="text-decoration-none text-inherit d-flex flex-column align-center pa-3 rounded-lg transition-transform evolution-branch-card"
                      :class="{ 'evolution-custom-card': branchStage.pokemon.pokedexNumber === currentPokemonId }"
                    >
                      <v-img
                        :src="branchStage.pokemon.imageUrl"
                        height="130"
                        width="130"
                        contain
                        class="evolution-custom-image"
                      ></v-img>
                      <div class="font-weight-bold mt-2">{{ branchStage.pokemon.name }}</div>
                      <div class="text-grey text-caption">#{{ branchStage.pokemon.pokedexNumber }}</div>
                    </router-link>

                    <!-- Ausgegraut wenn nicht in der Sammlung -->
                    <div v-else 
                         class="text-decoration-none text-inherit d-flex flex-column align-center pa-3 rounded-lg bg-grey-lighten-5 opacity-medium evolution-branch-card" 
                         @click="handleMissingPokemonClick(branchStage.pokemon)">
                      <v-img
                        :src="branchStage.pokemon.imageUrl"
                        height="130"
                        width="130"
                        contain
                        class="evolution-custom-greyed"
                      ></v-img>
                      <div class="font-weight-bold mt-2 text-grey-darken-1">{{ branchStage.pokemon.name }}</div>
                      <div class="text-grey text-caption">#{{ branchStage.pokemon.pokedexNumber }}</div>
                      <v-chip
                        size="small"
                        color="grey-lighten-2" 
                        class="mt-2 text-caption"
                      >
                        <v-icon start size="x-small">mdi-plus</v-icon>
                        Nicht in Sammlung
                      </v-chip>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Evolutionsketteinfo wenn unvollständig -->
        <div v-else-if="pokemon && pokemon.evolutionChainId" class="py-4">
          <v-alert
            type="info"
            icon="mdi-information-outline"
            color="info"
            class="my-2"
            variant="tonal"
            border="start"
          >
            <div class="text-h6 mb-2">Evolutionskette unvollständig</div>
            <p>Dieses Pokémon gehört zur Evolutionskette #{{ pokemon.evolutionChainId }} aber die anderen Entwicklungsstufen sind noch nicht in deiner Pokédex erfasst.</p>
            <p class="mt-2">Um die vollständige Evolutionskette zu sehen, füge bitte die fehlenden Evolutionen hinzu.</p>
            <p v-if="knownEvolutionChains[pokemon.evolutionChainId] && knownEvolutionChains[pokemon.evolutionChainId].length > 0" class="mt-2 text-subtitle-2">
              <strong>Bekannte Pokémon in dieser Kette:</strong> 
              <span v-for="(poke, index) in knownEvolutionChains[pokemon.evolutionChainId]" :key="poke.pokedexNumber">
                <router-link :to="{ name: 'pokemonDetail', params: { id: poke.pokedexNumber } }" class="evolution-known-link">
                  {{ poke.name }} (#{{ poke.pokedexNumber }})
                </router-link>
                <span v-if="index < knownEvolutionChains[pokemon.evolutionChainId].length - 1">, </span>
              </span>
            </p>
          </v-alert>
        </div>

        <!-- Hinweis für Pokémon ohne evolutionChainId -->
        <div v-else-if="pokemon" class="py-4">
          <v-alert
            type="info"
            variant="outlined"
            border
            icon="mdi-information-outline"
            class="my-2"
          >
            <div class="text-h6 mb-2">Evolution unbekannt</div>
            <p>Für dieses Pokémon ist keine Evolutionskette hinterlegt. Falls es zu einer Evolutionskette gehört, müssen die entsprechenden Informationen ergänzt werden.</p>
            <div class="mt-3">
              <v-chip color="primary" class="mb-1">Tipp</v-chip>
              <p class="mt-1">Um eine Evolutionskette zu erstellen:
                <ul>
                  <li>Beim Erstellen eines Pokémon die evolutionChainId angeben</li>
                  <li>Bei Basis-Pokémon keine evolvesFromId setzen</li>
                  <li>Bei Evolutions-Pokémon die pokedexNumber des Vorgängers als evolvesFromId setzen</li>
                </ul>
              </p>
            </div>
          </v-alert>
        </div>
      </v-card-text>
    </v-card>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { type Pokemon, type EvolutionChain, type EvolutionStage } from '../../types/pokemon';

// Props
const props = defineProps<{
  pokemon: Pokemon;
  evolutionChain: EvolutionChain | null;
  dominantColor: string;
  currentPokemonId: string;
  knownEvolutionChains: Record<number, {name: string, pokedexNumber: string}[]>;
}>();

// Emit events
const emit = defineEmits<{
  (event: 'missingPokemonClick', pokemon: { name: string, pokedexNumber: string, imageUrl?: string }): void;
}>();

// Computed properties
const hasBranches = computed(() => {
  if (!props.evolutionChain || !props.evolutionChain.branches) return false;
  return Object.keys(props.evolutionChain.branches).length > 0;
});

// Prüfen, ob eine bestimmte Stage Verzweigungsentwicklungen hat
function hasBranchesForStage(pokedexNumber: string): boolean {
  if (!props.evolutionChain || !props.evolutionChain.branches) return false;
  return !!props.evolutionChain.branches[pokedexNumber]?.length;
}

// Verzweigungsentwicklungen für eine bestimmte Stage abrufen
function getBranchesForStage(pokedexNumber: string): EvolutionStage[] {
  if (!props.evolutionChain || !props.evolutionChain.branches) return [];
  return props.evolutionChain.branches[pokedexNumber] || [];
}

// Formatiert die Evolutionsbedingung für die Anzeige
function getEvolutionCondition(stage: EvolutionStage): string {
  if (!stage.condition) {
    return stage.trigger || 'Unbekannt';
  }
  return stage.condition;
}

// Funktion zum Abrufen des Namens des direkten Vorgängers eines Pokémons
function getPreviousStageName(currentPokemon: Pokemon): string {
  if (!currentPokemon.evolvesFromId) {
    return 'Keine Entwicklung';
  }

  // Suche den Vorgänger in der Evolutionskette
  if (props.evolutionChain && props.evolutionChain.stages) {
    const previousPokemon = props.evolutionChain.stages.find(
      stage => stage.pokemon.pokedexNumber === currentPokemon.evolvesFromId
    );
    
    if (previousPokemon) {
      return previousPokemon.pokemon.name;
    }
  }
  
  // Fallback: Nur die ID anzeigen, wenn der Name nicht gefunden wird
  return `#${currentPokemon.evolvesFromId}`;
}

// Handle click on greyed-out Pokémon
function handleMissingPokemonClick(pokemon: { name: string, pokedexNumber: string, imageUrl?: string }) {
  emit('missingPokemonClick', pokemon);
}
</script>

<style scoped>
.evolution-custom-current {
  background-color: v-bind('dominantColor ? `rgba(${parseInt(dominantColor.slice(1,3), 16)}, ${parseInt(dominantColor.slice(3,5), 16)}, ${parseInt(dominantColor.slice(5,7), 16)}, 0.15)` : "rgba(0,0,0,0.05)"');
  border: 1px solid v-bind('dominantColor || "rgba(0,0,0,0.2)"');
  color: v-bind('dominantColor || "rgba(0,0,0,0.7)"');
  font-weight: 600;
}

.evolution-custom-card {
  border: 2px solid;
  border-color: v-bind('dominantColor || "rgba(0,0,0,0.7)"');
  box-shadow: 0 4px 8px rgba(0,0,0,0.15);
  transform: translateY(-5px);
  background-color: rgba(255,255,255,0.8);
}

.evolution-custom-image {
  transition: transform 0.3s ease;
}

.evolution-card {
  min-width: 180px;
  margin: 0.5rem;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}

.evolution-branch-card {
  min-width: 160px;
  margin: 0.5rem;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}

.evolution-custom-greyed {
  filter: grayscale(0.8);
}

.evolution-known-link {
  color: inherit;
  text-decoration: underline dotted;
  font-weight: 500;
}

.evolution-known-link:hover {
  color: rgba(0, 0, 0, 0.8);
}

/* Mobile Geräte */
@media (max-width: 600px) {
  .evolution-chain {
    flex-direction: column;
    padding: 1rem 0.5rem;
  }
  
  .evolution-stage {
    flex-direction: column;
    margin-bottom: 1.5rem;
  }
  
  .evolution-arrow {
    margin: 1rem 0;
    transform: none;
  }
  
  .evolution-arrow-icon {
    transform: rotate(90deg);
    margin: 0.5rem 0;
  }
  
  .evolution-condition {
    transform: none;
    max-width: 180px;
    margin-bottom: 0.5rem;
  }
  
  .evolution-branches {
    width: 100%;
    margin: 2rem 0;
  }

  .branch-pokemon-container {
    flex-direction: column;
  }

  .branch-pokemon {
    margin-bottom: 1.5rem;
  }

  .branch-condition {
    max-width: 200px;
  }
}
</style>