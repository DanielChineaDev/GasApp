# Recursos gráficos para Google Play

Generados a partir del logo y de capturas reales de la app
(`tools/generate_play_assets.py`).

## Contenido y dónde se suben en Play Console

| Archivo | Tamaño | Dónde |
|---|---|---|
| `play-icon-512.png` | 512×512 | *Icono de la app* (ficha de Play Store) |
| `feature-graphic-1024x500.png` | 1024×500 | *Gráfico de funciones* (Feature graphic) |
| `phone/phone-*.png` | 1080×1920 | *Capturas de teléfono* (mín. 2, hasta 8) |
| `tablet/tablet-*.png` | 2560×1600 | *Capturas de tablet 7"/10"* |
| `grafico-funciones.png` | 1400×1500 | Infografía de funciones (web, redes, o captura extra) |

## Notas
- Las capturas de teléfono usan pantallas reales (Inicio, Mapa, Favoritas) con
  titulares de marketing y marco de dispositivo.
- Las de tablet muestran dos teléfonos + titular y ventajas.
- Para regenerar todo: `python tools/generate_play_assets.py`
- Las capturas fuente están en `landing/assets/screen-*.png`. Si actualizas la
  UI, vuelve a capturar y regenera.

## Icono de la app (Android)
El icono del launcher ya está aplicado en el proyecto:
- `app/src/main/res/mipmap-*/ic_launcher*.png` (icono y versión redonda)
- `app/src/main/res/mipmap-*/ic_launcher_foreground.png` (gota, primer plano adaptativo)
- `app/src/main/res/drawable/ic_launcher_background.xml` (degradado azul de fondo)
