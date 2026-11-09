<div align="center">

# 📸 Jummo Photos AI
### **Smart Event Photography Suite — Ultra Luxury Studio Edition**

[![Platform](https://img.shields.io/badge/Platform-Android%207.0%2B%20(API%2024--36)-F6C445?style=for-the-badge&logo=android&logoColor=090610)](https://github.com/haiker729-sys/Photo-/releases)
[![UI](https://img.shields.io/badge/Jetpack%20Compose-Material%203-A020F0?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Build](https://img.shields.io/badge/AGP%209.1.1-Gradle%209.3.1%20%7C%20JDK%2021-7E22CE?style=for-the-badge&logo=gradle&logoColor=white)](https://github.com/haiker729-sys/Photo-/actions)
[![Backend](https://img.shields.io/badge/Firebase-Auth%20%26%20Cloud%20Firestore-FFCA28?style=for-the-badge&logo=firebase&logoColor=090610)](https://firebase.google.com/)

<p align="center">
  <strong>Commercial-Grade Event Photography • DSLR / RAW Import • Smart Upload Queue • Google Drive Sync • Branded Event QR • Consent-First AI Face Search</strong>
</p>

<p align="center">
  <a href="https://github.com/haiker729-sys/Photo-/releases/latest"><strong>⬇ Download Latest APK</strong></a>
  &nbsp;•&nbsp;
  <a href="https://haiker729-sys.github.io/Photo-/"><strong>🌐 Official Website</strong></a>
  &nbsp;•&nbsp;
  <a href="https://github.com/haiker729-sys/Photo-/releases"><strong>📦 All Releases</strong></a>
  &nbsp;•&nbsp;
  <a href="https://github.com/haiker729-sys/Photo-/actions"><strong>⚙ CI/CD Workflows</strong></a>
</p>

</div>

---

## ✨ Visual Showcase

<div align="center">
  <table>
    <tr>
      <td align="center" width="34%">
        <img src="docs/assets/screen-login.png" alt="Jummo Photos AI — Premium Login Screen" width="100%" />
        <br />
        <sub><strong>Premium Studio Login Screen</strong><br/>DSLR Bokeh Hero • 5 Event Categories • Google Auth</sub>
      </td>
      <td align="center" width="66%">
        <img src="docs/assets/screen-suite-8.png" alt="Jummo Photos AI — 8-Screen Ultra Luxury Suite" width="100%" />
        <br />
        <sub><strong>8-Screen Ultra Luxury Edition</strong><br/>Splash • Home • Events • CameraX • Uploads • Profile • Gallery • Admin</sub>
      </td>
    </tr>
  </table>
</div>

---

## 🏛 Executive Overview

**Jummo Photos AI** is an end-to-end Android photography studio platform engineered for professional wedding, festival, corporate, school, and live event photographers. Built natively with **Kotlin** and **Jetpack Compose**, it unifies on-location **CameraX** capture, zero-permission **DSLR / SD Card RAW & JPG import**, battery- and network-aware **WorkManager background uploads**, **Google Drive** cloud folder organization, **branded Event QR code sharing**, and **privacy-first Guest AI Face Search** into a single cohesive mobile suite.

### Key Pillars
- **Non-Destructive Local Storage**: Captured and imported originals are preserved safely in `filesDir/photos/<eventId>/` and are never automatically deleted by background sync workers.
- **Resilient Studio Upload Queue**: Automatic and manual upload modes powered by AndroidX `WorkManager` with configurable **Wi-Fi Only** and **Battery > 20%** hardware constraints.
- **Consent-First Guest Privacy**: Event guests scanning a QR code must provide explicit, verifiable consent before initiating AI Face Search, and can delete their face-search session data or report an incorrect match at any time.
- **Zero-Trust Multi-Tenant Cloud Architecture**: Every profile, event, upload record, and guest consent session in Cloud Firestore is strictly isolated by `userId` and `eventId` via verified `firestore.rules`.

---

## 🎨 Luxury Design System

Crafted in the **Jummo Photos AI Ultra Luxury** aesthetic combining deep studio contrast with champagne gold and electric violet accents:

| Design Token | Color Swatch | Hex Code | Usage Across Suite |
| :--- | :---: | :---: | :--- |
| `JummoObsidian` | ⬛ | `#090610` | Deep midnight black studio backdrop & edge-to-edge canvas |
| `JummoDeepPurple` | 🟪 | `#140B24` | Gradient underlays, navigation dock & modal containers |
| `JummoCardPurple` | 🟪 | `#1E1035` | Elevated glassmorphic cards, telemetry tiles & settings rows |
| `JummoVioletBright` | 🔮 | `#A855F7` | Active filter pills, glowing card borders & AI indicators |
| `JummoGoldBright` | 🟨 | `#F6C445` | Primary CTA buttons, Crown Camera Lens logo & Pro badges |
| `JummoSuccessGreen` | 🟩 | `#22C55E` | Active event badges, connected Google Drive & verified checks |

---

## 📱 Complete 8-Screen Product Architecture

| # | Screen | Source File | Core Capabilities |
| :-: | :--- | :--- | :--- |
| **01** | **Splash & Login Screen** | `MainActivity.kt`<br/>`AuthScreen.kt` | Animated Crown Camera Lens emblem, DSLR & bokeh hero banner, 5 horizontal event categories (*Weddings, Festivals, Corporate, Schools, Live Events*), 4 glass feature rows, and **Google Sign-In** (`CredentialManager` + Firebase Auth). |
| **02** | **Home Dashboard** | `HomeDashboardScreen.kt` | Personalized studio greeting (`Hello, Meraj Sir 👋`), AI photography hero banner, side-by-side `+ Create Event` and `Camera` buttons, **3+2 live telemetry grid** (*Events, Photos, Uploaded, Pending, Failed*), Google Drive sync status bar, and Recent Events list. |
| **03** | **My Events** | `EventsScreen.kt` | Instant search bar, category filter chips (*All, Wedding, Birthday, Festival, School, Corporate*), *Active / Archived* tabs, compact event cards with cover thumbnails, and 1-tap actions for *Gallery*, *QR Code*, *Edit*, *Archive*, and *Delete*. |
| **04** | **Pro CameraX Viewfinder** | `CameraScreen.kt` | Full-screen **CameraX** preview & capture with top studio bar (*Close, Flash, HDR, 4K, Grid*), floating active event selector pill (`📁 Event : ...`), last-shot thumbnail preview, gold-ring shutter button, front/rear lens switch, and *Photo / Video / Pro* mode selector. |
| **05** | **Upload Manager** | `UploadsScreen.kt` | Segmented *Queue / Uploaded / Failed* tabs, *Automatic Upload* toggle with *Wi-Fi Only* and *Battery > 20%* status badges, **Select Photos** (Android Photo Picker) and **DSLR / SD Import** (`RAW`, `DNG`, `CR2`, `NEF`, `ARW`, `PNG`, `JPG`), and per-file progress bars. |
| **06** | **Profile & Settings** | `ProfileScreen.kt` | Studio identity card with `👑 Photographer Pro` badge and 8 expandable configuration modules: *Account & Studio*, *Google Drive*, *Upload Preferences*, *Storage*, *Privacy & Security*, *Permissions*, *Help & Support*, and *About Jummo Photos AI*. |
| **07** | **Event Photo Gallery & Guest QR Portal** | `EventsScreen.kt`<br/>`GuestPortalModalScreen.kt`<br/>`QrCodeGenerator.kt` | 3-column photo grid with *All Photos*, *People (AI)*, and *Highlights* tabs, multi-select batch toolbar (*Select, Download, Share, More*), branded Event QR card generator, and **Consent-First Guest AI Face Search**. |
| **08** | **Admin Dashboard** | `AdminDashboardScreen.kt` | Role-based command center (*Super Admin, Event Admin, Support Admin, Finance / Storage Admin*), 2×2 KPI summary cards, dual-tone storage bar, 7-day upload line chart, and **14 interactive governance modules**. |

---

## 🌐 Official Website & Real-Time APK Release Verification

The repository includes a production-ready official website in [`/docs`](docs/) (deployed via GitHub Pages) as well as an in-app **Official Website & APK Release Portal** (`OfficialWebsiteScreen.kt`):

- **Live GitHub Releases Integration**: Queries `https://api.github.com/repos/haiker729-sys/Photo-/releases/latest` (with fallback to `/releases`) in real time.
- **Verified Asset Metadata**: Displays the exact published `tag_name`, verified `.apk` file size in MB, `published_at` date, and direct `browser_download_url`.
- **Strict Integrity Guarantee**: Never uses fake download links or expired CI artifact URLs. If no release is published yet, it displays an explicit **APK Release Status Notice** with instructions to trigger the release workflow.

---

## 📥 How to Download & Install the Android APK

1. Visit the [**Official Website**](https://haiker729-sys.github.io/Photo-/) or the [**GitHub Releases Page**](https://github.com/haiker729-sys/Photo-/releases).
2. Download the latest published `.apk` asset (`Jummo-Photos-AI-v1.0.0.apk` or `app-debug.apk`).
3. Open the downloaded `.apk` file on your Android device (**Android 7.0 Nougat / API 24 or newer**).
4. If prompted by Android, allow **"Install unknown apps"** for your browser or file manager.
5. Launch **Jummo Photos AI** and sign in with your Google account.

---

## 🛠 Technical Specifications

| Layer | Technology / Specification |
| :--- | :--- |
| **Application ID** | `com.aistudio.jummophotos.vkpzrt` |
| **Android Namespace** | `com.example` |
| **Language & Concurrency** | Kotlin `2.2.10` • Kotlin Coroutines & `StateFlow` |
| **Build Toolchain** | Gradle `9.3.1` • Android Gradle Plugin (AGP) `9.1.1` • JDK `21` |
| **SDK Compatibility** | `minSdk = 24` (Android 7.0) • `targetSdk = 36` • `compileSdk = 36` |
| **UI & Design System** | Jetpack Compose • Material Design 3 • Edge-to-Edge `WindowInsets` |
| **Authentication** | Firebase Authentication • Google Sign-In via Jetpack `CredentialManager` |
| **Cloud Database** | Cloud Firestore (`photographer_profiles`, `events`, `photo_uploads`, `guest_consents`) |
| **Camera & Media** | AndroidX CameraX `1.5.2` • Coil Compose • Photo Picker & SAF Document Importer |
| **Background Sync** | AndroidX `WorkManager` (`PhotoUploadWorker`) with network & battery constraints |
| **Automated Testing** | JUnit 4 • Robolectric (`sdk = [36]`) • Compose UI Test • Firestore Emulator Rules Tests |

---

## 📂 Repository Structure

```text
Photo-/
├── .github/workflows/
│   ├── android-apk-build.yml        # CI/CD: Builds APK, uploads artifact & publishes GitHub Release
│   └── deploy-website.yml           # CI/CD: Deploys /docs Official Website to GitHub Pages
├── app/
│   ├── build.gradle.kts             # App module Gradle config (AGP 9.1.1, Compose, Firebase, CameraX)
│   ├── google-services.json         # Firebase configuration
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml  # App permissions, FileProvider & MainActivity configuration
│       │   ├── assets/website/      # Bundled HTML5/CSS3/JS Official Website for offline/in-app access
│       │   ├── java/com/example/
│       │   │   ├── MainActivity.kt                  # Root navigation shell & luxury splash screen
│       │   │   ├── data/
│       │   │   │   ├── model/JummoModels.kt         # Firestore domain models & sealed UiState
│       │   │   │   └── repository/JummoRepository.kt # Real-time Firestore snapshot repositories
│       │   │   ├── ui/
│       │   │   │   ├── auth/AuthScreen.kt           # Screen 1: Premium Login Screen & Google Auth
│       │   │   │   ├── components/                  # Luxury bottom bar, camera emblem & shared UI
│       │   │   │   ├── screens/
│       │   │   │   │   ├── HomeDashboardScreen.kt   # Screen 2: Studio Home Dashboard
│       │   │   │   │   ├── EventsScreen.kt          # Screen 3 & 7: My Events & Photo Gallery
│       │   │   │   │   ├── CameraScreen.kt          # Screen 4: Pro CameraX Viewfinder
│       │   │   │   │   ├── UploadsScreen.kt         # Screen 5: Smart Upload Queue & DSLR Import
│       │   │   │   │   ├── ProfileScreen.kt         # Screen 6: Profile & Studio Settings
│       │   │   │   │   ├── GuestPortalModalScreen.kt # Consent-First Guest QR & AI Face Search
│       │   │   │   │   ├── AdminDashboardScreen.kt  # Screen 8: 14-Module Studio Admin Dashboard
│       │   │   │   │   └── OfficialWebsiteScreen.kt # Native + WebView Official Website Portal
│       │   │   │   ├── theme/                       # Color, Typography & Material 3 Theme definitions
│       │   │   │   └── viewmodel/JummoViewModel.kt  # MVVM state orchestration & file persistence
│       │   │   ├── util/QrCodeGenerator.kt          # Deterministic QR matrix & branded card bitmap
│       │   │   └── worker/PhotoUploadWorker.kt      # WorkManager background upload job
│       │   └── res/                                 # Custom adaptive icon, studio drawables & fonts
│       └── test/java/com/example/
│           └── JummoAppRobolectricTest.kt           # 7 Robolectric CUJ & UI verification tests
├── docs/                            # Standalone Official Website (index.html, styles.css, app.js)
├── firebase-blueprint.json          # Cloud Firestore schema blueprint
├── firestore.rules                  # Zero-trust Firestore security rules
├── firestore.test.js                # Local Firebase Emulator security rules test suite
├── gradle/libs.versions.toml        # Centralized Gradle version catalog (AGP 9.1.1)
├── build.gradle.kts                 # Root Gradle build configuration
└── settings.gradle.kts              # Project settings (rootProject.name = "Jummo Photos AI")
```

---

## 🚀 Local Development & Build Guide

### Prerequisites
- **JDK 21**
- **Gradle 9.3.1**
- **Android SDK** (`platforms;android-36`, `build-tools;36.0.0`, `platform-tools`)

### 1. Assemble the Debug APK
```bash
gradle :app:assembleDebug --no-daemon
```
The compiled and signed debug APK will be generated at:
```text
app/build/outputs/apk/debug/app-debug.apk
```

### 2. Run the Robolectric UI & Unit Test Suite
```bash
gradle :app:testDebugUnitTest --tests com.example.JummoAppRobolectricTest --no-daemon
```

Verified test cases in `JummoAppRobolectricTest`:
- `loginScreen_displaysHeroCategoriesFeatureCardGoogleButtonAndSecurityFooter`
- `qrCodeGenerator_generatesDeterministicMatrixAndBrandedBitmap`
- `homeDashboardScreen_displaysLogoProfileStorageAndQuickCamera`
- `eventsScreen_opensCreateEventAndEventDetailsWithPhotoManagement`
- `profileScreen_displaysAllProfessionalSettingsSections`
- `guestPortalModalScreen_requiresExplicitConsentBeforeFaceSearch`
- `officialWebsiteScreen_displaysHeroDownloadButtonFeaturesPreviewFaqAndFooter`

---

## ⚙️ CI/CD & Automated GitHub Releases

| Workflow | File | Trigger | Actions Performed |
| :--- | :--- | :--- | :--- |
| **Build Jummo Photos AI APK** | `.github/workflows/android-apk-build.yml` | Push to `main`/`master`, tag `v*`, PR, or manual dispatch | Configures JDK 21 + Gradle 9.3.1 + Android SDK 36, builds `app-debug.apk`, uploads the workflow artifact, and publishes `Jummo-Photos-AI-v1.0.0.apk` to **GitHub Releases**. |
| **Deploy Official Website** | `.github/workflows/deploy-website.yml` | Push to `main`/`master` or manual dispatch | Publishes the `/docs` Official Website directly to **GitHub Pages** (`https://haiker729-sys.github.io/Photo-/`). |

---

## 🔒 Security, Privacy & Google Play Compliance

- **Strict Firestore Ownership & Schema Enforcement**: `firestore.rules` validates document schemas, field types, string lengths, and owner `userId` matching across all collections (`photographer_profiles`, `events`, `photo_uploads`, and `guest_consents`).
- **Explicit Guest Face-Search Consent**: AI Face Search is gated behind a mandatory consent checkbox in `GuestPortalModalScreen`, with 1-tap session deletion (`deleteGuestFaceData`) and mismatch reporting (`reportIncorrectMatch`).
- **Zero Broad Storage Permissions**: Compliant with Google Play photo/video permission policies by exclusively using the Android Photo Picker (`PickMultipleVisualMedia`) and Storage Access Framework (`OpenMultipleDocuments`).

---

## 📬 Contact & Maintainer

- **Product**: Jummo Photos AI — Smart Event Photography Suite
- **Maintainer**: Meraj Sir (`Jummo Pro Studio`)
- **Email**: [merajalam906090@gmail.com](mailto:merajalam906090@gmail.com)
- **GitHub Repository**: [https://github.com/haiker729-sys/Photo-](https://github.com/haiker729-sys/Photo-)
