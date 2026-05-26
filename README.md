# GasApp — Gasolina barata España

Aplicación Android para encontrar las gasolineras más baratas de España en
tiempo real, comparar precios de combustible y navegar hasta ellas.

Los precios provienen de la **API oficial y gratuita del Gobierno**
(Ministerio para la Transición Ecológica), que publica todas las estaciones de
servicio de España con sus precios actualizados varias veces al día.

## Funcionalidades

- **Lista de gasolineras** ordenada por precio, con la más barata resaltada.
- **Selector de combustible**: Gasolina 95, Gasolina 98, Diésel, Diésel Premium.
- **Ubicación y distancia**: muestra la distancia a cada gasolinera.
- **Filtros**: distancia (1/5/10/25 km), marca y "abiertas ahora".
- **Detalle** de cada gasolinera con todos los precios y botón "Ir allí"
  (navegación con Google Maps).
- **Favoritas** con acceso rápido desde una pestaña dedicada.
- **Cache local** (Room) para uso offline parcial.

## Arquitectura

MVVM + Jetpack Compose, con separación en capas:

```
domain/   modelos, repositorio (interfaz), utilidades de negocio
data/      API (Retrofit), cache (Room), localización, mapeadores, repositorio
ui/        pantallas Compose + ViewModels (StateFlow)
di/        módulos Hilt (network, database, location, repository)
```

**Stack principal:** Kotlin · Jetpack Compose (Material 3) · Hilt ·
Retrofit + kotlinx.serialization · Room · Coroutines/Flow · Navigation Compose ·
Google Maps Compose · FusedLocation · Firebase (Auth + Firestore).

## Configuración para compilar

El proyecto necesita dos archivos locales que **no** se versionan:

1. **`local.properties`** — tu clave de Google Maps:

   ```
   MAPS_API_KEY=tu_clave_de_google_maps
   ```

   Crea la clave en Google Cloud Console habilitando *Maps SDK for Android*.

2. **`app/google-services.json`** — descárgalo de tu proyecto en
   [Firebase Console](https://console.firebase.google.com) (app Android con
   package `com.bpo.gasapp`).

Después, abre el proyecto en Android Studio y ejecuta, o desde la terminal:

```
./gradlew :app:assembleDebug
```

## Datos del proyecto

- **Package:** `com.bpo.gasapp`
- **minSdk:** 24 · **targetSdk:** 35
