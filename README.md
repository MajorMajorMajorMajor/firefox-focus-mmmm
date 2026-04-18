# Firefox Focus (mmmm fork)

A custom build of [Firefox Focus for Android](https://github.com/mozilla-mobile/firefox-android) with a handful of quality-of-life patches applied on top of upstream releases.

## What's different

- **No shortcuts cap** — the default build limits the number of home screen shortcuts (Top Sites). This fork removes that cap.
- **Grid layout with dynamic columns** — shortcuts display in a proper grid that adapts to screen width, instead of a fixed single-row strip.
- **Drag-to-reorder shortcuts** — long-press any shortcut to drag it to a new position.
- **Font inflation toggle** — adds a setting to disable Android's automatic text size inflation on desktop-layout pages, so pages render at the size the site intended.
- **Reader mode** — adds a reader mode button to the address bar and a reader mode entry in the browser menu, with appearance controls (font size, line width, theme).
- **Build watermark** — the new-tab page shows the build number in the corner so you can confirm which version is running.

## Install

Grab the latest APK from the [Releases](../../releases/latest) page (`app-focus-arm64-v8a-debug.apk`). The APK is signed with a stable debug key so upgrades install over previous builds without uninstalling first.

**arm64-v8a only** — this build targets 64-bit ARM devices.

### Obtainium

Point Obtainium at this repo (`MajorMajorMajorMajor/firefox-focus-mmmm`) and filter the asset name to `app-focus-arm64-v8a-debug.apk`. New releases are published automatically whenever upstream ships a new Focus version.

## Build

Releases are built automatically via GitHub Actions on every push to `main` that touches the Android source or CI files. The workflow:

1. Checks out the repo
2. Builds `app-focus-arm64-v8a-debug.apk` via `./mach build`
3. Signs with a persistent debug keystore (stored as the `ANDROID_DEBUG_KEYSTORE` repo secret)
4. Publishes a GitHub Release tagged `focus-<version>-build-<run_number>`

## Branch layout

| Branch | Purpose |
|---|---|
| `upstream-mirror` | Tracks the latest upstream `FIREFOX-ANDROID_X_Y_Z_RELEASE` tag |
| `src/patches` | Fork patches on top of `upstream-mirror` |
| `src/automation` | CI / signing changes on top of `src/patches` |
| `main` | Tip of `src/automation` — this is what gets built and released |

Upstream sync runs automatically every Monday at 03:00 UTC via `sync-upstream.yml`.

## Upstream

Based on Mozilla's [firefox-android](https://github.com/mozilla-mobile/firefox-android) source. See [README.upstream.md](README.upstream.md) for the original Mozilla README.
