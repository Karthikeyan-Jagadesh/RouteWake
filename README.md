# RouteWake — Premium Location-Based Travel Alarm Mobile Application

[![Platform](https://img.shields.io/badge/Platform-Android%20%7C%20Spring%20Boot-brightgreen.svg)]()
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.0-blue.svg)]()
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-blueviolet.svg)]()
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-green.svg)]()
[![Oracle Database](https://img.shields.io/badge/Database-Oracle%20XE-red.svg)]()

RouteWake is a high-end, production-grade native Android mobile application and synchronized Spring Boot backend that solves a common commuter problem: **falling asleep or losing track of time and missing your transit or highway stop**.

---

## 🌟 Architecture & Highlights

```
RouteWake System Architecture
 ┌──────────────────────────────────────────────────────────────┐
 │                  Android Client (Kotlin)                     │
 │  ┌────────────────────────────────────────────────────────┐  │
 │  │        Jetpack Compose UI (Material 3 Dark/Light)      │  │
 │  │  Splash • Onboarding • Auth • Home • Search • Config  │  │
 │  │  Active Navigation • Alarm Dialog • History • Settings │  │
 │  └───────────────────────────┬────────────────────────────┘  │
 │                              ▼                               │
 │  ┌────────────────────────────────────────────────────────┐  │
 │  │     Domain Engine (Haversine, Hysteresis, Deduplication)│  │
 │  └───────────────────────────┬────────────────────────────┘  │
 │                              ▼                               │
 │  ┌────────────────────────────────────────────────────────┐  │
 │  │      Foreground Service & Hardware Trigger System       │  │
 │  │  • FusedLocationProviderClient + LocationManager       │  │
 │  │  • Actionable Foreground Notifications                 │  │
 │  │  • AlarmAudioPlayer (RingtoneManager + Vibrator)        │  │
 │  └───────────────────────────┬────────────────────────────┘  │
 │                              ▼                               │
 │  ┌────────────────────────────────────────────────────────┐  │
 │  │    Data Layer (EncryptedSharedPreferences + Retrofit)  │  │
 │  └───────────────────────────┬────────────────────────────┘  │
 └──────────────────────────────┼───────────────────────────────┘
                                │ HTTPS / REST (JSESSIONID)
                                ▼
 ┌──────────────────────────────────────────────────────────────┐
 │                Spring Boot 3 REST Backend                    │
 │  • AuthController: /api/auth/register, /login, /logout, /me  │
 │  • TripController: /api/trips (POST, GET, DELETE)            │
 │  • Oracle XE Database (app_users, trip_alerts, saved_places) │
 └──────────────────────────────────────────────────────────────┘
```

---

## 📱 Mobile Application Features

1. **Native Jetpack Compose UI**:
   - Custom **Midnight Navy (`#070B12`)**, **Surface Slate (`#0F172A`)**, and **Electric Mint (`#10B981`)** design system.
   - Comprehensive support for Dark and Light themes with fluid transitions and haptic feedback.
   - Responsive and accessible layouts without cluttered cards or generic templates.

2. **9 Core Production Screens**:
   - **Splash & Onboarding**: Animated RouteWake radar pulse and contextual explanation of location & notification permissions.
   - **Authentication**: Seamless Sign In, Registration, validation, password toggling, and secure session management.
   - **Home Dashboard**: Quick trip actions, interactive map preview, GPS sensor readiness, active trip banner, and recent destinations.
   - **Destination Search**: Live Nominatim geocoding with 500ms debouncing, suggestions, interactive map pin drop, and address validation.
   - **Trip Configuration**: Dual-mode alerting:
     - **Distance Mode**: 500m, 1km, 2km, or custom threshold slider.
     - **ETA Mode**: 5, 10, 15, or 20 minutes lead-time with freshness indicators and explicit fallback disclaimers.
   - **Active Navigation Screen**: Live map tracking, route polylines, real-time distance remaining, ETA confidence, GPS accuracy badge, and pause/resume/end trip controls.
   - **High-Priority Alarm Engine**:
     - Android Notification Channel with `IMPORTANCE_HIGH`.
     - System Alarm Audio Stream (`RingtoneManager.TYPE_ALARM`) with loop playback.
     - Multi-pattern hardware vibration.
     - Interactive **Stop Alarm** and **Snooze** actions.
     - Strict deduplication preventing multi-trigger race conditions.
   - **Trip History**: Chronological view of past trips fetched from the Oracle DB, with status badges and deletion support.
   - **Saved Places**: Local encrypted persistence for Home, Work, and favorite transit hubs.
   - **Settings & Profile**: Custom server endpoint configuration, default alert preferences, appearance toggling, permission management, and secure logout.

3. **Robust Location Monitoring Engine**:
   - **Haversine Distance**: $R = 6,371,000$ meters exact calculation.
   - **Accuracy Filtering**: Automatically discards GPS readings with horizontal accuracy $> 50$ meters.
   - **Hysteresis**: Requires $N \ge 2$ consecutive qualifying readings inside the threshold radius to prevent GPS jitter false-positives.
   - **Foreground Service**: Continuous navigation monitoring in background with persistent notification and graceful recovery.

---

## 🛠️ Project Directory Structure

```
Location_Alarm_Tracker/
├── android/                             # Native Android Application (Kotlin + Compose)
│   ├── app/
│   │   ├── src/main/java/com/routewake/app/
│   │   │   ├── domain/                  # Pure Kotlin Domain Layer
│   │   │   │   ├── engine/              # HaversineCalculator, ArrivalDetector, EtaEstimator
│   │   │   │   └── model/               # LocationPoint, TripAlert, MonitoringState, SavedPlace
│   │   │   ├── data/                    # Data Layer
│   │   │   │   ├── local/               # SessionManager (EncryptedSharedPreferences)
│   │   │   │   ├── remote/              # RetrofitClient, RouteWakeApi, NominatimApi, OsrmApi
│   │   │   │   └── repository/          # AuthRepository, TripRepository, SavedPlacesRepository
│   │   │   ├── service/                 # LocationForegroundService, AlarmAudioPlayer, NotificationHelper
│   │   │   ├── theme/                   # Color, Theme, Type tokens
│   │   │   ├── ui/                      # Jetpack Compose Screens & Components
│   │   │   │   ├── splash/, onboarding/, auth/, home/, destination/, tripsetup/, navigation/, history/, savedplaces/, settings/
│   │   │   │   └── components/          # Reusable RouteWake cards, buttons, badges, NativeMapView
│   │   │   └── MainActivity.kt          # Navigation host & runtime permission handler
│   │   └── src/test/java/               # Unit Test Suite (100% Passing)
│   └── build/outputs/apk/debug/app-debug.apk # Generated Debug APK
│
└── backend/                             # Spring Boot 3 + Oracle XE Backend
    ├── src/main/java/com/locationalarm/
    │   ├── config/                      # Oracle JDBC DataSource
    │   ├── controller/                  # AuthController, TripController, PageController
    │   ├── service/                     # AuthService, TripService
    │   └── repository/                  # UserRepository, TripAlertRepository
    ├── src/main/resources/
    │   ├── application.properties       # Server port 8081 & Oracle DB credentials
    │   └── schema.sql                   # Oracle DDL for app_users, trip_alerts, saved_places
    ├── pom.xml                          # Maven 3 configuration
    └── run-backend.bat                  # One-click launcher script
```

---

## 🚀 Getting Started

### 1. Start the Spring Boot Backend

#### Prerequisites:
- Java 17 or Java 21
- Oracle Database (XE, 19c, 21c, or 23ai)

#### Setup Database:
1. Run `backend/src/main/resources/schema.sql` in Oracle SQL Developer or SQL*Plus:
   ```sql
   @backend/src/main/resources/schema.sql
   ```
2. Verify credentials in `backend/src/main/resources/application.properties`:
   ```properties
   server.port=8081
   app.db.url=jdbc:oracle:thin:@localhost:1521:xe
   app.db.username=karthi2
   app.db.password=password1
   ```

#### Launch Backend:
- **Windows (One-Click):**
  Double click `backend/run-backend.bat`
- **Or via Terminal:**
  ```bash
  cd backend
  mvn spring-boot:run
  ```
The backend starts on `http://localhost:8081`. Both the Thymeleaf web application and mobile REST APIs (`/api/auth/*`, `/api/trips/*`) will be live.

---

### 2. Install and Run the Android App

#### Pre-built Debug APK:
The build has generated the debug APK ready to install:
```
android/app/build/outputs/apk/debug/app-debug.apk
```

#### Install on a Physical Device:
1. Enable **Developer Options** and **USB Debugging** on your Android phone.
2. Connect your phone via USB cable and run:
   ```bash
   adb install -r android/app/build/outputs/apk/debug/app-debug.apk
   ```
3. Alternatively, transfer `app-debug.apk` directly to your phone via Google Drive, WhatsApp, or USB storage and open it in Files to install.

#### Connecting Mobile App to Backend:
- **Android Emulator**: In the RouteWake App, go to **Settings** -> **Server Endpoint URL** -> set to `http://10.0.2.2:8081/`. (This is default).
- **Physical Device**: Ensure your phone and PC are connected to the same Wi-Fi network. Find your PC's local IP address (`ipconfig`, e.g. `192.168.1.15`), open **Settings** in the RouteWake app, and set the endpoint to:
  ```
  http://192.168.1.15:8081/
  ```

---

## 🧪 Testing & Verification

### Running Automated Unit Tests
To execute the comprehensive test suite for mathematical accuracy, hysteresis filtering, and session state machines:
```bash
cd android
.\gradlew.bat testDebugUnitTest
```
Tests included:
- `HaversineCalculatorTest`: Validates distance between coordinates with exact tolerances ($R = 6,371,000$ m) and rejects out-of-range coordinates.
- `ArrivalDetectorTest`: Validates horizontal accuracy cutoff ($>50$m), hysteresis ($N \ge 2$ consecutive qualifying samples), and alert deduplication.
- `EtaEstimatorTest`: Tests lead-time alert triggering and fallback speed calculations.
- `TripMonitoringSessionTest`: Tests state machine transitions (`IDLE` -> `PREPARING` -> `MONITORING` -> `PAUSED` -> `ARRIVED`).

### Rebuilding Debug APK
```bash
cd android
.\gradlew.bat assembleDebug
```
Output will be placed in `android/app/build/outputs/apk/debug/app-debug.apk`.
