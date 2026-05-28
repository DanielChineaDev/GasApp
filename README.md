# ⛽ GasApp — Gasolina barata España

Una aplicación Android moderna y rápida para encontrar las gasolineras más baratas de España **en tiempo real**, comparar precios de combustible, navegar hasta ellas y llevar un control privado de tus repostajes con estadísticas inteligentes.

Los precios provienen de la **API oficial y gratuita del Gobierno** (MITECO), que publica todas las estaciones de servicio de España con actualizaciones varias veces al día.

> **Ahorra dinero repostando inteligentemente.**

---

## 🎯 Funcionalidades principales

### 🔎 Busca y explora
- **Dashboard de inicio** con la gasolinera más barata cerca de ti.
- **Lista** ordenada por precio/distancia/valor, con **buscador insensible a acentos**.
- **Heatmap de precios** (verde/ámbar/rojo según media de zona).
- **Mapa** con clústeres de gasolineras y carteles de precio; panel inferior con info rápida.
- **Selector de combustible**: Gasolina 95, Gasolina 98, Diésel, Diésel Premium.
- **Filtros inteligentes**: distancia (slider 1-30 km, default 5), marca (normalizadas), "abiertas ahora".
- **Ubicación en tiempo real** y distancia exacta; **pull-to-refresh**.
- **Detalle** con foto (Street View), histórico de precios y "Ir allí" (Google Maps/web).

### 👤 Sincronización y personalización
- **Login** con email/contraseña y **Google** (Credential Manager).
- **Favoritas** y **combustible por defecto** sincronizados en Firestore.
- Perfil editable con avatar circular.
- **Onboarding** interactivo la primera vez.
- **Ajustes**: tema claro/oscuro, Material You (opcional), combustible, alertas.
- Botón "Volver a ver tour inicial" en Ajustes.

### 💰 Ahorro e inteligencia
- **Dinero ahorrado**: calcula automáticamente cuánto ahorras repostando por debajo de la media.
- **Sistema de logros** (requiere sesión): primeros repostajes, coleccionista, ahorrador, mecenas.
- **Notificaciones** de bajada de precio en favoritas y **alertas personalizadas**.
- **Refresco automático** en segundo plano (WorkManager).

### 📊 Estadísticas y herramientas
- **Gráfico de gasto** mensual con exportación a CSV.
- **Consumo real** (L/100 km) y coste/km calculados automáticamente.
- **Modo ahorro**: encuentra gasolineras baratas en tu ruta.
- **Modo coche**: precio en grande para ver al volante.
- **OCR del ticket** (ML Kit): registra repostajes escaneando fotos.
- **Multi-vehículo**: gestiona consumo y preferencias por coche.
- **Calendario de gastos**: navega meses pasados/futuros.

### 🎁 Sistema
- **Widget** con favoritas más baratas.
- **Atajos de app** para acceso rápido.
- **Avatares de marca** en mapas y listas.
- **Reseñas** de gasolineras (Firestore).
- **Códigos promocionales** para quitar anuncios.

---

## 🏗️ Arquitectura

**MVVM + Jetpack Compose**, con separación clara en capas:

```
domain/   → Modelos, interfaces de repositorios, utilidades de negocio
data/     → API (Retrofit), BD local (Room), localización, ajustes (DataStore),
            remoto (Firestore), mapeos, implementaciones de repositorios
ui/       → Pantallas Compose, ViewModels (StateFlow), navegación
di/       → Inyección Hilt: network, BD, ubicación, ajustes, Firebase, repositorios
work/     → WorkManager (refresco en background)
widget/   → Glance (home screen widget)
notifications/ → Firebase Cloud Messaging + push locales
```

**Stack tecnológico:**
- **Kotlin** · **Jetpack Compose** (Material 3 + Material You opcional)
- **Hilt** · **Retrofit** + **kotlinx.serialization** (parser decimal español)
- **Room** (v1→v5 migraciones reales) · **DataStore**
- **Coroutines** · **Flow** · **Navigation Compose**
- **Google Maps Compose** (clústeres, Street View)
- **FusedLocation** · **Geocoder**
- **WorkManager** · **Glance** · **ML Kit Text Recognition**
- **Credential Manager** · **Firebase (Auth + Firestore)**
- **AdMob** + **Google Play Billing** (eliminar anuncios)

---

## 📱 Requisitos y configuración

### Antes de compilar

El proyecto necesita archivos locales que **no se versionan**:

#### 1. `local.properties`

```properties
MAPS_API_KEY=tu_clave_de_google_maps
WEB_CLIENT_ID=xxxxx.apps.googleusercontent.com
ADMOB_APP_ID=ca-app-pub-xxxxxxxxxxxxxxxx~yyyyyyyyy
ADMOB_BANNER_UNIT=ca-app-pub-xxxxxxxxxxxxxxxx/zzzzzzzzzz
```

- **MAPS_API_KEY**: desde Google Cloud Console → *Maps SDK for Android* + *Street View Static API*
- **WEB_CLIENT_ID**: desde Firebase Console → Authentication → OAuth
- **AdMob**: desde AdMob Console (opcional, para anuncios)

#### 2. `app/google-services.json`

Descárgalo de [Firebase Console](https://console.firebase.google.com) (proyecto Android `com.bpo.gasapp`).

Habilita:
- **Authentication**: Email + Google
- **Firestore**: almacena favoritas, reseñas, códigos promocionales

Añade tu **huella SHA-1** en Firebase → Authentication → Sign-in methods → Google.

#### 3. `firestore.rules`

Ya está versionado. Despliega con:
```bash
firebase deploy --only firestore:rules
```

### Compilar en debug

```bash
./gradlew :app:assembleDebug
```

---

## 🏭 Compilar para producción

### 1. Crear keystore (una vez)

```bash
keytool -genkey -v -keystore release.keystore -alias gasapp \
  -keyalg RSA -keysize 2048 -validity 10000
```

### 2. Crear `keystore.properties` (NO se versiona)

```properties
storeFile=release.keystore
storePassword=tu_password_seguro
keyAlias=gasapp
keyPassword=tu_password_seguro
```

### 3. Generar artefactos (R8 + shrink de recursos)

```bash
./gradlew :app:assembleRelease     # APK
./gradlew :app:bundleRelease       # AAB para Google Play
```

> **Nota:** La BD exporta su esquema en `app/schemas/` y usa **migraciones reales**, conservando datos del usuario al subir de versión.

### 4. Registrar SHA-1 en Firebase

Añade la huella SHA-1 del keystore de release en Firebase.

---

## 🚀 Características avanzadas

### Login con favoritas locales
Al iniciar sesión, si hay favoritas guardadas localmente, GasApp pregunta:
- **Fusionar**: une favoritas locales y remotas.
- **Conservar solo estas**: local gana.
- **Descartar**: usa las de la cuenta.

### Cambio automático de combustible
Seleccionar un vehículo cambia automáticamente:
- Combustible por defecto.
- Selector de combustible en la lista y filtros.
- Combustible en las estadísticas.

### Aprende el diseño
El tour inicial se puede reproducir en cualquier momento desde **Ajustes**.

---

## 🛠️ Configuración del proyecto

- **Package**: `com.bpo.gasapp`
- **minSdk**: 24 · **targetSdk**: 35
- **AGP**: 8.7.3 · **Kotlin**: 2.0.21
- **Firebase BoM**: 33.7.0 (compatible con Kotlin 2.0)

---

## 💝 Apoya el desarrollo

GasApp es **100 % gratuita**. Si te resulta útil, considera invitarme a un café en **[Ko-fi](https://ko-fi.com/josedanielchinea)** ☕

---

## 📄 Licencia

Desarrollado por **Jose Daniel Chinea** · Contacto: `info@gasapp.cloud`

> "Información oficial de precios en tiempo real. Hecha para ahorrar."
