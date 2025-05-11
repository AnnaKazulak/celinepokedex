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

// Import axios and our custom configured instance
import axios from 'axios'
import axiosInstance from './utils/axiosConfig'

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

// Replace global axios with our configured instance
axios.defaults.timeout = axiosInstance.defaults.timeout
// Copy interceptors
axios.interceptors.request = axiosInstance.interceptors.request
axios.interceptors.response = axiosInstance.interceptors.response

app.use(router)
app.use(vuetify) 
app.mount('#app')
