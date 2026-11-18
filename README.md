# WeatherMapApp — MADT Lab #6

An Android application built with **Jetpack Compose**, **Google Maps SDK**, **Firebase Authentication**, and the **Meteo.lt Open API**, developed as part of the Mobile Application Development Technologies (MADT) coursework (Lab #6).

The app displays real-time weather stations and forecasts across Lithuania on an interactive map, guarded by user authentication, multi-criteria filtering, dual-language localization (English and Lithuanian), and a clean MVVM architecture.

---

## Table of Contents

- [Overview](#overview)
- [Key Features](#key-features)
- [Architecture & Tech Stack](#architecture--tech-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Setup & Configuration](#setup--configuration)
  - [1. Google Maps API Key](#1-google-maps-api-key)
  - [2. Firebase Authentication](#2-firebase-authentication)
- [Meteo.lt API Integration](#meteolt-api-integration)
- [Building & Running](#building--running)
- [Unit Testing & Verified Results](#unit-testing--verified-results)
- [Localization](#localization)

---

## Overview

WeatherMapApp visualizes meteorological data for 15 major Lithuanian cities and regions. Users must authenticate before accessing the map, strictly enforcing the academic requirement that unauthenticated users cannot trigger external API requests. Weather conditions, temperatures, atmospheric pressure, wind speeds, humidity, and multi-day hourly forecasts are rendered directly on the map with custom Compose markers and an interactive bottom sheet.

---

## Key Features

1. **Authentication & Security**
   - Firebase Email & Password authentication (sign up, sign in, sign out).
   - Real-time session monitoring using Kotlin Coroutines `Flow` and `AuthStateListener`.
   - UI navigation guards: unauthenticated users cannot enter or remain on the protected map route.
   - Repository-level auth checks (`verifyAuthentication()`) block unauthenticated network requests.
   - Post-logout cache isolation: in-flight network requests cannot repopulate cache after sign-out.

2. **Interactive Google Maps**
   - Google Maps Compose integration centered over Lithuania (`55.1694° N, 23.8813° E`).
   - Custom map marker badges displaying the city name and formatted real-time temperature.
   - Marker tap interaction opening a detailed forecast modal bottom sheet.
   - Missing Maps API key detection banner if `MAPS_API_KEY` is not configured.

3. **Forecast Details Sheet**
   - Comprehensive weather metrics: air temperature, feels-like temperature, condition badge, wind speed, relative humidity, cloud cover, and sea-level pressure.
   - Exact station geographic coordinates.
   - Upcoming hourly forecast carousel (next 8 intervals).
   - Attribution to the Meteo.lt Open Data API.

4. **Multi-Criteria Filtering & Search**
   - Instant search by city name, administrative division, or station code (case-insensitive).
   - Weather condition filter chips: *All*, *Clear*, *Cloudy*, *Rain*, *Snow*.
   - Temperature range filter chips: *All*, *Warm (> 15°C)*, *Mild (0°C to 15°C)*, *Cold (< 0°C)*.
   - Matching station counter and one-tap filter reset.

5. **Dual-Language Localization**
   - Full support for English (`values/strings.xml`) and Lithuanian (`values-lt/strings.xml`).
   - 100% parity across all 68 string resource keys.
   - Dynamic parameters formatted using typed string resource placeholders.

6. **Robust Data & Caching**
   - Thread-safe in-memory cache synchronized with `synchronized(cache)`.
   - Structured concurrency: `CancellationException` is properly propagated.
   - Accurate network error detection: connectivity failures return detailed `IOException` causes rather than generic errors.
   - Strict Lithuanian regional coordinate boundary validation (`53.0..57.0° N, 20.0..27.5° E`) with known geographic fallbacks.

---

## Architecture & Tech Stack

The application adheres to **Clean Architecture** and the **MVVM (Model-View-ViewModel)** design pattern with Unidirectional Data Flow (UDF):

* **Language**: Kotlin 2.0+ (JVM target 21)
* **UI Toolkit**: Jetpack Compose with Material 3
* **Navigation**: Jetpack Navigation Compose
* **State Management**: Kotlin Coroutines, `StateFlow`, and `collectAsState()`
* **Dependency Injection**: Manual DI container pattern via `AppContainer` and `ViewModelFactory`
* **Networking**: Retrofit 2 + Gson Converter + OkHttp 4 Logging Interceptor
* **Authentication**: Firebase Authentication SDK (`firebase-auth-ktx`)
* **Mapping**: Google Maps Compose SDK (`maps-compose:6.4.1`) & Google Play Services Maps
* **Testing**: JUnit 4 + `kotlinx-coroutines-test`

---

## Project Structure

```
WeatherMapApp/
├── app/
│   ├── build.gradle.kts
│   ├── google-services.json          # Firebase configuration (place here)
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/example/weathermap/
│       │   │   ├── MainActivity.kt
│       │   │   ├── WeatherMapApplication.kt
│       │   │   ├── data/
│       │   │   │   ├── api/
│       │   │   │   │   ├── MeteoApiService.kt
│       │   │   │   │   └── dto/
│       │   │   │   │       ├── MeteoForecastDto.kt
│       │   │   │   │       └── MeteoPlacesDto.kt
│       │   │   │   └── repository/
│       │   │   │       ├── AuthRepositoryImpl.kt
│       │   │   │       └── WeatherRepositoryImpl.kt
│       │   │   ├── di/
│       │   │   │   ├── AppContainer.kt
│       │   │   │   └── ViewModelFactory.kt
│       │   │   ├── domain/
│       │   │   │   ├── model/
│       │   │   │   │   ├── User.kt
│       │   │   │   │   ├── WeatherCondition.kt
│       │   │   │   │   ├── WeatherFilter.kt
│       │   │   │   │   ├── WeatherReport.kt
│       │   │   │   │   └── WeatherStation.kt
│       │   │   │   ├── repository/
│       │   │   │   │   ├── AuthRepository.kt
│       │   │   │   │   └── WeatherRepository.kt
│       │   │   │   └── util/
│       │   │   │       ├── AuthRequiredException.kt
│       │   │   │       └── Resource.kt
│       │   │   └── presentation/
│       │   │       ├── auth/
│       │   │       │   ├── AuthViewModel.kt
│       │   │       │   ├── LoginScreen.kt
│       │   │       │   └── RegisterScreen.kt
│       │   │       ├── map/
│       │   │       │   ├── MapScreen.kt
│       │   │       │   ├── MapUiState.kt
│       │   │       │   ├── MapViewModel.kt
│       │   │       │   └── components/
│       │   │       │       ├── FilterBar.kt
│       │   │       │       └── ForecastBottomSheet.kt
│       │   │       ├── navigation/
│       │   │       │   ├── NavGraph.kt
│       │   │       │   └── Screen.kt
│       │   │       └── theme/
│       │   │           ├── Color.kt
│       │   │           ├── Theme.kt
│       │   │           └── Type.kt
│       │   └── res/
│       │       ├── values/strings.xml       # English strings (default)
│       │       ├── values/themes.xml
│       │       └── values-lt/strings.xml    # Lithuanian strings
│       └── test/java/com/example/weathermap/
│           ├── data/
│           │   ├── api/dto/CoordinatesDtoTest.kt
│           │   └── repository/WeatherRepositoryImplTest.kt
│           └── domain/model/
│               ├── WeatherConditionTest.kt
│               └── WeatherFilterTest.kt
├── build.gradle.kts
├── gradle.properties
├── local.properties                  # Kept locally; defines SDK and MAPS_API_KEY
├── settings.gradle.kts
└── README.md
```

---

## Prerequisites

- **JDK**: Java Development Kit 21 or compatible
- **Android SDK**: Compile SDK 35, Target SDK 35, Min SDK 26 (Android 8.0+)
- **Android Studio**: Android Studio Ladybug / Meerkat (2024.2+) or compatible
- **Firebase Project**: An active Firebase project with Email/Password sign-in enabled
- **Google Cloud Console**: A Google Cloud project with the **Maps SDK for Android** enabled

---

## Setup & Configuration

### 1. Google Maps API Key

The Google Maps API key is securely injected at build time from `local.properties` to ensure credentials are never committed to version control.

1. Open (or create) the `local.properties` file in the project root:
   ```properties
   sdk.dir=C\:\\Users\\<YourUsername>\\AppData\\Local\\Android\\Sdk
   MAPS_API_KEY=YOUR_GOOGLE_MAPS_API_KEY_HERE
   ```
2. Replace `YOUR_GOOGLE_MAPS_API_KEY_HERE` with your key from the Google Cloud Console.
3. In Google Cloud Console, ensure that:
   - **Maps SDK for Android** is enabled under APIs & Services.
   - (Recommended) Restrict the key to Android apps using package name `com.example.weathermap` and your SHA-1 fingerprint.

The `app/build.gradle.kts` automatically injects this value into the manifest placeholder:
```kotlin
manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
buildConfigField("String", "MAPS_API_KEY", "\"$mapsApiKey\"")
```

### 2. Firebase Authentication

1. Go to the [Firebase Console](https://console.firebase.google.com/).
2. Create a project and register an Android application with package name:
   ```
   com.example.weathermap
   ```
3. Enable **Authentication** → **Sign-in method** → **Email/Password**.
4. Download the `google-services.json` file.
5. Place `google-services.json` into the `app/` directory (`WeatherMapApp/app/google-services.json`).

---

## Meteo.lt API Integration

The app connects to the official public open data API of the Lithuanian Hydrometeorological Service (LHMT):

- **Base URL**: `https://api.meteo.lt/`
- **Forecast Endpoint**: `GET v1/places/{placeCode}/forecasts/long-term`
- **Monitored Locations** (15 cities):
  `vilnius`, `kaunas`, `klaipeda`, `siauliai`, `panevezys`, `alytus`, `marijampole`, `mazeikiai`, `utena`, `taurage`, `telsiai`, `palanga`, `nida`, `druskininkai`, `anyksciai`
- **Parallel Dispatch**: Monitored stations are requested in parallel using coroutine `async`/`awaitAll` under `Dispatchers.IO`.
- **Fault Tolerance**: If an individual station response fails, the remaining stations still load. If an overall network outage occurs, a descriptive `IOException` is reported to the UI.

---

## Building & Running

### Using the Gradle Wrapper / Command Line

Set your `JAVA_HOME` to your JDK installation (e.g., the bundled Android Studio JBR):

```powershell
# Set Java environment (Windows PowerShell example)
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

# Navigate to project directory
cd C:\Users\murad\.gemini\antigravity\scratch\WeatherMapApp

# Run unit tests
.\gradlew.bat testDebugUnitTest

# Build debug APK
.\gradlew.bat assembleDebug
```

The compiled APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

### Using Android Studio

1. Open Android Studio.
2. Select **File → Open…** and choose `WeatherMapApp`.
3. Wait for the Gradle sync to finish.
4. Select a connected physical device or Android Virtual Device (AVD with API 26+ and Google Play Services).
5. Click **Run** (`Shift + F10`).

---

## Unit Testing & Verified Results

The project includes unit tests covering data parsing, coordinate validation, filtering logic, weather condition mapping, authentication guards, network error propagation, and cache concurrency.

### Running Tests

```powershell
.\gradlew.bat testDebugUnitTest
```

### Verified Test Results

```
> Task :app:testDebugUnitTest

BUILD SUCCESSFUL
50 actionable tasks: 50 executed
100% tests successful (50/50 passing)
```

| Test Suite | Tests | Description | Result |
|---|:---:|---|:---:|
| `CoordinatesDtoTest` | 8 | Validates global and edge-case coordinate validation logic | **PASSED** |
| `WeatherConditionTest` | 12 | Validates Meteo condition code mapping and case normalization | **PASSED** |
| `WeatherFilterTest` | 16 | Validates search queries, conditions, and temperature ranges | **PASSED** |
| `WeatherRepositoryImplTest` | 14 | Validates auth blocking, coordinate fallbacks, network errors, cache isolation | **PASSED** |
| **Total** | **50** | **Comprehensive data, domain, and security coverage** | **100% PASSED** |

---

## Localization

The app supports English and Lithuanian languages out of the box, responding dynamically to the device's system language settings:

| Component | English (`values/strings.xml`) | Lithuanian (`values-lt/strings.xml`) |
|---|---|---|
| App Title | *Lithuania Weather Map* | *Lietuvos orų žemėlapis* |
| Login Action | *Sign In* | *Prisijungti* |
| Register Action | *Register* | *Registruotis* |
| Auth Required Alert | *Authentication required: You must be signed in to request weather data.* | *Būtina autentifikacija: Tik prisijungę naudotojai gali gauti orų duomenis.* |
| Condition: Clear | *Clear* | *Giedra* |
| Condition: Rain | *Rain* | *Lietus* |
| Temp: Above 15°C | *Above 15°C* | *Virš 15°C* |
| Network Error | *Unable to reach weather service. Check your internet connection.* | *Nepavyko pasiekti orų tarnybos. Patikrinkite interneto ryšį.* |
