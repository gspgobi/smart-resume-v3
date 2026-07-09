# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Debug build (TestAdMob flavor, default)
./gradlew assembleTestAdMobDebug

# Release build
./gradlew assembleTestAdMobRelease
./gradlew assembleProdAdMobRelease
./gradlew assembleNoAdMobRelease

# All variants
./gradlew assemble

# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Run a single test class
./gradlew testTestAdMobDebugUnitTest --tests "com.nithra.nithraresume.ExampleUnitTest"

# Clean
./gradlew clean
```

APK output naming convention: `smart-resume-{flavor}-{buildType}-{versionCode}-{versionName}.apk`

## Product Flavors

| Flavor | AdMob enabled | Test IDs |
|---|---|---|
| `TestAdMob` (default) | yes | yes |
| `ProdAdMob` | yes | no |
| `NoAdMob` | no | — |

`BuildConfig.isAdMobEnable` and `BuildConfig.isTestAdMobId` control ad behaviour at runtime.

## Architecture

MVVM + Clean Architecture with Hilt DI, Room database, Coroutines/Flow, and Jetpack Compose.

**Data flow:** `Room DAOs` → `Repositories` → `ViewModels` (StateFlow/Flow) → `Compose Screens`

All screens receive only a `NavController`; they obtain their ViewModel via `hiltViewModel()`. Arguments are passed through Navigation Compose routes, never directly between composables.

## Section Type System

Sections are the core data model. Each section added to a resume (`SectionHeadAdded`) has a `section_head_base_id` (1–8) that determines which child table and screen it maps to:

| ID | Type | Child table | Screen |
|---|---|---|---|
| 1 | Contact Information | `section_child_1` | SectionChild1Screen |
| 2 | Work Experience | `section_child_2` | SectionChild2Screen + Sub |
| 3 | Education | `section_child_3` | SectionChild3Screen + Sub |
| 4 | Declaration + Signature | `section_child_4` | SectionChild4Screen + SignatureScreen |
| 5 | Paragraph / Bulleted Text | `section_child_5` | SectionChild5Screen |
| 6 | Split Text | `section_child_6` | SectionChild6Screen + Sub |
| 7 | Multiple Item Text | `section_child_7` | SectionChild7Screen + Sub |
| 8 | Cover Letter (Add-on) | `section_child_8` | SectionChild8Screen |

Child types 1, 4, 5, 8 are **single-row** (one record per section head); types 2, 3, 6, 7 are **list-based** (multiple records, have index_position for reordering and Sub-screens for add/edit).

## Navigation

All routes are defined as `sealed class Screen` in `ui/navigation/Screen.kt`. The `NavGraph.kt` wires them. Use `Screen.Foo.createRoute(args)` to navigate — never construct route strings manually.

Key route patterns:
- Profile-scoped screens receive `profileId: Int`
- Section editing screens receive `sectionHeadAddedId: Int`
- Sub-edit screens also receive `itemId: Int` (value `-1` means "new item")

## Database

- **File:** `smart_resume_v2.db` — identical name to V2 to preserve existing user data
- **Current version:** 2
- **Migration 1→2** (`Migrations.kt`): rewrites all 15 tables using rename-create-copy-drop to fix PK `NOT NULL` and `BOOLEAN`→`INTEGER` type mismatches between the V2 SQLiteOpenHelper schema and Room's expected schema. New V3 installs go through `seedCallback` and start at version 2.
- **Two DAO interfaces** handle all child data: `SectionChildSingleDao` (child types 1, 4, 5, 8) and `SectionChildListDao` (child types 2, 3, 6, 7). Both are injected into the single `SectionChildRepository`.

## Key Constraints

- `MAX_PROFILES = 20`, `MAX_SECTIONS = 20`, `MAX_CHILD_ITEMS = 10`
- `minSdk = 24`, `targetSdk / compileSdk = 37`
- Kotlin only — no Java sources
- Font asset filenames in `assets/fonts/` are used as the font identifier throughout the DB and PDF layer (e.g. `"TIMES NEW ROMAN.TTF"`). Constants live in `utils/Constants.kt`.

## PDF Generation

`pdf/ResumePdfBuilder.kt` uses iTextPDF 5.4.0. It receives a `ResumePdfData` aggregate (all section data + format settings) and writes a PDF to `getExternalFilesDir(null)/GeneratedResume/`. The 6 resume formats (Functional, Harvard, Classic, Modern, Simple, Grayscale) each have distinct layout logic inside `ResumePdfBuilder`.

## Hilt DI Modules

| Module | Provides |
|---|---|
| `DatabaseModule` | `SmartResumeDatabase`, all 9 DAOs, `PrefsManager`, `AnalyticsManager` |
| `NetworkModule` | `Retrofit`, `OkHttpClient`, `ApiService` |
| `RepositoryModule` | All 5 repositories |

All components are `@Singleton` scoped at `SingletonComponent`.

## Setup Requirements

- Place `google-services.json` in `app/` before building (Firebase; not committed to VCS)
- Release signing is read from `keystore.properties` at the project root (not committed)
- Requires Android Studio Hedgehog or later; API 24+ device or emulator
