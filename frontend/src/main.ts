import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

// Vuetify importieren
import { createVuetify } from 'vuetify'
import 'vuetify/styles' // Vuetify CSS laden
import { aliases, mdi } from 'vuetify/iconsets/mdi' // Icons optional
import * as components from 'vuetify/components'
import * as directives from 'vuetify/directives'

// Vuetify-Instanz erstellen
const vuetify = createVuetify({
  components,
  directives,
  icons: {
    defaultSet: 'mdi',
    aliases,
    sets: {
      mdi,
    },
  },
})

const app = createApp(App)

app.use(router)
app.use(vuetify) // Vuetify einbinden
app.mount('#app')
