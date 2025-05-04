import { createVuetify } from 'vuetify';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { mount, flushPromises } from '@vue/test-utils';
import HomeView from '../views/HomeView.vue';
import type { Mock } from 'vitest';
import axios from 'axios';

vi.mock('axios');

describe('HomeView.vue', () => {
  let vuetify: any;
  const globalConfig = {
    plugins: [] as any[],
    stubs: {
      'v-container': { template: '<div><slot /></div>' },
      'v-row': { template: '<div><slot /></div>' },
      'v-col': { template: '<div class="v-col"><slot /></div>' },
      'v-card': { template: '<div><slot /></div>' },
      'v-img': { template: '<img />' },
      'v-card-title': { template: '<div class="v-card-title"><slot /></div>' },
      'v-card-subtitle': { template: '<div><slot /></div>' },
      'v-card-text': { template: '<div><slot /></div>' },
      'v-text-field': { 
        template: '<input class="v-text-field" :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />', 
        props: ['modelValue'] 
      },
      'v-alert': { template: '<div class="v-alert"><slot /></div>' },
      'v-progress-circular': { template: '<div class="v-progress-circular"></div>' }
    },
  };

  beforeEach(() => {
    vuetify = createVuetify();
    globalConfig.plugins = [vuetify];

    (axios.get as Mock).mockResolvedValue({
      data: [
        { id: 1, name: 'Pikachu', pokedexNumber: '025', imageUrl: '', description: '', type1: 'Elektro', type2: null, height: 0.4, weight: 6.0 },
        { id: 2, name: 'Glumanda', pokedexNumber: '004', imageUrl: '', description: '', type1: 'Feuer', type2: null, height: 0.6, weight: 8.5 },
        { id: 3, name: 'Schiggy', pokedexNumber: '007', imageUrl: '', description: '', type1: 'Wasser', type2: null, height: 0.5, weight: 9.0 },
      ],
    });
  });

  it('renders one v-col per Pokémon', async () => {
    const wrapper = mount(HomeView, { global: globalConfig });

    await flushPromises();

    // Nur die v-cols für Pokémon, nicht die zusätzlichen v-cols
    const cols = wrapper.findAll('.v-col:has(.v-card-title)');
    expect(cols.length).toBe(3);
  });

  it('displays Pokémon names correctly', async () => {
    const wrapper = mount(HomeView, { global: globalConfig });

    await flushPromises();

    const titles = wrapper.findAll('.v-card-title');
    expect(titles).toHaveLength(3);
    expect(titles[0].text()).toBe('Pikachu');
    expect(titles[1].text()).toBe('Glumanda');
    expect(titles[2].text()).toBe('Schiggy');
  });
  
  it('filters Pokémon based on search query', async () => {
    const wrapper = mount(HomeView, { global: globalConfig });
    await flushPromises();
    
    // Überprüfen, dass anfangs alle Pokémon angezeigt werden
    let cols = wrapper.findAll('.v-col:has(.v-card-title)');
    expect(cols.length).toBe(3);
    
    // Nach "Pika" suchen (client-side filtering)
    const searchField = wrapper.find('.v-text-field');
    await searchField.setValue('Pika');
    await wrapper.vm.$nextTick();
    
    // Überprüfen, dass nur Pikachu angezeigt wird
    cols = wrapper.findAll('.v-col:has(.v-card-title)');
    expect(cols.length).toBe(1);
    expect(wrapper.find('.v-card-title').text()).toBe('Pikachu');
    
    // Suche zurücksetzen
    await searchField.setValue('');
    await wrapper.vm.$nextTick();
    
    // Überprüfen, dass wieder alle Pokémon angezeigt werden
    cols = wrapper.findAll('.v-col:has(.v-card-title)');
    expect(cols.length).toBe(3);
  });
  
  it('shows alert when no Pokémon match the search', async () => {
    const wrapper = mount(HomeView, { global: globalConfig });
    await flushPromises();
    
    // Nach nicht existierendem Pokémon suchen
    const searchField = wrapper.find('.v-text-field');
    await searchField.setValue('Nicht existierendes Pokémon');
    await wrapper.vm.$nextTick();
    
    // Überprüfen, dass keine Pokémon angezeigt werden
    const cols = wrapper.findAll('.v-col:has(.v-card-title)');
    expect(cols.length).toBe(0);
    
    // Überprüfen, dass die Meldung "Keine Pokémon gefunden" angezeigt wird
    const alert = wrapper.find('.v-alert');
    expect(alert.exists()).toBe(true);
  });
});
