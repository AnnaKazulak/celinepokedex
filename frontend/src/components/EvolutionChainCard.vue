<template>
  <div class="evolution-section">
    <v-card>
      <v-card-title class="text-white" 
                   :style="{ background: dominantColor || 'rgba(0,0,0,0.5)' }">
        Evolution
      </v-card-title>
      <v-card-text>
        <!-- Vollständige Evolutionskette anzeigen -->
        <div v-if="evolutionChain && (evolutionChain.stages || hasBranches)" class="evolution-content">
          <div v-if="evolutionChain.stages && evolutionChain.stages.length > 0" class="evolution-chain">
            <div v-for="(stage, index) in evolutionChain.stages" :key="index" class="evolution-stage">
              <!-- Pokemon in this stage -->
              <div class="evolution-pokemon">
                <!-- Evolution stage label -->
                <div class="evolution-stage-label" :class="{ 'current-pokemon': stage.pokemon.pokedexNumber === currentPokemonId }">
                  <template v-if="index === 0">
                    <v-icon size="small" color="primary" class="stage-icon">mdi-egg-outline</v-icon>
                    <span>Grundform</span>
                  </template>
                  <template v-else-if="index === evolutionChain.stages.length - 1">
                    <v-icon size="small" color="success" class="stage-icon">mdi-star-outline</v-icon>
                    <span>Finale Form</span>
                  </template>
                  <template v-else>
                    <v-icon size="small" color="info" class="stage-icon">mdi-arrow-up-bold-circle-outline</v-icon>
                    <span>Entwicklung {{ index }}</span>
                  </template>
                </div>

                <router-link 
                  v-if="stage.pokemon.inUserCollection !== false"
                  :to="{ name: 'pokemonDetail', params: { id: stage.pokemon.pokedexNumber } }"
                  class="evolution-link"
                  :class="{ 'current-pokemon-card': stage.pokemon.pokedexNumber === currentPokemonId }"
                >
                  <v-img
                    :src="stage.pokemon.imageUrl"
                    height="120"
                    width="120"
                    contain
                    class="evolution-image"
                  ></v-img>
                  <div class="evolution-name">{{ stage.pokemon.name }}</div>
                  <div class="evolution-number">#{{ stage.pokemon.pokedexNumber }}</div>
                </router-link>

                <!-- Ausgegraut wenn nicht in der Sammlung -->
                <div v-else class="evolution-link greyed-out" @click="handleMissingPokemonClick(stage.pokemon)">
                  <v-img
                    :src="stage.pokemon.imageUrl"
                    height="120"
                    width="120"
                    contain
                    class="evolution-image greyed-out-image"
                  ></v-img>
                  <div class="evolution-name">{{ stage.pokemon.name }}</div>
                  <div class="evolution-number">#{{ stage.pokemon.pokedexNumber }}</div>
                  <v-chip
                    size="small"
                    color="grey-lighten-2" 
                    class="missing-chip"
                  >
                    <v-icon start size="x-small">mdi-plus</v-icon>
                    Nicht in Sammlung
                  </v-chip>
                </div>
                
                <!-- Show from which Pokémon it evolves for stages > 0 -->
                <div v-if="index > 0" class="evolution-relation">
                  <v-icon size="x-small" color="grey">mdi-arrow-up-thin</v-icon>
                  <span>Entwickelt aus {{ getPreviousStageName(stage.pokemon) }}</span>
                </div>
              </div>
              
              <!-- Evolution Arrow with condition -->
              <div v-if="index < evolutionChain.stages.length - 1" class="evolution-arrow">
                <div class="evolution-condition">
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

              <!-- Verzweigungsentwicklungen anzeigen -->
              <div v-if="hasBranchesForStage(stage.pokemon.pokedexNumber)" class="evolution-branches">
                <div class="branch-header">
                  <v-icon 
                    :color="dominantColor || 'rgba(0,0,0,0.5)'"
                    size="small"
                    class="mr-1"
                  >
                    mdi-call-split
                  </v-icon>
                  <span class="branch-title">Weitere Entwicklungen</span>
                </div>
                
                <div class="branch-pokemon-container">
                  <div v-for="(branchStage, branchIdx) in getBranchesForStage(stage.pokemon.pokedexNumber)" 
                      :key="`branch-${branchIdx}`"
                      class="branch-pokemon">
                    
                    <!-- Evolution condition for branch -->
                    <div class="branch-condition">
                      <v-chip size="small" color="grey-lighten-3" class="branch-condition-chip">
                        <v-icon size="x-small" class="mr-1" color="grey-darken-1">mdi-arrow-decision-outline</v-icon>
                        {{ getEvolutionCondition(branchStage) }}
                      </v-chip>
                    </div>
                    
                    <!-- Branch Pokemon Link -->
                    <router-link 
                      v-if="branchStage.pokemon.inUserCollection !== false"
                      :to="{ name: 'pokemonDetail', params: { id: branchStage.pokemon.pokedexNumber } }"
                      class="evolution-link branch-link"
                      :class="{ 'current-pokemon-card': branchStage.pokemon.pokedexNumber === currentPokemonId }"
                    >
                      <v-img
                        :src="branchStage.pokemon.imageUrl"
                        height="100"
                        width="100"
                        contain
                        class="branch-image"
                      ></v-img>
                      <div class="evolution-name">{{ branchStage.pokemon.name }}</div>
                      <div class="evolution-number">#{{ branchStage.pokemon.pokedexNumber }}</div>
                    </router-link>

                    <!-- Ausgegraut wenn nicht in der Sammlung -->
                    <div v-else class="evolution-link branch-link greyed-out" @click="handleMissingPokemonClick(branchStage.pokemon)">
                      <v-img
                        :src="branchStage.pokemon.imageUrl"
                        height="100"
                        width="100"
                        contain
                        class="branch-image greyed-out-image"
                      ></v-img>
                      <div class="evolution-name">{{ branchStage.pokemon.name }}</div>
                      <div class="evolution-number">#{{ branchStage.pokemon.pokedexNumber }}</div>
                      <v-chip
                        size="small"
                        color="grey-lighten-2" 
                        class="missing-chip"
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
        <div v-else-if="pokemon && pokemon.evolutionChainId" class="evolution-incomplete">
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
                <router-link :to="{ name: 'pokemonDetail', params: { id: poke.pokedexNumber } }" class="known-pokemon-link">
                  {{ poke.name }} (#{{ poke.pokedexNumber }})
                </router-link>
                <span v-if="index < knownEvolutionChains[pokemon.evolutionChainId].length - 1">, </span>
              </span>
            </p>
          </v-alert>
        </div>

        <!-- Hinweis für Pokémon ohne evolutionChainId -->
        <div v-else-if="pokemon" class="evolution-unknown">
          <v-alert
            type="info"
            variant="outlined"
            border
            icon="mdi-information-outline"
            class="my-2"
          >
            <div class="text-h6 mb-2">Evolution unbekannt</div>
            <p>Für dieses Pokémon ist keine Evolutionskette hinterlegt. Falls es zu einer Evolutionskette gehört, müssen die entsprechenden Informationen ergänzt werden.</p>
            <div class="mt-3 evolution-help">
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
import { type Pokemon, type EvolutionChain, type EvolutionStage } from '@/types/pokemon';

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
.evolution-section {
  max-width: 1400px;
  margin: 2rem auto;
}

.evolution-chain {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 1.5rem;
}

.evolution-stage {
  display: flex;
  align-items: center;
  position: relative;
}

.evolution-pokemon {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  transition: all 0.3s ease;
}

.evolution-link {
  text-decoration: none;
  color: inherit;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0.75rem;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.evolution-link:hover {
  background: rgba(0, 0, 0, 0.05);
  transform: translateY(-5px);
}

.evolution-image {
  transition: transform 0.3s ease;
}

.evolution-link:hover .evolution-image {
  transform: scale(1.1);
}

.evolution-name {
  font-weight: bold;
  margin-top: 0.5rem;
}

.evolution-number {
  color: grey;
  font-size: 0.9rem;
}

/* Styling für ausgegraute Pokémon in der Evolution */
.greyed-out {
  position: relative;
  opacity: 0.7;
  cursor: default;
  background-color: rgba(0, 0, 0, 0.03);
  border-radius: 12px;
  padding: 0.75rem;
}

.greyed-out .evolution-name,
.greyed-out .evolution-number {
  color: #777;
}

.greyed-out-image {
  filter: grayscale(0.8);
}

.missing-chip {
  margin-top: 8px;
  font-size: 0.7rem;
}

.evolution-arrow {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 1rem;
  margin: 0 0.5rem;
}

.evolution-condition {
  margin-top: 0.5rem;
  font-size: 0.85rem;
  color: rgba(0, 0, 0, 0.6);
  text-align: center;
  max-width: 120px;
}

/* Verzweigungsentwicklungen Styling */
.evolution-branches {
  position: relative;
  margin-top: 2rem;
  border-top: 1px dashed rgba(0, 0, 0, 0.2);
  padding-top: 1rem;
  width: 100%;
}

.branch-header {
  display: flex;
  align-items: center;
  margin-bottom: 0.75rem;
  color: rgba(0, 0, 0, 0.6);
  font-size: 0.9rem;
}

.branch-title {
  font-weight: 500;
}

.branch-pokemon-container {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  justify-content: center;
}

.branch-pokemon {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.branch-condition {
  font-size: 0.8rem;
  color: rgba(0, 0, 0, 0.6);
  margin-bottom: 0.5rem;
  text-align: center;
  max-width: 120px;
  background-color: rgba(0, 0, 0, 0.05);
  padding: 4px 8px;
  border-radius: 4px;
}

.branch-link {
  padding: 0.5rem;
}

.branch-image {
  transition: transform 0.3s ease;
}

.branch-link:hover .branch-image {
  transform: scale(1.1);
}

.known-pokemon-link {
  color: inherit;
  text-decoration: underline dotted;
  font-weight: 500;
}

.known-pokemon-link:hover {
  color: rgba(0, 0, 0, 0.8);
}

.evolution-stage-label {
  font-size: 0.85rem;
  margin-bottom: 0.5rem;
  padding: 4px 8px;
  border-radius: 12px;
  background-color: rgba(0,0,0,0.05);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.stage-icon {
  margin-right: 2px;
}

.evolution-relation {
  font-size: 0.75rem;
  color: rgba(0,0,0,0.6);
  margin-top: 0.5rem;
  display: flex;
  align-items: center;
  gap: 4px;
}

.current-pokemon {
  background-color: v-bind('dominantColor ? `rgba(${parseInt(dominantColor.slice(1,3), 16)}, ${parseInt(dominantColor.slice(3,5), 16)}, ${parseInt(dominantColor.slice(5,7), 16)}, 0.15)` : "rgba(0,0,0,0.05)"');
  border: 1px solid v-bind('dominantColor || "rgba(0,0,0,0.2)"');
  color: v-bind('dominantColor || "rgba(0,0,0,0.7)"');
  font-weight: 600;
}

.current-pokemon-card {
  border: 2px solid;
  border-color: v-bind('dominantColor || "rgba(0,0,0,0.7)"');
  box-shadow: 0 4px 8px rgba(0,0,0,0.15);
  transform: translateY(-5px);
  background-color: rgba(255,255,255,0.8);
}

.branch-condition-chip {
  font-size: 0.75rem;
  margin-bottom: 0.5rem;
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
  
  .evolution-arrow .v-icon {
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