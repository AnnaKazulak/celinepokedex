import { createRouter, createWebHistory } from 'vue-router'
import { eventBus } from '@/utils/eventBus'
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

// Add a global after-navigation hook to reset navbar when returning to home
router.afterEach((to, from) => {
  // Only trigger reset when navigating to home from a detail view
  if (to.name === 'home' && 
     (from.name === 'pokemonDetail' || from.name === 'fantasyCharacterDetail')) {
    
    // Direct event to force navbar reset - will be handled by Navbar component
    setTimeout(() => {
      eventBus.emit('force-navbar-reset', { fromRoute: from.name });
    }, 50); // Short delay to ensure Vue has updated the DOM
  }
})

export default router
