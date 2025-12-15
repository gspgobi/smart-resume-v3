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
- **DB version:** 1 (no migration needed)
- **Tables:** 15 (resume formats, section heads, 8 section child types, user profiles, FCM data)
- **Seed data:** 6 resume formats, 2 section groups, 19 section templates

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
| targetSdk / compileSdk | 36 |
| Language | Kotlin only |
| Build system | Gradle with Kotlin DSL |
| Max profiles | 20 |
| Max sections per profile | 20 |
| Max items per list section | 10 |

---

## V2 → V3 Data Continuity

Because the package name, database name, and all column names are identical to V2:

- Room opens the existing SQLite database transparently on first launch after upgrade
- No migration scripts are needed
- Generated PDFs stored at `getExternalFilesDir()` paths are preserved
- Profile photos and signature image paths stored in DB columns are unchanged

---

## Build & Run

1. Add `google-services.json` to `app/`
2. Open in Android Studio Hedgehog or later
3. Sync Gradle and run on a device / emulator (API 24+)
