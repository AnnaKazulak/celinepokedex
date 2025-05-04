<template>
  <v-dialog v-model="dialogModel" max-width="800px" content-class="pokemon-dialog-wrapper">
    <v-card class="pokemon-main-card">
      <!-- Schwebendes Bild -->
      <div class="floating-image-container">
        <v-img
          :src="pokemon.imageUrl || APP_CONFIG.DEFAULT_IMAGE_URL"
          class="pokemon-floating-image"
          contain
          @load="onImageLoad"
        >
          <template v-slot:placeholder>
            <div class="d-flex align-center justify-center fill-height">
              <v-progress-circular color="primary" indeterminate></v-progress-circular>
            </div>
          </template>
        </v-img>
      </div>
      
      <div class="card-header">
        <div class="level-text"></div>
        
        <!-- Fehleranzeige -->
        <v-alert
          v-if="errorMessage"
          type="error"
          density="compact"
          variant="tonal"
          class="mb-4"
          closable
        >
          {{ errorMessage }}
        </v-alert>
        
        <v-form ref="form" v-model="valid" @submit.prevent="savePokemon" class="pokemon-form">
          <!-- Grunddaten -->
          <div class="form-section">
            <div class="form-row">
              <v-text-field
                v-model="pokemon.name"
                label="Name *"
                density="compact"
                variant="underlined"
                required
                class="name-input"
                :rules="[v => !!v || 'Name ist erforderlich']"
                @update:model-value="watchNameChanges"
              ></v-text-field>
              
              <v-text-field
                v-model="pokemon.pokedexNumber"
                label="Pokédex-Nr. *"
                density="compact"
                variant="underlined"
                required
                class="number-input"
                :rules="[
                  v => !!v || 'Pokédex-Nummer ist erforderlich',
                  v => /^\d+$/.test(v) || 'Nur Zahlen erlaubt'
                ]"
                @update:model-value="watchPokedexNumberChanges"
                persistent-hint
                append-inner-icon="mdi-magnify"
              >
                <template v-slot:append>
                  <v-tooltip text="Zufälligen Pokémon generieren" location="bottom">
                    <template v-slot:activator="{ props }">
                      <v-btn
                        v-bind="props"
                        icon="mdi-dice-multiple"
                        variant="text"
                        density="comfortable"
                        color="primary"
                        @click="generateRandomPokedexNumber"
                        :loading="isLoading"
                      ></v-btn>
                    </template>
                  </v-tooltip>
                </template>
              </v-text-field>
            </div>
            
            <v-textarea
              v-model="pokemon.description"
              label="Beschreibung"
              rows="2"
              variant="underlined"
              density="compact"
              counter="150"
              class="description-input"
              :rules="[v => v.length <= 150 || 'Maximal 150 Zeichen erlaubt']"
              hint="Maximal 150 Zeichen"
              persistent-hint
            ></v-textarea>
          </div>
          
          <!-- Typen -->
          <div class="form-section">
            <h4 class="section-title">Typen</h4>
            <div class="form-row">
              <v-select
                v-model="pokemon.type1"
                :items="pokemonTypes"
                label="Typ 1 *"
                density="compact"
                variant="underlined"
                required
                class="type-input"
                :rules="[v => !!v || 'Typ 1 ist erforderlich']"
              ></v-select>
              <v-select
                v-model="pokemon.type2"
                :items="pokemonTypes"
                label="Typ 2"
                density="compact"
                variant="underlined"
                clearable
                class="type-input"
              ></v-select>
            </div>
          </div>
          
          <!-- Eigenschaften -->
          <div class="form-section">
            <h4 class="section-title">Eigenschaften</h4>
            <div class="form-row">
              <v-text-field
                v-model="pokemon.category"
                label="Kategorie"
                density="compact"
                variant="underlined"
                class="category-input"
              ></v-text-field>
              <v-text-field
                v-model="pokemon.ability"
                label="Fähigkeit"
                density="compact"
                variant="underlined"
                class="ability-input"
              ></v-text-field>
            </div>
          </div>
          
          <!-- Evolutionsketten-Informationen -->
          <div class="form-section">
            <h4 class="section-title">Evolution</h4>
            <div class="form-row">
              <v-text-field
                v-model="pokemon.evolutionChainId"
                label="Evolutionskette ID"
                density="compact"
                variant="underlined"
                type="number"
                hint="Fortlaufende Nummer der Evolutionskette"
                class="evolution-input"
              ></v-text-field>
              <v-text-field
                v-model="pokemon.evolvesFromId"
                label="Entwickelt aus"
                density="compact"
                variant="underlined"
                hint="Pokédex-Nr. der Vorentwicklung"
                class="evolution-input"
                persistent-hint
              ></v-text-field>
            </div>
          </div>
          
          <!-- Verstecktes Bild-URL-Feld -->
          <div class="image-url-container">
            <div class="form-row">
              <v-text-field
                v-model="pokemon.imageUrl"
                label="Bild URL"
                variant="outlined"
                density="compact"
                hide-details
                class="flex-grow-1"
              ></v-text-field>
            </div>
          </div>
        </v-form>
      </div>
      
      <!-- Footer mit Maßen und Aktionen -->
      <div class="pokemon-footer" :style="{ background: dominantColor }">
        <div class="footer-section measurements">
          <div class="footer-value">
            <v-text-field
              v-model="pokemon.height"
              type="number"
              placeholder="0.0"
              step="0.1"
              density="compact"
              hide-details
              variant="outlined"
              class="footer-input"
              bg-color="rgba(255,255,255,0.2)"
              color="white"
            ></v-text-field>
            <span>Größe (m)</span>
          </div>
          <div class="footer-value">
            <v-text-field
              v-model="pokemon.weight"
              type="number"
              placeholder="0.0"
              step="0.1"
              density="compact"
              hide-details
              variant="outlined"
              class="footer-input"
              bg-color="rgba(255,255,255,0.2)"
              color="white"
            ></v-text-field>
            <span>Gewicht (kg)</span>
          </div>
        </div>
        
        <div class="footer-section actions">
          <v-btn 
            color="white" 
            variant="text" 
            @click="dialogModel = false"
            class="cancel-btn"
          >
            Abbrechen
          </v-btn>
          <v-btn 
            color="white" 
            variant="elevated" 
            @click="savePokemon" 
            :loading="isLoading"
            :disabled="!valid"
            class="save-btn"
          >
            Speichern
          </v-btn>
        </div>
      </div>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router'; // Router importieren
import { eventBus } from '../utils/eventBus';
import type { Pokemon, PokemonFormData } from '../types/pokemon';
import { PokemonType } from '../types/pokemon';
import type { VForm } from 'vuetify';
import { extractDominantColor } from '../utils/colorUtils';
import { API_ENDPOINTS, EXTERNAL_API, APP_CONFIG, TIMEOUTS, VALIDATION_RULES } from '../utils/constants';
import { debounce, capitalizeFirstLetter, cleanText, translatePokemonType } from '../utils/helpers';

// Props und Emits
const props = defineProps({
  dialog: {
    type: Boolean,
    required: true
  },
  prefillData: {
    type: Object as () => PokemonFormData | null,
    default: null
  }
});

const emit = defineEmits(['update:dialog', 'pokemon-created']);

// Verfügbare Pokémon-Typen (entsprechend dem Type-Enum im Backend)
const pokemonTypes = Object.values(PokemonType);

// Reaktive Zustandsvariablen
const valid = ref(false);
const form = ref<VForm | null>(null);
const isLoading = ref(false);
const lastNameUsedForImageGeneration = ref('');
const lastPokedexNumberUsedForImageGeneration = ref('');
const dominantColor = ref(APP_CONFIG.DEFAULT_COLOR); 
const errorMessage = ref('');
const pokemonExistsInDb = ref(false); // Neue Variable für den Graustufen-Status

const pokemon = reactive<PokemonFormData>({
  name: '',
  pokedexNumber: '',
  description: '',
  type1: null,
  type2: null,
  height: null,
  weight: null,
  imageUrl: '',
  category: '',
  ability: '',
  evolutionChainId: null,
  evolvesFromId: null,
  inUserCollection: true // Diese Eigenschaft standardmäßig auf true setzen
});

// Computed property für v-model:dialog
const dialogModel = computed({
  get: () => props.dialog,
  set: (value) => emit('update:dialog', value)
});

// Computed property um zu prüfen, ob eine Bild-URL generiert werden kann
const canGenerateImageUrl = computed(() => {
  return (pokemon.name && pokemon.name.trim().length > 0) || 
         (pokemon.pokedexNumber && pokemon.pokedexNumber.trim().length > 0);
});

// Extrahiere die dominante Farbe aus dem Pokémon-Bild
async function onImageLoad() {
  try {
    if (pokemon.imageUrl) {
      dominantColor.value = await extractDominantColor(pokemon.imageUrl);
    }
  } catch (error) {
    console.error('Fehler bei der Farbextraktion:', error);
  }
}

// Funktion zur Abfrage der PokéAPI anhand der Pokédex-Nummer
const fetchPokemonInfoByNumber = async (pokedexNumber: string) => {
  try {
    errorMessage.value = '';
    isLoading.value = true;
    const number = parseInt(pokedexNumber.trim(), 10);
    if (isNaN(number)) return;
    
    // Prüfe, ob das Pokémon bereits in der DB existiert
    await checkIfPokemonExists(pokedexNumber);
    
    // Zuerst die Grunddaten des Pokémon abrufen
    const pokemonResponse = await axios.get(EXTERNAL_API.POKEMON(number));
    const pokeData = pokemonResponse.data;
    
    // Bild-URL setzen
    if (pokeData.sprites?.other?.['official-artwork']?.front_default) {
      pokemon.imageUrl = pokeData.sprites.other['official-artwork'].front_default;
    } else if (pokeData.sprites?.front_default) {
      pokemon.imageUrl = pokeData.sprites.front_default;
    }
    
    // Pokédex-Nummer setzen
    if (pokeData.id) {
      pokemon.pokedexNumber = String(pokeData.id).padStart(3, '0');
    }
    
    // Automatisch Größe und Gewicht befüllen
    if (pokeData.height) {
      pokemon.height = pokeData.height / 10; // PokeAPI gibt Höhe in Dezimetern an
    }
    
    if (pokeData.weight) {
      pokemon.weight = pokeData.weight / 10; // PokeAPI gibt Gewicht in Hektogramm an
    }
    
    // Fähigkeit befüllen (erste Fähigkeit aus der Liste)
    if (pokeData.abilities && pokeData.abilities.length > 0) {
      try {
        const abilityResponse = await axios.get(pokeData.abilities[0].ability.url);
        const abilityData = abilityResponse.data;
        
        // Deutsche Fähigkeitsbeschreibung suchen
        const germanName = abilityData.names.find(
          (nameObj: any) => nameObj.language && nameObj.language.name === 'de'
        );
        
        if (germanName && germanName.name) {
          pokemon.ability = germanName.name;
        } else {
          pokemon.ability = capitalizeFirstLetter(pokeData.abilities[0].ability.name);
        }
      } catch (error) {
        // Fallback: Englischen Namen verwenden
        pokemon.ability = capitalizeFirstLetter(pokeData.abilities[0].ability.name);
      }
    }
    
    // Jetzt zusätzlich den Pokémon-Species-Endpunkt abfragen, um weitere Details zu erhalten
    try {
      const speciesResponse = await axios.get(EXTERNAL_API.POKEMON_SPECIES(number));
      const speciesData = speciesResponse.data;
      
      // Evolution Chain Informationen abrufen
      if (speciesData.evolution_chain && speciesData.evolution_chain.url) {
        try {
          // Evolution Chain ID aus der URL extrahieren
          const evolutionChainUrl = speciesData.evolution_chain.url;
          const evolutionChainIdMatch = evolutionChainUrl.match(/\/evolution-chain\/(\d+)\//);
          if (evolutionChainIdMatch && evolutionChainIdMatch[1]) {
            pokemon.evolutionChainId = parseInt(evolutionChainIdMatch[1], 10);
            
            // Evolution Chain Daten abrufen
            const evolutionChainResponse = await axios.get(evolutionChainUrl);
            const evolutionChainData = evolutionChainResponse.data;
            
            // Überprüfen, ob das aktuelle Pokémon eine Entwicklung ist
            let evolvesFrom = null;
            
            // Die Funktion sucht rekursiv in der Evolutionskette nach Entwicklungen
            const findEvolution = (chain: any, targetId: number): string | null => {
              if (!chain) return null;
              
              // ID des aktuellen Pokémon in der Kette
              const speciesUrl = chain.species.url;
              const speciesIdMatch = speciesUrl.match(/\/pokemon-species\/(\d+)\//);
              const currentId = speciesIdMatch ? parseInt(speciesIdMatch[1], 10) : null;
              
              // Wenn das aktuelle Pokémon gefunden wurde
              if (currentId === targetId) {
                return null; // Dieses Pokémon evolviert nicht von einem anderen
              }
              
              // Für jede Entwicklung prüfen
              if (chain.evolves_to && chain.evolves_to.length > 0) {
                for (const evolution of chain.evolves_to) {
                  const evoSpeciesUrl = evolution.species.url;
                  const evoIdMatch = evoSpeciesUrl.match(/\/pokemon-species\/(\d+)\//);
                  const evoId = evoIdMatch ? parseInt(evoIdMatch[1], 10) : null;
                  
                  // Wenn dieses Pokémon eine direkte Entwicklung ist
                  if (evoId === targetId) {
                    return currentId ? String(currentId).padStart(3, '0') : null;
                  }
                  
                  // Rekursiv in weiteren Entwicklungen suchen
                  const nestedResult = findEvolution(evolution, targetId);
                  if (nestedResult) return nestedResult;
                }
              }
              
              return null;
            };
            
            // Evolutionskette nach dem Vorgänger des aktuellen Pokémon durchsuchen
            evolvesFrom = findEvolution(evolutionChainData.chain, number);
            if (evolvesFrom) {
              pokemon.evolvesFromId = evolvesFrom;
            }
          }
        } catch (evolutionError) {
          console.error("Fehler beim Abrufen der Evolutionskette:", evolutionError);
        }
      }
      
      // Deutschen Namen suchen in der Namen-Liste
      if (speciesData.names && Array.isArray(speciesData.names)) {
        const germanName = speciesData.names.find(
          (nameObj: any) => nameObj.language && nameObj.language.name === 'de'
        );
        
        if (germanName && germanName.name) {
          pokemon.name = germanName.name;
        } else {
          // Fallback: Englischen Namen verwenden und ersten Buchstaben groß schreiben
          if (pokeData.name) {
            pokemon.name = capitalizeFirstLetter(pokeData.name);
          }
        }
      } else {
        // Fallback: Englischen Namen verwenden
        if (pokeData.name) {
          pokemon.name = capitalizeFirstLetter(pokeData.name);
        }
      }
      
      // Kategorie (Genus) aus den species-Daten abrufen
      if (speciesData.genera && Array.isArray(speciesData.genera)) {
        const germanGenus = speciesData.genera.find(
          (genusObj: any) => genusObj.language && genusObj.language.name === 'de'
        );
        
        if (germanGenus && germanGenus.genus) {
          // "Pokémon" aus der Kategorie entfernen
          pokemon.category = germanGenus.genus.replace(' Pokémon', '').trim();
        }
      }
      
      // Beschreibung abrufen
      if (speciesData.flavor_text_entries && Array.isArray(speciesData.flavor_text_entries)) {
        const germanDescription = speciesData.flavor_text_entries.find(
          (entry: any) => entry.language && entry.language.name === 'de'
        );
        
        if (germanDescription && germanDescription.flavor_text) {
          // Beschreibung bereinigen
          pokemon.description = cleanText(germanDescription.flavor_text);
        }
      }
      
    } catch (speciesError) {
      console.error("Fehler beim Abrufen der Pokémon-Species-Daten:", speciesError);
      // Fallback: Englischen Namen verwenden
      if (pokeData.name) {
        pokemon.name = capitalizeFirstLetter(pokeData.name);
      }
    }
    
    // Typ 1 immer automatisch befüllen
    if (pokeData.types && pokeData.types.length > 0) {
      const type1 = translatePokemonType(pokeData.types[0].type.name);
      if (type1 && Object.values(PokemonType).includes(type1 as PokemonType)) {
        pokemon.type1 = type1 as PokemonType;
      }
    }
    
    // Bei Typ 2 auch immer befüllen, wenn vorhanden
    if (pokeData.types && pokeData.types.length > 1) {
      const type2 = translatePokemonType(pokeData.types[1].type.name);
      if (type2 && Object.values(PokemonType).includes(type2 as PokemonType)) {
        pokemon.type2 = type2 as PokemonType;
      } else {
        // Wenn es keinen zweiten Typ gibt, Typ 2 leeren
        pokemon.type2 = null;
      }
    } else {
      // Wenn es keinen zweiten Typ gibt, Typ 2 leeren
      pokemon.type2 = null;
    }
    
    // Farbe aus dem Bild extrahieren
    if (pokemon.imageUrl) {
      onImageLoad();
    }
    
  } catch (error) {
    console.error('Fehler beim Abrufen der Pokémon-Informationen mit Nummer:', error);
    // Fallback: Direkte Bildpfad-Generierung
    try {
      pokemon.imageUrl = EXTERNAL_API.SPRITE_URL(pokedexNumber);
      onImageLoad();
    } catch (imgError) {
      console.error('Fehler beim Erstellen des Fallback-Bilds:', imgError);
    }
  } finally {
    isLoading.value = false;
  }
};

// Funktion zur automatischen Generierung der Bild-URL
const generateImageUrl = () => {
  if (pokemon.pokedexNumber && pokemon.pokedexNumber.trim() !== '') {
    // Wenn eine Pokédex-Nummer vorhanden ist, verwende diese
    const pokedexNumber = parseInt(pokemon.pokedexNumber.trim(), 10);
    if (!isNaN(pokedexNumber)) {
      pokemon.imageUrl = EXTERNAL_API.SPRITE_URL(pokedexNumber);
      lastPokedexNumberUsedForImageGeneration.value = pokemon.pokedexNumber;
      onImageLoad();
      return;
    }
  }
  
  if (pokemon.name && pokemon.name.trim() !== '') {
    // Wenn ein Name vorhanden ist, versuche eine Abfrage an die PokeAPI
    fetchPokemonInfoByName(pokemon.name.trim().toLowerCase());
    lastNameUsedForImageGeneration.value = pokemon.name;
  }
};

// Funktion zur Abfrage der PokéAPI, um Informationen über ein Pokémon anhand des Namens zu erhalten
const fetchPokemonInfoByName = async (name: string) => {
  try {
    errorMessage.value = '';
    isLoading.value = true;
    const formattedName = name.trim().toLowerCase();
    
    const response = await axios.get(`${EXTERNAL_API.BASE_URL}/pokemon/${formattedName}`);
    const pokeData = response.data;
    
    // Bild-URL setzen
    if (pokeData.sprites?.other?.['official-artwork']?.front_default) {
      pokemon.imageUrl = pokeData.sprites.other['official-artwork'].front_default;
    } else if (pokeData.sprites?.front_default) {
      pokemon.imageUrl = pokeData.sprites.front_default;
    }
    
    // Pokemon-Name und Pokédex-Nummer aktualisieren (immer aktualisieren)
    if (pokeData.name) {
      pokemon.name = capitalizeFirstLetter(pokeData.name);
    }
    
    if (pokeData.id) {
      pokemon.pokedexNumber = String(pokeData.id).padStart(3, '0');
    }
    
    // Automatische Befüllung der Typen beibehalten, wenn diese leer sind
    if (!pokemon.type1 && pokeData.types && pokeData.types.length > 0) {
      const type1 = pokeData.types[0].type.name.toUpperCase();
      if (Object.values(PokemonType).includes(type1 as PokemonType)) {
        pokemon.type1 = type1 as PokemonType;
      }
    }
    
    if (!pokemon.type2 && pokeData.types && pokeData.types.length > 1) {
      const type2 = pokeData.types[1].type.name.toUpperCase();
      if (Object.values(PokemonType).includes(type2 as PokemonType)) {
        pokemon.type2 = type2 as PokemonType;
      }
    }
    
    // Farbe aus dem Bild extrahieren
    if (pokemon.imageUrl) {
      onImageLoad();
    }
    
  } catch (error) {
    console.error('Fehler beim Abrufen der Pokémon-Informationen:', error);
    // Versuchen wir eine andere API-Strategie - suchen nach dem Namen
    try {
      const searchResponse = await axios.get(`${EXTERNAL_API.BASE_URL}/pokemon-species/${name.toLowerCase()}`);
      const searchData = searchResponse.data;
      
      if (searchData && searchData.id) {
        const pokedexNumber = searchData.id;
        pokemon.pokedexNumber = String(pokedexNumber).padStart(3, '0');
        
        // Mit der ID eine neue Anfrage stellen
        fetchPokemonInfoByNumber(String(pokedexNumber));
      }
    } catch (secondError) {
      console.error('Auch alternative Suche fehlgeschlagen:', secondError);
      
      // Generiere einen Fallback-Bildpfad, wenn eine Nummer vorhanden ist
      if (pokemon.pokedexNumber) {
        pokemon.imageUrl = EXTERNAL_API.SPRITE_URL(pokemon.pokedexNumber);
        onImageLoad();
      }
    }
  } finally {
    isLoading.value = false;
  }
};

// Funktion zur Generierung einer zufälligen Pokédex-Nummer
const generateRandomPokedexNumber = () => {
  // Zufallszahl zwischen 1 und MAX_POKEDEX_NUMBER generieren
  const randomNum = Math.floor(Math.random() * APP_CONFIG.MAX_POKEDEX_NUMBER) + 1;
  // Format als 3-stellige Zahl (z.B. "001", "025", "150")
  const formattedNumber = String(randomNum).padStart(3, '0');
  
  // Setze die generierte Nummer und hole Pokemon-Daten
  pokemon.pokedexNumber = formattedNumber;
  fetchPokemonInfoByNumber(formattedNumber);
};

// Debounced Versionen der API-Aufrufe erstellen
const debouncedFetchByName = debounce((name: string) => {
  if (name && name.trim() !== '' && name !== lastNameUsedForImageGeneration.value) {
    fetchPokemonInfoByName(name.trim().toLowerCase());
    lastNameUsedForImageGeneration.value = name;
  }
}, TIMEOUTS.DEBOUNCE_SEARCH);

const debouncedFetchByNumber = debounce((number: string) => {
  if (number && number.trim() !== '' && number !== lastPokedexNumberUsedForImageGeneration.value) {
    fetchPokemonInfoByNumber(number.trim());
    lastPokedexNumberUsedForImageGeneration.value = number;
  }
}, TIMEOUTS.DEBOUNCE_SEARCH);

// Überwachen des Namens für automatische Vervollständigung mit Debouncing
const watchNameChanges = (newName: string) => {
  debouncedFetchByName(newName);
};

// Überwachen der Pokédex-Nummer für automatische Vervollständigung mit Debouncing
const watchPokedexNumberChanges = (newNumber: string) => {
  debouncedFetchByNumber(newNumber);
};

// Dialog schließen
const closeDialog = () => {
  emit('update:dialog', false);
  resetForm();
};

// Formular zurücksetzen
const resetForm = () => {
  if (form.value) {
    form.value.reset();
  }
  
  // Alle Felder zurücksetzen
  pokemon.name = '';
  pokemon.pokedexNumber = '';
  pokemon.description = '';
  pokemon.type1 = null;
  pokemon.type2 = null;
  pokemon.height = null;
  pokemon.weight = null;
  pokemon.imageUrl = '';
  pokemon.category = '';
  pokemon.ability = '';
  pokemon.evolutionChainId = null;
  pokemon.evolvesFromId = null;
  errorMessage.value = '';
  
  // Zurücksetzen der letzten verwendeten Werte für die Bildgenerierung
  lastNameUsedForImageGeneration.value = '';
  lastPokedexNumberUsedForImageGeneration.value = '';
  dominantColor.value = APP_CONFIG.DEFAULT_COLOR; 
};

// Funktion zum Prüfen, ob ein Pokémon mit der angegebenen Pokédex-Nummer bereits existiert
const checkIfPokemonExists = async (pokedexNumber: string): Promise<boolean> => {
  try {
    const response = await axios.get(API_ENDPOINTS.POKEMONS + '/search', {
      params: { pokedexNumber }
    });
    
    // Wenn mindestens ein Pokémon gefunden wurde, existiert es bereits
    const exists = response.data && response.data.length > 0;
    pokemonExistsInDb.value = exists; // Setze den Graustufen-Status
    return exists;
  } catch (error) {
    console.error('Fehler beim Prüfen der Pokédex-Nummer:', error);
    pokemonExistsInDb.value = false;
    return false;
  }
};

// Pokemon speichern
const router = useRouter(); // Router-Instanz erstellen

const savePokemon = async () => {
  // Beschreibung auf die maximale Länge begrenzen (für zusätzliche Sicherheit)
  if (pokemon.description && pokemon.description.length > APP_CONFIG.MAX_DESCRIPTION_LENGTH) {
    pokemon.description = pokemon.description.substring(0, APP_CONFIG.MAX_DESCRIPTION_LENGTH);
  }
  
  isLoading.value = true;
  errorMessage.value = '';
  
  try {
    // Prüfen, ob ein Pokémon mit dieser Pokédex-Nummer bereits existiert
    const pokemonExists = await checkIfPokemonExists(pokemon.pokedexNumber);
    
    if (pokemonExists) {
      errorMessage.value = `Ein Pokémon mit der Pokédex-Nummer ${pokemon.pokedexNumber} existiert bereits in deinem Pokédex!`;
      isLoading.value = false;
      return;
    }
    
    const response = await axios.post<Pokemon>(API_ENDPOINTS.POKEMONS, pokemon);
    
    // Lokales Event emittieren (für parent component)
    emit('pokemon-created', response.data);
    // Globales Event emittieren (für HomeView)
    eventBus.emit('pokemon-created', response.data);
    
    // Dialog schließen
    closeDialog();
    
    // Zur Detailansicht des neu erstellten Pokémons navigieren
    router.push({ name: 'pokemonDetail', params: { id: response.data.pokedexNumber } });
  } catch (error) {
    console.error('Fehler beim Speichern des Pokémon:', error);
    errorMessage.value = 'Fehler beim Speichern des Pokémon. Bitte versuche es erneut.';
  } finally {
    isLoading.value = false;
  }
};

// Watch for prefillData changes to update the form
watch(() => props.prefillData, (newPrefillData) => {
  if (newPrefillData) {
    if (newPrefillData.name) {
      pokemon.name = newPrefillData.name;
    }
    
    if (newPrefillData.pokedexNumber) {
      pokemon.pokedexNumber = newPrefillData.pokedexNumber;
      // Automatically fetch Pokemon info when pokedexNumber is provided
      if (pokemon.pokedexNumber && pokemon.pokedexNumber.trim() !== '') {
        fetchPokemonInfoByNumber(pokemon.pokedexNumber);
      }
    }
    
    if (newPrefillData.evolutionChainId) {
      pokemon.evolutionChainId = newPrefillData.evolutionChainId;
    }
    
    // Prefill other fields if provided
    if (newPrefillData.evolvesFromId) {
      pokemon.evolvesFromId = newPrefillData.evolvesFromId;
    }
    
    if (newPrefillData.type1) {
      pokemon.type1 = newPrefillData.type1;
    }
    
    if (newPrefillData.imageUrl) {
      pokemon.imageUrl = newPrefillData.imageUrl;
      onImageLoad();
    }
  }
}, { immediate: true });

// Bei Dialog-Öffnung Farbe extrahieren wenn Bild bereits da ist
watch(() => props.dialog, (newVal) => {
  if (newVal && pokemon.imageUrl) {
    onImageLoad();
  }
});

// Minimales Watcher Setup für die Validierung
watch([() => pokemon.name, () => pokemon.pokedexNumber, () => pokemon.type1], 
  () => {
    const hasRequiredFields = 
      pokemon.name && 
      pokemon.name.trim() !== '' && 
      pokemon.pokedexNumber && 
      pokemon.pokedexNumber.trim() !== '' && 
      /^\d+$/.test(pokemon.pokedexNumber.trim()) &&
      pokemon.type1 !== null;

    valid.value = hasRequiredFields;
  }, 
  { immediate: true }
);

// Watch für Dialogstatus
defineExpose({ closeDialog });
</script>

<style scoped>
/* Styling für den Dialog selbst */
:deep(.pokemon-dialog-wrapper) {
  box-shadow: none;
  background: transparent;
}

/* Entferne den unnötigen Container-Wrapper */
.pokemon-main-card {
  width: 100%;
  max-width: 600px;
  min-height: 520px;
  background: white;
  padding-top: 240px; /* Angepasst für das schwebende Bild */
  border-radius: 16px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  margin: 0 auto;
  overflow: hidden;
  position: relative; /* Notwendig für die absolute Positionierung des Bildes */
}

.floating-image-container {
  position: absolute;
  top: 30px; /* Standardposition für kleine Bildschirme */
  left: 50%;
  transform: translateX(-50%);
  z-index: 10;
  width: 240px; /* von 320px auf 240px reduziert */
}

/* Anpassungen für mittlere und größere Bildschirme */
@media (min-width: 601px) {
  .floating-image-container {
    top: -20px; /* Noch höhere Position für mittlere Bildschirme */
  }
}

@media (min-width: 960px) {
  .floating-image-container {
    top: -30px; /* Noch höhere Position für große Bildschirme */
  }
}

.pokemon-floating-image {
  width: 240px; /* von 320px auf 240px reduziert */
  height: 240px; /* von 320px auf 240px reduziert */
}

.card-header {
  padding: 16px 24px;
}

.level-text {
  font-size: 16px;
  color: #b20072;
  font-weight: 600;
  margin-bottom: 16px;
  text-align: center;
}

.pokemon-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-section {
  margin-bottom: 8px;
}

.section-title {
  font-size: 15px;
  color: #555;
  margin-bottom: 4px;
  font-weight: 500;
}

.form-row {
  display: flex;
  gap: 16px;
}

.form-row > * {
  flex: 1;
}

.image-url-container {
  margin-top: 8px;
  border-top: 1px dashed #ddd;
  padding-top: 8px;
}

.pokemon-footer {
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  margin-top: 16px;
  transition: background-color 0.5s ease;
}

.footer-section {
  display: flex;
  gap: 16px;
}

.measurements {
  display: flex;
  gap: 24px;
}

.footer-value {
  text-align: center;
}

.footer-value span {
  display: block;
  font-size: 12px;
  font-weight: 400;
  margin-top: -8px;
}

.footer-input {
  color: white !important;
  max-width: 80px;
}

.footer-input :deep(input) {
  color: white !important;
  text-align: center;
  font-weight: bold;
}

.actions {
  display: flex;
  gap: 8px;
}

.save-btn {
  background-color: rgba(255, 255, 255, 0.2) !important;
}

/* Optimierungen für kleine Bildschirme */
@media (max-width: 600px) {
  .form-row {
    flex-direction: column;
    gap: 0;
  }
  
  .pokemon-footer {
    flex-direction: column;
    gap: 16px;
  }
  
  .floating-image-container {
    width: 180px;
  }
  
  .pokemon-floating-image {
    width: 180px;
    height: 180px;
  }
}

.greyscale-filter {
  filter: grayscale(100%) opacity(0.6);
  transition: filter 0.3s ease-in-out;
}

.greyscale-filter:hover {
  filter: grayscale(50%) opacity(0.8);
}
</style>