# TASKS.md — Development Roadmap & Task Tracker

This document tracks completed tasks, active developments, and the future feature roadmap for **SAM Tools**.

---

## 🏁 Phase 1: MVP Core (v1.0.0) — [COMPLETED]

- [x] **Project Scaffolding**:
  - [x] Gradle 8.9+ wrapper with Kotlin Gradle DSL & Version Catalog (`libs.versions.toml`).
  - [x] Target SDK 35 (Android 15), Min SDK 26 (Android 8.0+ / One UI 1.0+).
  - [x] Jetpack Compose BOM, Material Design 3, Jetpack Navigation.
- [x] **Save to Downloads**:
  - [x] Scoped Storage engine (`FileSaveManager.kt`) utilizing `MediaStore.Downloads`.
  - [x] Intent filters for `ACTION_SEND` and `ACTION_SEND_MULTIPLE` with `*/*` and popular MIME types.
  - [x] Google Files compatibility `activity-alias` (`com.google.android.apps.nbu.files.gateway.savetodownloads.SaveToDownloadsActivity`).
  - [x] Interactive Material 3 BottomSheet dialog showing progress and quick actions.
  - [x] Instant background save mode configurable via settings.
  - [x] Custom subfolder support (e.g., `Downloads/SAM Tools`).
  - [x] Automatic collision handling and name deduplication (`file (1).ext`).
- [x] **App Shortcuts & Tailscale Control**:
  - [x] Tailscale Connect (`com.tailscale.ipn.CONNECT_VPN`) broadcast controller.
  - [x] Tailscale Disconnect (`com.tailscale.ipn.DISCONNECT_VPN`) broadcast controller.
  - [x] Zero-UI `ShortcutTrampolineActivity` for fast execution.
  - [x] Settings screen to enable/disable specific dynamic shortcuts.
  - [x] "Pin to Home Screen" (`requestPinShortcut`) support for 1-tap widgets.
  - [x] Extensible `AppShortcutRegistry` architecture.
- [x] **Localization**:
  - [x] Full English localization (`values/strings.xml`).
  - [x] Full Ukrainian localization (`values-uk/strings.xml`).
- [x] **Visuals & Graphics**:
  - [x] Vector adaptive icons (`ic_launcher_background`, `ic_launcher_foreground`).
  - [x] Android 13+ monochrome icon for Material You / One UI themed icons.
  - [x] Feature vector icons (`ic_download`, `ic_tailscale_connect`, `ic_tailscale_disconnect`, `ic_pin`).
- [x] **CI/CD & Documentation**:
  - [x] GitHub Actions CI workflow (`.github/workflows/ci.yml`).
  - [x] GitHub Actions Release workflow (`.github/workflows/release.yml`).
  - [x] Comprehensive documentation: `README.md`, `AGENTS.md`, `TASKS.md`, `RELEASE_PROCESS.md`.

---

## 📋 Phase 2: Next Enhancements (v1.1.0) — [BACKLOG]

- [ ] **Tailscale Quick Settings (QS) Tiles**:
  - [ ] Add `TileService` to toggle Tailscale VPN directly from Samsung One UI Quick Panel (notification shade).
- [ ] **Storage Access Framework (SAF) Folder Picker**:
  - [ ] Allow users to pick arbitrary storage directories (e.g. Documents, SD Card, custom paths) beyond `Downloads`.
- [ ] **One UI Notification Actions**:
  - [ ] Show actionable Heads-up notification on save complete with "Share", "Delete", and "Open" actions.
- [ ] **Additional One UI Shortcuts**:
  - [ ] NextDNS / AdGuard DNS Private DNS profile switcher shortcut.
  - [ ] Clipboard cleaner shortcut.
  - [ ] Screen resolution / refresh rate profile switcher shortcut (if system permits).

---

## 💡 Phase 3: Advanced Capabilities (v1.2.0+) — [IDEAS]

- [ ] **Share Sheet Direct Target Prioritization**:
  - [ ] Explore Android `ChooserTargetService` / Direct Share API to pin "Save to Downloads" to the top row of One UI Share Sheet.
- [ ] **Batch Auto-Rename Rules**:
  - [ ] Custom naming patterns for saved files (e.g., `{date}_{source}_{name}.{ext}`).
- [ ] **Tasker / MacroDroid Integration**:
  - [ ] Expose SAM Tools actions as Tasker plugins or standard intent actions.
