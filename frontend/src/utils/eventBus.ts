import { ref } from 'vue';
import type { Pokemon } from '../types/pokemon';

// Interface für Event-Handler-Funktionen
type EventCallback<T = any> = (...args: T[]) => void;

// Interface für Listener-Map
interface EventListeners {
  [event: string]: EventCallback[];
}

// Eine typsichere Event-Bus-Implementierung
const listeners = ref<EventListeners>({});

export const eventBus = {
  // Event registrieren
  on<T>(event: string, callback: EventCallback<T>): void {
    if (!listeners.value[event]) {
      listeners.value[event] = [];
    }
    listeners.value[event].push(callback as EventCallback);
  },
  
  // Event auslösen
  emit<T>(event: string, ...args: T[]): void {
    if (listeners.value[event]) {
      listeners.value[event].forEach(callback => callback(...args));
    }
  },
  
  // Event deregistrieren
  off(event: string, callback?: EventCallback): void {
    if (listeners.value[event]) {
      if (callback) {
        listeners.value[event] = listeners.value[event].filter(cb => cb !== callback);
      } else {
        listeners.value[event] = [];
      }
    }
  }
};

// Typen für spezifische Events
export interface EventMap {
  'pokemon-created': [Pokemon];
  'register-pokemon-color': [{id: number | string, color: string, element: HTMLElement | null}];
  'detail-page-color-change': [string]; // Neues Event für die Detail-Seite
  // Hier können weitere Event-Typen definiert werden
}