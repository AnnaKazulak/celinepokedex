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

    const cols = wrapper.findAll('.v-col');
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
});
