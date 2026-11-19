// Service Worker - NEON PULSE MUSIC
// Cache-first offline asset caching & runtime resilience
const CACHE_NAME = 'neon-pulse-v1.0.0';
const STATIC_ASSETS = [
  './',
  './index.html',
  './manifest.json',
  './sw.js',
  './icon-192.png',
  './icon-512.png',
  'https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;600;700;800&family=Space+Mono:wght@400;700&display=swap'
];

// 1. Instalação e pré-cache dos assets estáticos
self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => {
      console.log('[NEON PULSE SW] Pré-armazenando assets estáticos no cache...');
      return cache.addAll(STATIC_ASSETS).catch((err) => {
        console.warn('[NEON PULSE SW] Alguns assets remotos podem falhar no pre-cache inicial:', err);
      });
    }).then(() => self.skipWaiting())
  );
});

// 2. Ativação e limpeza de versões anteriores de cache
self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys().then((keys) => {
      return Promise.all(
        keys.map((key) => {
          if (key !== CACHE_NAME) {
            console.log('[NEON PULSE SW] Removendo cache legado:', key);
            return caches.delete(key);
          }
        })
      );
    }).then(() => self.clients.claim())
  );
});

// 3. Interceptação de requisições com estratégia Cache First e fallback de rede
self.addEventListener('fetch', (event) => {
  // Ignora esquemas não-HTTP (ex: blob: das músicas locais ou chrome-extension:)
  if (!event.request.url.startsWith('http')) return;

  event.respondWith(
    caches.match(event.request).then((cachedResponse) => {
      if (cachedResponse) {
        return cachedResponse;
      }
      return fetch(event.request)
        .then((networkResponse) => {
          if (!networkResponse || networkResponse.status !== 200 || networkResponse.type !== 'basic') {
            return networkResponse;
          }
          const responseToCache = networkResponse.clone();
          caches.open(CACHE_NAME).then((cache) => {
            cache.put(event.request, responseToCache);
          });
          return networkResponse;
        })
        .catch(() => {
          // Fallback offline se tudo falhar
          if (event.request.mode === 'navigate') {
            return caches.match('./index.html');
          }
        });
    })
  );
});
