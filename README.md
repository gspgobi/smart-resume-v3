# Smart Resume Builder

> A production Android app that lets users build, customise, and export professional PDF resumes — live on the Play Store with real users.

[![Platform](https://img.shields.io/badge/platform-Android-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Min SDK](https://img.shields.io/badge/minSdk-24-brightgreen)](https://apilevels.com)
[![Play Store](https://img.shields.io/badge/Play%20Store-live-green?logo=googleplay&logoColor=white)](https://play.google.com/store/apps/details?id=com.nithra.nithraresume)
[![License](https://img.shields.io/badge/license-Source%20Available-blue)](LICENSE)

---

## About

**[View on Google Play →](https://play.google.com/store/apps/details?id=com.nithra.nithraresume)**

Smart Resume Builder (V3) is a complete, ground-up rewrite of an existing Play Store app — migrating from a legacy XML/View-based architecture to **100% Kotlin + Jetpack Compose** while preserving full data continuity for users upgrading from V2.

Users can create multiple resume profiles, populate them with structured sections (contact info, work history, education, declarations, and more), pick from 6 PDF layout formats, and export or share the generated PDF directly from the app.

**What makes this project interesting from an engineering perspective:**
- The V2 → V3 migration was seamless for users: same database file, no data loss, no re-entry required
- PDF generation is done entirely on-device using a custom layout engine built on iTextPDF
- The section type system supports 8 distinct data shapes through a single, unified UI pattern
- All state management is done through unidirectional data flow with Kotlin Coroutines + Flow

---

## Screenshots

> _Add screenshots here_

---

## Features

- **Multiple profiles** — create and manage up to 20 independent resume profiles
- **8 section types** — contact info, work experience, education, declaration + signature, paragraph text, split text, multi-item text, cover letter
- **6 PDF formats** — Functional, Harvard, Classic, Modern, Simple, Grayscale
- **Per-profile customisation** — font (8 options), font size, background colour, date format (12 options), bullet style
- **On-device PDF export** — generated entirely without a server; view, share, or rename from within the app
- **Sample resumes** — browse and import pre-filled resume profiles
- **Drag-and-drop reordering** — reorder sections and list entries within sections
- **Signature capture** — draw a signature directly on-screen and embed it in the PDF
- **Dark / Light / System theme**
- **Push notifications** via Firebase Cloud Messaging

---

## Architecture

```
MVVM + Clean Architecture
```

```
UI Layer          Compose Screens + ViewModels (StateFlow / derivedStateOf)
                          ↑
Domain Layer      Repositories (single source of truth)
                          ↑
Data Layer        Room DAOs  ·  Retrofit  ·  DataStore
```

All screens receive only a `NavController` and obtain their ViewModel via `hiltViewModel()`. Arguments travel through Navigation Compose routes, never between composables directly.

---

## Tech Stack

| Layer | Technology |
|---|---|
| UI | Jetpack Compose + Material 3 |
| Navigation | Navigation Compose |
| Dependency Injection | Hilt |
| Database | Room (SQLite) |
| Async | Kotlin Coroutines + Flow |
| Preferences | DataStore (with SharedPreferences migration) |
| Networking | Retrofit + OkHttp |
| Image loading | Coil |
| Image cropping | Android Image Cropper |
| PDF generation | iTextPDF 5.4.0 (on-device) |
| Push notifications | Firebase Cloud Messaging |
| Analytics / Crash | Firebase Analytics + Crashlytics |
| Ads | AdMob (banner + interstitial) |
| In-app review | Play In-App Review |
| In-app update | Play In-App Update |

---

## Engineering Highlights

### Zero-downtime V2 → V3 database migration
The app opens the same SQLite database file as V2 (`smart_resume_v2.db`). A single Room migration (v1 → v2) rewrites all 15 tables using the rename-create-copy-drop pattern to fix `NOT NULL` and `BOOLEAN → INTEGER` type mismatches — so existing users upgrade without any data loss or re-entry.

### On-device PDF generation
`ResumePdfBuilder.kt` drives a custom layout engine on top of iTextPDF. It receives a `ResumePdfData` aggregate (all section data + format settings) and produces a pixel-precise PDF entirely on-device, with no server round-trip. Each of the 6 resume formats has distinct layout logic (column widths, header styles, divider rules, colour schemes).

### Flexible section type system
Eight section types share a common `SectionHeadAdded` parent but diverge into two DAO families:
- **Single-row** (types 1, 4, 5, 8) — one record per section head, edited in place
- **List-based** (types 2, 3, 6, 7) — multiple records with `index_position` ordering and sub-edit screens

A single `SectionChildRepository` wraps both DAO families and exposes an atomic `deleteAllChildrenForHead()` via `db.withTransaction {}`.

### Dirty-state tracking without a separate "original" copy
Form dirty state in each ViewModel is tracked by comparing current field values against an `originalFormState: StateFlow<…>` snapshot captured at load time. The screen derives `isDirty` as a `remember { derivedStateOf { … } }`, so recomposition is skipped when unrelated state changes.

### File migration on first post-upgrade launch
On first launch after upgrading from V2, the app migrates photo, signature, and PDF files from shared external storage (`Nithra/SmartResume/…`) to app-scoped storage — updating all database paths atomically and showing a progress dialog. This required handling Android 13+ (API 33) photo permission changes with a conditional permission request path.

---

## Package Structure

```
com.nithra.nithraresume/
├── data/
│   ├── api/              Retrofit ApiService + ApiRepository
│   ├── db/
│   │   ├── dao/          9 DAO interfaces
│   │   ├── entity/       15 Room entities
│   │   └── SmartResumeDatabase.kt
│   ├── model/            Domain models + entity↔model mappers
│   └── repository/       5 repositories
├── di/                   Hilt modules (Database, Network, Repository)
├── pdf/                  ResumePdfBuilder (iTextPDF, 6 format engines)
├── service/              FCM messaging service
├── ui/
│   ├── common/           Shared composables
│   ├── format/           Resume format / font / colour picker
│   ├── generate/         PDF generation screen
│   ├── main/             Main screen + navigation drawer
│   ├── navigation/       NavGraph + Screen sealed class
│   ├── notification/     Notification list + detail
│   ├── profile/          Profile list / create / rename / delete
│   ├── sample/           Sample resume browser
│   ├── section/
│   │   ├── child/        SectionChild1–8 screens + sub-edit screens
│   │   └── head/         Section list (add / remove / reorder)
│   ├── settings/         App settings (theme, notifications)
│   ├── splash/           Splash + V2 file migration
│   ├── theme/            Color, Typography, Theme
│   └── viewshare/        View & share generated PDF
└── utils/                Constants, DateTimeUtils, PrefsManager,
                          AdMobManager, SharedAdViewModel
```

---

## Build & Run

### Prerequisites

- Android Studio Hedgehog or later
- JDK 11
- API 24+ device or emulator

### Setup

1. **Firebase (debug):**
   Copy the example config and fill in your Firebase project values:
   ```bash
   cp app/src/debug/google-services.json.example app/src/debug/google-services.json
   ```
   Create a Firebase project, register the app with package `com.nithra.nithraresume`, and download the real `google-services.json`.

2. **Signing (release only):**
   Create `keystore.properties` at the project root:
   ```properties
   storeFile=path/to/your.jks
   storePassword=your-store-password
   keyAlias=your-key-alias
   keyPassword=your-key-password
   ```

3. Open the project in Android Studio and sync Gradle.

### Build Commands

```bash
# Debug (TestAdMob flavor — uses test ad unit IDs)
./gradlew assembleTestAdMobDebug

# Release builds
./gradlew assembleProdAdMobRelease   # Production ads
./gradlew assembleNoAdMobRelease     # No ads

# All variants
./gradlew assemble

# Install debug on connected device
./gradlew installTestAdMobDebug

# Tests
./gradlew test                        # Unit tests
./gradlew connectedAndroidTest        # Instrumented tests

# Clean
./gradlew clean
```

**APK output:** `smart-resume-{flavor}-{buildType}-{versionCode}-{versionName}.apk`

### Product Flavors

| Flavor | AdMob | Test IDs | Use Case |
|---|---|---|---|
| `TestAdMob` (default) | ✓ | ✓ | Development |
| `ProdAdMob` | ✓ | ✗ | Play Store release |
| `NoAdMob` | ✗ | — | Ad-free build |

---

## Key Constraints

| Item | Value |
|---|---|
| `minSdk` | 24 (Android 7.0) |
| `targetSdk` / `compileSdk` | 37 |
| Language | Kotlin only |
| Max profiles | 20 |
| Max sections per profile | 20 |
| Max list items per section | 10 |
