# Complete Release & Publishing Guide for SAM Tools 🚀

This document details the end-to-end process for building, testing, versioning, generating release notes, signing, and publishing **SAM Tools** releases to GitHub Releases using GitHub Actions.

---

## 📑 Table of Contents
1. [Prerequisites](#1-prerequisites)
2. [Local Testing & Verification](#2-local-testing--verification)
3. [Versioning & Autoincrement Strategy](#3-versioning--autoincrement-strategy)
4. [Writing Release Notes](#4-writing-release-notes)
5. [Release Keystore & GitHub Secrets Setup](#5-release-keystore--github-secrets-setup)
6. [Publishing via GitHub Actions (Automated)](#6-publishing-via-github-actions-automated)
7. [Manual Workflow Dispatch Release](#7-manual-workflow-dispatch-release)
8. [Post-Release Verification](#8-post-release-verification)

---

## 1. Prerequisites

Before publishing a release, ensure you have:
- **Git** configured with access to your repository.
- **JDK 17 or JDK 21** installed locally.
- **Android SDK** (API 35 platforms and build-tools installed).
- A connected Android physical device (Samsung Galaxy recommended) or an Android Virtual Device (AVD).

---

## 2. Local Testing & Verification

Always run all automated checks locally before triggering a release.

### Step 2.1: Run Unit Tests
```bash
./gradlew testDebugUnitTest
```
All unit tests in `FileSaveManagerTest` and `AppShortcutRegistryTest` must pass.

### Step 2.2: Run Lint Checks
```bash
./gradlew lintDebug
```
Ensure there are no fatal errors in resources or manifests.

### Step 2.3: Build Debug APK for Verification
```bash
./gradlew assembleDebug
```
The output APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`

### Step 2.4: Test on Device
Install the APK directly using ADB:
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

**Verification Checklist**:
- [ ] Open the app and verify the Home dashboard loads without crashing.
- [ ] Go to your gallery/browser, share an image/PDF, and select **"Save to Downloads"**. Check that the file appears in your `Downloads` directory.
- [ ] Long press the SAM Tools icon on your launcher: verify that **Tailscale Connect** and **Tailscale Disconnect** shortcuts appear.
- [ ] Open SAM Tools -> Shortcuts: Tap **"Pin to Home"** and check that a dedicated icon appears on your home screen.
- [ ] Switch device language to Ukrainian (`Українська`) and verify full translation accuracy.

---

## 3. Versioning & Autoincrement Strategy

Android uses two version parameters in `app/build.gradle.kts`:
- **`versionCode`** (integer): Internal version number evaluated by the OS and app stores to determine upgrade priority. Must increase with every published release.
- **`versionName`** (string): User-facing semantic version string (e.g. `1.0.0`, `1.1.0`).

### Semantic Versioning Format (`MAJOR.MINOR.PATCH`):
- **`MAJOR`**: Significant architectural overhaul or breaking changes.
- **`MINOR`**: New One UI tools, new shortcuts, or substantial feature additions.
- **`PATCH`**: Bug fixes, performance improvements, translation updates.

### Version Autoincrement Methods:

#### Method A: Git-Tag Driven Versioning (Recommended)
You can derive `versionName` directly from the latest Git tag in `app/build.gradle.kts`:
```kotlin
fun getVersionName(): String {
    return try {
        val stdout = java.io.ByteArrayOutputStream()
        exec {
            commandLine("git", "describe", "--tags", "--abbrev=0")
            standardOutput = stdout
        }
        stdout.toString().trim().removePrefix("v")
    } catch (e: Exception) {
        "1.0.0"
    }
}

fun getVersionCode(): Int {
    return try {
        val stdout = java.io.ByteArrayOutputStream()
        exec {
            commandLine("git", "rev-list", "--count", "HEAD")
            standardOutput = stdout
        }
        stdout.toString().trim().toInt()
    } catch (e: Exception) {
        1
    }
}
```

#### Method B: Manual Bump in `app/build.gradle.kts`
Before releasing, update:
```kotlin
defaultConfig {
    versionCode = 2       // increment by 1
    versionName = "1.1.0" // new semver
}
```
Commit the bump:
```bash
git add app/build.gradle.kts
git commit -m "chore: bump version to v1.1.0"
```

---

## 4. Writing Release Notes

A great release note communicates what changed clearly to both technical users and daily Galaxy phone users.

### Recommended Release Notes Structure:
```markdown
## 🛠️ What's New in v1.1.0

### ✨ New Features
- **Tailscale Quick Settings Tile**: Added One UI Quick Panel tile for one-tap VPN toggling (#12).
- **Custom Download Subfolder**: Saved files can now automatically route into `Downloads/SAM Tools` (#15).

### 🐛 Bug Fixes & Improvements
- Fixed an issue where filenames with colons or slashes caused save failures on Android 14 (#18).
- Improved zero-UI shortcut trampoline response time.

### 🌐 Localization
- Updated Ukrainian translations for new settings terms.

### 📦 Artifacts
- `sam-tools.apk`: Universal APK for direct sideloading.
- `sam-tools.aab`: Android App Bundle for store distribution.
```

When using GitHub Actions, if you don't supply custom release notes, GitHub's automated release notes generator (`generate_release_notes: true`) will generate a clean list of merged pull requests and commit messages.

---

## 5. Release Keystore & GitHub Secrets Setup

To produce production-signed APKs that users can update seamlessly without reinstalling:

### Step 5.1: Generate a Keystore (One-Time)
Run on your local machine:
```bash
keytool -genkey -v -keystore sam-tools-release.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias samtools \
  -storepass "YOUR_STRONG_PASSWORD" \
  -keypass "YOUR_STRONG_PASSWORD"
```

### Step 5.2: Encode the Keystore to Base64
```bash
# macOS
base64 -i sam-tools-release.jks | pbcopy

# Linux
base64 -w 0 sam-tools-release.jks
```

### Step 5.3: Add Secrets to GitHub Repository
Go to: **GitHub Repository -> Settings -> Secrets and variables -> Actions -> New repository secret**.

Add the following secrets:
| Secret Name | Description |
|---|---|
| `SIGNING_KEY_BASE64` | Base64-encoded string of `sam-tools-release.jks` |
| `SIGNING_KEY_PASSWORD` | Password for the key entry |
| `SIGNING_STORE_PASSWORD` | Password for the keystore file |
| `SIGNING_KEY_ALIAS` | Key alias (e.g. `samtools`) |

*(Note: If signing secrets are omitted, the workflow builds an unsigned release APK or signed debug APK ready for testing).*

---

## 6. Publishing via GitHub Actions (Automated)

The repository includes `.github/workflows/release.yml`. Publishing a release is as simple as pushing a Git tag.

### Step 6.1: Ensure Clean Branch
```bash
git checkout main
git pull origin main
```

### Step 6.2: Create and Push Semver Tag
```bash
# Tag the current commit
git tag -a v1.0.0 -m "Release v1.0.0"

# Push the tag to GitHub
git push origin v1.0.0
```

### Step 6.3: GitHub Actions Execution
1. The `Build & Publish Release` workflow triggers automatically.
2. Runs all unit tests.
3. Compiles `sam-tools.apk` and `sam-tools.aab`.
4. Creates a new GitHub Release under the `v1.0.0` tag.
5. Uploads the compiled APK and AAB assets to the release.

---

## 7. Manual Workflow Dispatch Release

You can also trigger a release directly from the GitHub web interface without pushing a tag locally:

1. Navigate to the **Actions** tab on your GitHub repository.
2. Select **"Build & Publish Release"** from the left sidebar.
3. Click the **"Run workflow"** button.
4. Enter:
   - **Release tag**: e.g., `v1.0.0`
   - **Release Title**: e.g., `SAM Tools v1.0.0 — Initial Release`
   - **Custom release notes**: (Optional release highlights)
   - **Mark as pre-release**: Check if releasing a Beta/RC build.
5. Click **Run workflow**.

---

## 8. Post-Release Verification

After the workflow finishes:
1. Navigate to **Releases** on your GitHub repository page (`https://github.com/<owner>/sam-tools/releases`).
2. Verify that:
   - The release tag, title, and release notes are formatted properly.
   - `sam-tools.apk` and `sam-tools.aab` are attached and downloadable.
3. Download `sam-tools.apk` to a Samsung Galaxy device, install it, and verify that update installation succeeds.
