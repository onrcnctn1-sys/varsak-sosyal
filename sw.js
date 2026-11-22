// Varsak Sosyal service worker: uygulama kabuğu önbellekte, veri her zaman önce ağdan
const CACHE = "varsak-v5";
const SHELL = ["./", "./index.html", "./manifest.webmanifest", "./icons/varsak-192.png", "./icons/varsak-512.png", "./assets/hero.svg", "./assets/kedi.svg", "./assets/kopek.svg", "./assets/kus.svg", "./assets/mama.svg", "./assets/asi.svg", "./assets/stetoskop.svg", "./assets/telefon.svg", "./assets/kamera.svg", "./assets/liste.svg", "./assets/ucret.svg", "./assets/egitim.svg", "./assets/kupa.svg", "./assets/makas.svg"];
self.addEventListener("install", (e) => { e.waitUntil(caches.open(CACHE).then((c) => c.addAll(SHELL))); self.skipWaiting(); });
self.addEventListener("activate", (e) => {
  e.waitUntil(caches.keys().then((ks) => Promise.all(ks.filter((k) => k !== CACHE).map((k) => caches.delete(k)))));
  self.clients.claim();
});
self.addEventListener("fetch", (e) => {
  const url = new URL(e.request.url);
  if (e.request.method !== "GET" || url.origin !== location.origin) return;
  const isData = url.pathname.includes("/data/");
  if (isData || e.request.mode === "navigate") {
    // Ağ önce; çevrimdışıysa son kopya
    const key = new Request(url.origin + url.pathname);
    e.respondWith(fetch(e.request, { cache: "no-store" }).then((r) => { const cp = r.clone(); caches.open(CACHE).then((c) => c.put(key, cp)); return r; })
      .catch(() => caches.match(key).then((m) => m || caches.match("./index.html"))));
    return;
  }
  e.respondWith(caches.match(e.request).then((m) => m || fetch(e.request)));
});
