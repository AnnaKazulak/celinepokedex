import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeView.vue'
import PokemonDetailView from '@/views/PokemonDetailView.vue'
import FantasyCharacterDetailView from '@/views/FantasyCharacterDetailView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/pokemon/:id',
      name: 'pokemonDetail',
      component: PokemonDetailView,
      props: true
    },
    {
      path: '/fantasy-character/:id',
      name: 'fantasyCharacterDetail',
      component: FantasyCharacterDetailView,
      props: true
    },
  ],
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

export default router
