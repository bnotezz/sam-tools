# SAM Tools 🛠️

A lightweight, modern Android utility built specifically to bridge gaps and supercharge productivity on Samsung Galaxy devices running One UI.

Designed with **Jetpack Compose**, **Material Design 3**, **Kotlin Coroutines**, and **Scoped Storage (MediaStore)**, SAM Tools provides native-feeling tools with zero bloatware, no ads, and no tracking.

---

## ✨ Features

### 1. 📥 Save to Downloads (Share Sheet Integration)
One UI's stock share sheet often makes it tricky to simply "save a shared file or picture directly to the Downloads folder" without opening a heavy file manager or selecting nested folders.
- Registers seamlessly in the system Share Sheet for `ACTION_SEND` and `ACTION_SEND_MULTIPLE`.
- Mirrors Google Files' `SaveToDownloadsActivity` behavior with an explicit compatibility activity alias (`com.google.android.apps.nbu.files.gateway.savetodownloads.SaveToDownloadsActivity`).
- Supports **all MIME types**: images, videos, audio, PDFs, APKs, documents, and plain text.
- **Smart Scoped Storage**: Uses `MediaStore.Downloads` on Android 10+ (API 29+) without requiring legacy storage permissions.
- **Flexible UX**: Choose between:
  - *Interactive Bottom Sheet*: Shows saving progress, file previews, and buttons to open the saved file or the Downloads folder.
  - *Instant Background Save*: Saves silently in the background and delivers a quick confirmation toast or notification.
- **Duplicate Collision Handling**: Automatically manages filenames with clean increments (e.g. `file (1).pdf`).
- **Custom Subfolder Support**: Keep downloads organized in `Downloads/SAM Tools` or straight in root `Downloads`.

### 2. ⚡ Dynamic App Shortcuts (Tailscale VPN & Beyond)
Quickly toggle Tailscale VPN directly from your launcher or home screen:
- **Tailscale Connect**: Dispatches `com.tailscale.ipn.CONNECT_VPN` broadcast to Tailscale.
- **Tailscale Disconnect**: Dispatches `com.tailscale.ipn.DISCONNECT_VPN` broadcast to Tailscale.
- **Launcher Shortcuts**: Long-press the SAM Tools icon on your home screen or app drawer to access shortcuts.
- **Home Screen Pinning**: Pin dedicated 1-tap connect/disconnect icons directly onto your One UI home screen.
- **Zero-UI Trampoline**: Actions fire instantaneously through a lightweight trampoline activity with no lingering window or background overhead.
- **Settings Toggle**: Enable or disable specific dynamic shortcuts according to your preferences.
- **Extensible Architecture**: Designed with an `AppShortcutRegistry` so additional One UI tools can easily be plugged in.

### 3. 🌐 Dual Language Support
- **English** (Default)
- **Ukrainian** (Повна якісна українська локалізація адаптована під термінологію One UI)

### 4. 🎨 Modern Design & Theming
- **Material Design 3** with dynamic color (Material You) adapting to your One UI palette.
- Dark and Light theme support.
- Fully edge-to-edge layout (`enableEdgeToEdge()`).
- Modern adaptive launcher icons with monochrome support (Android 13+).

---

## 📱 Screenshots & Architecture

```
sam-tools/
├── app/
│   ├── src/main/java/com/samtools/
│   │   ├── SamToolsApp.kt                     # Application & dynamic shortcut synchronizer
│   │   ├── data/
│   │   │   ├── preferences/                   # Jetpack DataStore preferences
│   │   │   └── shortcuts/                     # App shortcuts registry & pinning engine
│   │   ├── service/
│   │   │   ├── FileSaveManager.kt             # MediaStore Scoped Storage file saver
│   │   │   └── TailscaleController.kt         # Tailscale broadcast dispatcher
│   │   └── ui/
│   │       ├── MainActivity.kt                # Jetpack Compose dashboard & settings
│   │       ├── SaveToDownloadsActivity.kt     # Share sheet target with M3 bottom sheet
│   │       ├── ShortcutTrampolineActivity.kt  # Zero-UI launcher shortcut trampoline
│   │       ├── navigation/                    # Navigation graph
│   │       ├── screens/                       # Compose screens (Home, Shortcuts, Downloads, Settings)
│   │       └── theme/                         # Dynamic Color & Typography
│   └── src/main/res/                          # Vector icons, adaptive icons, and strings (EN + UK)
└── .github/workflows/                         # CI and automated GitHub Releases
```

---

## 🚀 Building and Running

### Prerequisites
- **JDK 17 or JDK 21**
- **Android SDK** (API 35 compileSdk, minSdk 26)
- **Gradle 8.9+** (configured in gradle-wrapper)

### Run Locally
1. Clone the repository:
   ```bash
   git clone https://github.com/sam-tools/sam-tools.git
   cd sam-tools
   ```
2. Build debug APK:
   ```bash
   ./gradlew assembleDebug
   ```
3. Run unit tests:
   ```bash
   ./gradlew testDebugUnitTest
   ```
4. Install on connected device or emulator:
   ```bash
   ./gradlew installDebug
   ```

---

## 📦 Publishing & GitHub Releases

SAM Tools includes a fully automated GitHub Actions release pipeline (`.github/workflows/release.yml`).

### Quick Release:
1. Tag your commit with a semver version:
   ```bash
   git tag -a v1.0.0 -m "Release v1.0.0"
   git push origin v1.0.0
   ```
2. GitHub Actions will automatically:
   - Run all unit tests and lint checks.
   - Build the release APK (`sam-tools.apk`) and Android App Bundle (`sam-tools.aab`).
   - Auto-generate the release notes from commits.
   - Publish a new release on GitHub with downloadable assets.

For full details on signing keys, version bumping, and manual workflow dispatch, see [RELEASE_PROCESS.md](RELEASE_PROCESS.md).

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
