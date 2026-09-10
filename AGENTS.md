# AGENTS.md — AI Agent Development Guide for SAM Tools

Welcome to the **SAM Tools** codebase. This document outlines architectural patterns, code conventions, invariant rules, and maintenance guidelines for AI coding agents working on this project.

---

## 🧭 Project Purpose & Philosophy

SAM Tools is an open-source Android utility designed to enhance the Samsung One UI experience by delivering missing system conveniences.

### Core Principles:
1. **Lightweight & Fast**: Zero unnecessary dependencies, minimal memory footprint, fast launch times.
2. **Zero Bloat / Privacy First**: No third-party tracking, no advertising SDKs, no unnecessary network requests.
3. **Strict Modern Android Standards**:
   - Modern Scoped Storage (`MediaStore.Downloads`) on API 29+. Never revert to legacy external storage permissions.
   - Jetpack Compose with Material Design 3 and Dynamic Color (Material You).
   - Kotlin Coroutines and StateFlow/DataStore for reactive state.
   - Gradle Version Catalog (`gradle/libs.versions.toml`) for dependency management.
4. **Bilingual Requirement**: Every new user-facing string **must** be localized in both English (`values/strings.xml`) and Ukrainian (`values-uk/strings.xml`).

---

## 🏛️ Architecture Overview

```
com.samtools
├── SamToolsApp.kt                     # Application lifecycle; observes preferences to update shortcuts
├── data/
│   ├── preferences/                   # Jetpack DataStore preferences
│   │   ├── UserPreferences.kt         # Data model
│   │   └── UserPreferencesRepository.kt
│   └── shortcuts/                     # Dynamic & pinned shortcuts registry
│       ├── AppShortcutItem.kt         # Model for individual shortcuts
│       └── AppShortcutRegistry.kt     # System shortcut manager adapter
├── service/                           # Core operational engines
│   ├── FileSaveManager.kt             # MediaStore Scoped Storage file saver
│   └── TailscaleController.kt         # Tailscale broadcast and intent controller
└── ui/                                # Jetpack Compose UI
    ├── MainActivity.kt                # Main launcher activity & settings dashboard
    ├── SaveToDownloadsActivity.kt     # Share-sheet target (ACTION_SEND / ACTION_SEND_MULTIPLE)
    ├── ShortcutTrampolineActivity.kt  # Zero-UI launcher shortcut action dispatcher
    ├── navigation/
    │   ├── Screen.kt                  # Routes & navigation metadata
    │   └── NavGraph.kt                # Compose navigation graph
    ├── screens/
    │   ├── home/HomeScreen.kt
    │   ├── shortcuts/ShortcutsScreen.kt
    │   ├── savetodownloads/SaveToDownloadsScreen.kt
    │   └── settings/SettingsScreen.kt
    └── theme/
        ├── Color.kt
        ├── Theme.kt
        └── Type.kt
```

---

## 🔒 Key Invariants & Rules

### 1. `SaveToDownloadsActivity`
- Must support both single file (`ACTION_SEND`) and multi-file (`ACTION_SEND_MULTIPLE`) intents.
- Must handle generic MIME types (`*/*`) as well as common types (`image/*`, `video/*`, `audio/*`, `application/pdf`, `text/plain`).
- Must handle both file URIs (`EXTRA_STREAM`, `clipData`) and plain text snippets (`EXTRA_TEXT`).
- Must preserve the compatibility `activity-alias` declared in `AndroidManifest.xml` (`com.google.android.apps.nbu.files.gateway.savetodownloads.SaveToDownloadsActivity`).
- Must respect `UserPreferences.instantSave` (silent background save vs. interactive BottomSheet).

### 2. `ShortcutTrampolineActivity`
- Must use theme `@style/Theme.SamTools.Trampoline` (transparent, zero preview, floating).
- Must execute the action and immediately call `finish()`.
- Must never linger in recents (`android:excludeFromRecents="true"`, `android:noHistory="true"`).

### 3. Adding New Shortcuts
To add a new launcher shortcut:
1. Add new string resources in `values/strings.xml` and `values-uk/strings.xml`.
2. Add action constant and definition in `AppShortcutRegistry.getAllShortcuts()`.
3. Add a preference toggle in `UserPreferences.kt` and `UserPreferencesRepository.kt`.
4. Handle the intent action in `ShortcutTrampolineActivity.kt`.
5. Add UI card in `ShortcutsScreen.kt`.

### 4. Adding New One UI Tools
1. Create a dedicated controller or service in `com.samtools.service`.
2. Expose settings in `UserPreferencesRepository`.
3. Add corresponding Compose screen and register in `Screen.kt` and `NavGraph.kt`.

---

## 🛠️ Verification & Build Commands

Always verify changes using the following Gradle commands:

```bash
# Run unit tests
./gradlew testDebugUnitTest

# Run lint checks
./gradlew lintDebug

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease
```
