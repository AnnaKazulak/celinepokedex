import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

// Material Design Icons importieren
import '@mdi/font/css/materialdesignicons.css'

// Vuetify importieren
import { createVuetify } from 'vuetify'
import 'vuetify/styles' 
import { aliases, mdi } from 'vuetify/iconsets/mdi' 
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
app.use(vuetify) 
app.mount('#app')
