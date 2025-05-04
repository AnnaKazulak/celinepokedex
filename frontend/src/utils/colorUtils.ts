// Hilfsfunktion zur Extraktion der dominanten Farbe aus einem Bild
export function extractDominantColor(imgUrl: string): Promise<string> {
  return new Promise((resolve) => {
    const img = new Image();
    img.crossOrigin = 'Anonymous';
    img.src = imgUrl;
    
    img.onload = () => {
      // Canvas erstellen, um das Bild zu analysieren
      const canvas = document.createElement('canvas');
      const ctx = canvas.getContext('2d');
      const size = 50; // Skalierte Größe für schnellere Analyse
      
      canvas.width = size;
      canvas.height = size;
      
      ctx.drawImage(img, 0, 0, size, size);
      
      // Pixel-Daten sammeln
      const imageData = ctx.getImageData(0, 0, size, size).data;
      const colorCounts = {};
      let dominantColor = '#b20072'; // Standardfarbe als Fallback
      let maxCount = 0;
      
      // RGB-Werte jeden 5. Pixels analysieren (zur Optimierung)
      for(let i = 0; i < imageData.length; i += 20) {
        const r = imageData[i];
        const g = imageData[i + 1];
        const b = imageData[i + 2];
        const a = imageData[i + 3];
        
        // Transparente Pixel überspringen
        if (a < 125) continue;
        
        // Zu helle und zu dunkle Farben überspringen (oft Hintergrund)
        const brightness = (r + g + b) / 3;
        if (brightness < 20 || brightness > 230) continue;
        
        const rgb = `${r},${g},${b}`;
        
        if (!colorCounts[rgb]) {
          colorCounts[rgb] = 0;
        }
        
        colorCounts[rgb]++;
        
        if (colorCounts[rgb] > maxCount) {
          maxCount = colorCounts[rgb];
          dominantColor = `rgb(${rgb})`;
        }
      }
      
      resolve(dominantColor);
    };
    
    img.onerror = () => {
      resolve('#b20072'); // Fallback-Farbe bei Fehler
    };
  });
}