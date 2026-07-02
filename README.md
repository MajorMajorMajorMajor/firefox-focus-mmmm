# Firefox Focus: My Custom Fork

A custom build of [Firefox Focus for Android](https://github.com/mozilla-mobile/firefox-android) with quality-of-life patches automatically applied on top of upstream releases.

## Added Features

- **Reworked shortcuts feature**
  - Add an unlimited number of shortcuts (was a maximum of 4)
  - Shortcuts display in a grid that adapts to screen width
  - Long-press any shortcut to drag it to a new position.
    
- **Fix font rendering**
  - Trigger Android's automatic text size inflation on non-mobile aware pages the way Firefox for Android does
  - Control this behaviour with a new option: `Settings` -> `Privacy & Security` -> `Web Content` -> `Scale text for readability`
    
- **Add reader mode**
  - Reader mode brought over from Firefox for Android
  - Address bar button to activate reader mode, appears if a compatible page is detected
  - Reader mode entry in the browser menu which *always appears*, so reader mode can be forced even if the detection failed
    
- **Build watermark**
  - The New Tab page show a watermark with the Firefox release version and the build number for this fork

## Install

### Manual install

Grab the latest APK from the [Releases](../../releases/latest) page. New releases are published automatically whenever upstream ships a new Focus version.

### Obtainium

Control updates with Obtainium by pointing it at this GitHub repository.

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
