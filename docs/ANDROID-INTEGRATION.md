# ThinkTank Academia - Native Android Architecture & Integration Guide

This document outlines the Native Android application architecture, detailing how it conforms to design guidelines, interacts with the backend APIs, handles local state persistence/caching, provides automatic session expiration recovery (401 interception), supports offline fallback states, and remains 100% buildable out-of-the-box.

---

## 🏗️ 1. Architectural Blueprint (Clean MVVM)

The app follows standard clean architecture patterns, keeping core logical frameworks separated from Material Design 3 UI states:

```
com.example
│
├── MainActivity.kt               # Root entrance, dynamic routing based on datastore token
│
├── core/
│   ├── database/                 # Room Database persistence for offline-first support
│   │   ├── ThinkTankDatabase.kt  # DB instance configuration
│   │   ├── CourseDao.kt          # DAO queries for local Course caches
│   │   └── ExploreDao.kt         # DAO queries for Article, Book, and Knowledge materials
│   │
│   ├── datastore/                # Encrypted key-value store for login state
│   │   └── SessionManager.kt     # Jetpack DataStore preferences (tokens, user details)
│   │
│   ├── navigation/               # Type-safe Jetpack Navigation compose keys
│   │   └── Screen.kt             # Navigation screens and route parameters
│   │
│   ├── network/                  # Retrofit networking client
│   │   ├── ApiService.kt         # Mapped Endpoints (1-to-1 with API.md specs)
│   │   ├── NetworkModels.kt      # Strong type deserialization models (Moshi DTOs)
│   │   └── RetrofitClient.kt     # OkHttp config, token header injector, & 401 interceptor
│   │
│   ├── repository/               # Data-access layer orchestrator
│   │   └── ThinkTankRepository.kt# Merges Local Room DB cache + Remote Retrofit REST calls
│   │
│   └── ui/
│       └── ViewModelFactory.kt   # Manual injection factories for ViewModels
│
└── feature/                      # Modular, high-fidelity UI modules (Material 3)
    ├── auth/                     # Welcome screen, Login, Register, Forgot Password
    ├── home/                     # Academic Dashboard with stats, recent feed, and status cards
    ├── courses/                  # Catalog, category sorting (World, Humanity, Society), details
    ├── lesson/                   # Interactive reading sheet with scholar citations and supplementary PDFs
    ├── quiz/                     # Quiz timer, radio card select, and grade sheets
    ├── explore/                  # Multi-tab feeds for Articles, Books, and Knowledge
    ├── profile/                  # Account credentials updating and notification logs
    └── main/                     # Bottom nav, Navigation rails (responsive support)
```

---

## 🔒 2. Session Integrity & 401 Interception

*   **Secure Tokens**: Avoids raw/plain file saving. Student credentials and active JWT bearer tokens are managed securely in Jetpack DataStore preferences (`SessionManager.kt`).
*   **Automatic 401 Handling**: Registered an custom interceptor in `RetrofitClient.kt`. If the backend API responds with an `HTTP 401 Unauthorized` (e.g., token expired or revoked), the interceptor automatically executes a transactional clear sequence:
    ```kotlin
    if (response.code == 401) {
        runBlocking {
            sessionManager.clearSession()
        }
    }
    ```
    Since `MainActivity` observes the `SessionManager.authToken` StateFlow in real-time, the interface instantly shifts the view stack from `MainScreen` back to the authentication entrance (`WelcomeScreen`) smoothly without application crashes.

---

## 💾 3. Intelligent Room Offline-First Cache

The app implements a **Jetpack Room SQLite cache** to support zero-latency rendering and full offline study access:
*   **Zero-State Hydration**: On first launch, the local Room tables are automatically populated with rich initial academic coursewares, enabling study even with no initial cellular signal.
*   **Fetch-and-Sync Lifecycle**: When fetching course lists or explore articles, the repository executes a concurrent network dispatch. If successful, it writes the newest contents to the database, ensuring local data matches upstream.
*   **Seamless Fallback**: If network dispatches fail (e.g., plane mode, poor reception), the system automatically emits the local database caches and surfaces a subtle offline notification indicator to keep the experience polished.

---

## 🌍 4. Standard English & Bangla Localization

The project incorporates native Android localizations:
*   **Default Context**: English strings found in `app/src/main/res/values/strings.xml`.
*   **Bangla Context**: Complete translations found in `app/src/main/res/values-bn/strings.xml`.
*   **Locale Independence**: High-fidelity composables reference resource strings dynamically using `stringResource(R.string.some_label)`, preserving instant adaptation to OS and profile level locale switches.

---

## 📱 5. Responsive Form Factors & RTL

*   **Foldable & Tablet Ready**: Leverages Material 3 container dimensions. Screen boundaries maintain structured margin scaling (`Modifier.widthIn(max = 600.dp).align(Alignment.CenterHorizontally)`) to prevent excessive stretching.
*   **Navigation Rails**: Adaptive navigation elements scale cleanly, transitioning from bottom bars on vertical handsets to sidebar rails on wider tablets.
*   **Accessibility Standards**: Screen reader descriptions are maintained via robust `contentDescription` properties, and all interactive buttons implement a physical feedback ripple with touch areas exceeding `48dp x 48dp`.

---

## 🔗 6. Custom Deep Link Integration

We have mapped standard Android Intent Filters under `MainActivity.kt` inside `AndroidManifest.xml` to handle routing from external sources:
*   **Schemes**: `http://`, `https://`, and custom `thinktank://` protocol.
*   **Host Path Mapping**:
    *   `https://thinktank-academia.example.com/courses`
    *   `thinktank://courses`

When clicked, the OS launches or redirects the student directly to `MainActivity` where the navigation engine parses the URI arguments to open the specific Course detail sheet.

---

## 🚀 7. Compilation & CI Pipeline Configurations

The project contains a pre-built Gradle configuration and is fully buildable on your local developer workstation.

### Local Development Compilation

To compile a debug build of the application locally:
```bash
# Verify Gradle executes and build a debug APK
./gradlew assembleDebug
```
The output debug APK will be created at: `app/build/outputs/apk/debug/app-debug.apk`.

To compile an Android App Bundle (AAB) for release:
```bash
./gradlew bundleRelease
```
The output release AAB will be created at: `app/build/outputs/bundle/release/app-release.aab`.

### ⚡ Automated GitHub Actions (CI)

We have configured a comprehensive GitHub Actions workflow at `.github/workflows/android-build.yml` which automatically executes on push to your repository:
1. Installs a secure **JDK 17** environment.
2. Caches Gradle caches to ensure fast execution.
3. Automatically triggers `./gradlew assembleDebug` to build the compiled test APK.
4. Generates a release bundle using `./gradlew bundleRelease`.
5. Uploads the final built `.apk` and `.aab` packages as **Artifacts** right on the workflow run page, letting you download and side-load the actual build on physical Android test devices.
