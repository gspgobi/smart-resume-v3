# Smart Resume V3

A complete rewrite of Smart Resume V2 in **Kotlin + Jetpack Compose**, preserving full data continuity for existing users upgrading from V2.

---

## Architecture

```
MVVM + Clean Architecture + Hilt + Room + Coroutines/Flow + Jetpack Compose + Navigation Compose
```

## Tech Stack

| Layer | Technology |
|-------|-----------|
| UI | Jetpack Compose + Material 3 |
| Navigation | Navigation Compose |
| DI | Hilt |
| Database | Room (SQLite) |
| Async | Coroutines + Flow |
| Preferences | DataStore |
| Networking | Retrofit + OkHttp |
| Image loading | Coil |
| PDF generation | iTextPDF 5.4.0 |
| Push notifications | Firebase Cloud Messaging |
| Analytics / Crash | Firebase Analytics + Crashlytics |
| Ads | AdMob |

---

## Package Structure

```
com.nithra.nithraresume/
├── data/
│   ├── api/                  Retrofit ApiService + ApiRepository
│   ├── db/
│   │   ├── dao/              9 @Dao interfaces
│   │   ├── entity/           15 @Entity data classes
│   │   └── SmartResumeDatabase.kt
│   ├── model/                Domain models + entity↔model mappers
│   └── repository/           5 repositories
├── di/                       Hilt modules (DB, Network, Repository)
├── pdf/                      ResumePdfBuilder.kt (iTextPDF, 6 formats)
├── service/                  SmartResumeMessagingService (FCM)
├── ui/
│   ├── common/               Shared composables (BulletTypeDropdown,
│   │                         DateFormatPickerDialog, ObjAccompBottomSheet,
│   │                         FeedbackDialog)
│   ├── format/               Resume format / font / colour picker
│   ├── generate/             PDF generation screen
│   ├── main/                 Main screen + navigation drawer
│   ├── navigation/           NavGraph + Screen sealed class
│   ├── notification/         Notification list + detail
│   ├── profile/              User profile list / add / delete
│   ├── sample/               Sample resumes browser
│   ├── section/
│   │   ├── child/            SectionChild1–8 screens + sub-edit screens
│   │   └── head/             Section head list (add / remove / reorder)
│   ├── settings/             App settings
│   ├── splash/               Splash screen + ViewModel
│   ├── theme/                Color, Typography, Theme
│   └── viewshare/            View & share generated PDF
└── utils/                    Constants, DateTimeUtils, PrefsManager,
                              AdMobManager, AnalyticsManager
```

---

## Database

- **DB name:** `smart_resume_v2.db` (identical to V2 — existing user data survives upgrade)
- **DB version:** 2
- **Migration 1→2:** Rewrites all 15 tables to fix PK `NOT NULL` and `BOOLEAN`→`INTEGER` type constraints
- **Tables:** 15 (resume formats, section heads, 8 section child types, user profiles, FCM data)
- **DAOs:** 9 (split into `SectionChildSingleDao` for types 1,4,5,8 and `SectionChildListDao` for types 2,3,6,7)
- **Seed data:** 6 resume formats, 2 section groups, 19 section templates

---

## Section Type System

Each resume section (`SectionHeadAdded`) has a `section_head_base_id` (1–8) that determines its data table and edit screen:

| ID | Type | Child Table | Screen |
|---|---|---|---|
| 1 | Contact Information | `section_child_1` | SectionChild1Screen |
| 2 | Work Experience | `section_child_2` | SectionChild2Screen + Sub |
| 3 | Education | `section_child_3` | SectionChild3Screen + Sub |
| 4 | Declaration + Signature | `section_child_4` | SectionChild4Screen + SignatureScreen |
| 5 | Paragraph / Bulleted Text | `section_child_5` | SectionChild5Screen |
| 6 | Split Text | `section_child_6` | SectionChild6Screen + Sub |
| 7 | Multiple Item Text | `section_child_7` | SectionChild7Screen + Sub |
| 8 | Cover Letter (Add-on) | `section_child_8` | SectionChild8Screen |

Types 1, 4, 5, 8 are **single-row** (one record per section); types 2, 3, 6, 7 are **list-based** (multiple records with reordering and sub-edit screens).

---

## Resume Formats

| Format | Style |
|--------|-------|
| Functional | Skills / accomplishments focused |
| Harvard | Academic / traditional |
| Classic | Standard professional |
| Modern | Contemporary |
| Simple | Minimal / clean |
| Grayscale | Black & white |

**Per-profile customisation:** font (8 options), font size, background colour (White / Peach), date format (12 formats), bullet style.

---

## Key Constraints

| Constraint | Value |
|---|---|
| Package name | `com.nithra.nithraresume` |
| minSdk | 24 |
| targetSdk / compileSdk | 37 |
| Language | Kotlin only |
| Build system | Gradle with Kotlin DSL |
| Product flavors | TestAdMob (default), ProdAdMob, NoAdMob |
| Max profiles | 20 |
| Max sections per profile | 20 |
| Max items per list section | 10 |

---

## V2 → V3 Data Continuity

**Database:** Room opens the existing SQLite database transparently on upgrade — no migration scripts needed.

**Files:** On first launch after upgrade, the app automatically migrates:
- Profile photos from `Nithra/SmartResume/Photo/` → app-scoped storage
- Signature images from `Nithra/SmartResume/Signature/` → app-scoped storage  
- Generated resume PDFs from `Nithra/SmartResume/Files/` → app-scoped storage

All file paths in the database are updated automatically. Users see a "Restoring Your Files" dialog while migration completes (usually under 5 seconds).

**Why app-scoped storage?**
- No storage permissions required (better privacy)
- Automatic backup via Android's backup system
- Files deleted when app is uninstalled

---

## Build & Run

### Setup
1. **Firebase:** Add `google-services.json` to `app/` directory
2. **Signing:** Create `keystore.properties` at project root with:
   ```properties
   storeFile=path/to/keystore.jks
   storePassword=your-password
   keyAlias=your-alias
   keyPassword=your-key-password
   ```
3. Open in Android Studio Hedgehog or later
4. Sync Gradle

### Build Commands

```bash
# Debug build (TestAdMob, default)
./gradlew assembleTestAdMobDebug

# Release builds
./gradlew assembleTestAdMobRelease   # Test IDs
./gradlew assembleProdAdMobRelease   # Production (no test IDs)
./gradlew assembleNoAdMobRelease     # No ads

# All variants
./gradlew assemble

# Bundle (AAB) for Play Store
./gradlew bundleTestAdMobRelease

# Tests
./gradlew test                                          # Unit tests
./gradlew connectedAndroidTest                          # Instrumented tests
./gradlew testTestAdMobDebugUnitTest --tests "com..*"   # Single test class

# Clean
./gradlew clean
```

### APK Naming

Output: `smart-resume-{flavor}-{buildType}-{versionCode}-{versionName}.apk`

Example: `smart-resume-TestAdMob-debug-72-4.2.0.apk`

### Product Flavors

| Flavor | AdMob | Test IDs | Use Case |
|---|---|---|---|
| **TestAdMob** | ✓ | ✓ | Development & testing |
| **ProdAdMob** | ✓ | ✗ | Production (Play Store) |
| **NoAdMob** | ✗ | ✓ | Ad-free variant |

Control at runtime via `BuildConfig.isAdMobEnable` and `BuildConfig.isTestAdMobId`.

### Run on Device

```bash
# Install debug APK
./gradlew installTestAdMobDebug

# Build and run
./gradlew runTestAdMobDebug
```
