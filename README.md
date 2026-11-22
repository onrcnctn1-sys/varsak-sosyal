# Varsak Sosyal

Varsak Veteriner Polikliniği için veteriner sosyal medya içerik radarı. Dünyadaki ve Türkiye'deki
veteriner hesaplarını, öne çıkan video formatlarını, gündemdeki konuları ve klinik için video fikirlerini gösterir.

- **Web / telefon:** https://onrcnctn1-sys.github.io/varsak-sosyal/
- **Android APK:** https://github.com/onrcnctn1-sys/varsak-sosyal/releases/latest/download/varsak-sosyal.apk

## Nasıl çalışır

- `index.html` uygulamanın kendisi; `data/*.json` dosyalarındaki verileri gösterir ve açıkken her 10 dakikada bir yeni veri olup olmadığına bakar.
- `data/` klasörü, Claude'un zamanlanmış görevi tarafından saatte bir herkese açık kaynaklardan güncellenir.
- `android/` klasörü, siteyi tam ekran açan küçük bir Android uygulaması. `android/` değiştikçe GitHub Actions APK'yı otomatik derler ve yukarıdaki indirme linkine koyar.

Not: APK, depoda duran herkese açık bir "debug" anahtarıyla imzalanır. Kişisel kullanım için yeterlidir; Google Play'e yüklemek için ayrı, gizli bir anahtar gerekir.
