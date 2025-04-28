# CelinePokedex

Willkommen zu **CelinePokedex** – einer einfachen Pokédex-Webanwendung! 🧡

Dieses Projekt besteht aus:
- **Frontend**: Vue 3 + Vite + Vuetify
- **Backend**: Spring Boot (Java)
- **Datenbank**: (Wird bald hinzugefügt!)

## Funktionen ✨
- Anzeige einer Liste von Pokémon
- Details wie Name, Pokédex-Nummer, Typen, Größe und Gewicht
- Moderne UI mit Vuetify
- Getrennte Frontend-/Backend-Architektur
- API-Anbindung an das Backend

## Lokale Entwicklung 🚀

### Voraussetzungen
- Node.js & npm
- Java 17+
- Maven

### Starten des Frontends
```bash
cd frontend
npm install
npm run dev

### Starten des Backends
```bash
./mvnw spring-boot:run

Das Frontend läuft dann auf http://localhost:5173

Das Backend läuft auf http://localhost:8080

### Tests
```bash
cd frontend
npm run test:unit

### Status
✅ Frontend- und Backend-Grundstruktur fertig
✅ Erste API-Endpunkte bereit
❌ Anbindung einer kostenlosen Open Source-Datenbank
❌ Deployment (bald)