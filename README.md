# GroupTrack

A [Hammerhead Karoo](https://www.hammerhead.io/) extension for group rides, built on the
official [`karoo-ext`](https://github.com/hammerheadnav/karoo-ext) SDK.

Two features (in priority order):

1. **"Gap to Last" data field** — a selectable ride-page field showing how far behind the
   last rider in the group is. Uses a breadcrumb method (no shared route required): each
   rider builds their own ridden trail and snaps groupmates' reported positions onto it.
2. **Mechanical alert** — a rider who stops taps a button; everyone else gets an in-ride
   alert overlay (with sound) naming who stopped and roughly how far back.

No self-hosted backend: riders sync over a public MQTT broker, scoped by a random group
UUID, with payloads encrypted from the group code.

> **Status: Stage 1 (skeleton).** The "Gap to Last" field is registered and selectable in
> the data-field picker, currently emitting a test counter. MQTT, the gap engine and
> mechanical alerts follow in later stages — see [Roadmap](#roadmap).

## Requirements

- JDK 17
- Android SDK (platform 35, build-tools 35) — `compileSdk 35`, `minSdk 23`, `targetSdk 34`
- A GitHub token with `read:packages` (see below)

## GitHub Packages auth (required)

`karoo-ext` is distributed via GitHub Packages, which requires authentication **even for
public packages**. `settings.gradle.kts` resolves credentials in this order:

1. `gpr.user` / `gpr.key` in `local.properties` (recommended for local dev / Android Studio)
2. `GITHUB_ACTOR` / `GITHUB_TOKEN` (GitHub Actions — already wired in CI)
3. `USERNAME` / `TOKEN` environment variables (manual CLI builds)

Create a token at GitHub → Settings → Developer settings → Personal access tokens with the
`read:packages` scope, then add to `local.properties` (which is git-ignored):

```properties
sdk.dir=/path/to/Android/sdk
gpr.user=your-github-username
gpr.key=ghp_xxxxxxxxxxxxxxxxxxxx
```

(If you have the `gh` CLI logged in with `read:packages`, `gh auth token` prints a usable token.)

## Build

```bash
# Debug APK
./gradlew assembleDebug
# -> app/build/outputs/apk/debug/app-debug.apk

# Release APK (Stage 1: signed with the debug key so it sideloads without a keystore)
./gradlew assembleRelease
# -> app/build/outputs/apk/release/app-release.apk
```

## Install on a Karoo (sideload)

The Karoo 3 is an Android device reachable over ADB.

1. Enable ADB on the Karoo: **Settings → un-lock developer options**, then enable USB / WiFi
   debugging (Karoo 3 supports ADB over WiFi — note its IP).
2. Connect:
   ```bash
   # USB
   adb devices
   # …or WiFi
   adb connect <karoo-ip>:5555
   ```
3. Install:
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```
4. On the Karoo, edit a ride profile / data page and add the **Gap to Last** field from the
   field picker. Start a ride (or ride sim) and the field should tick upward every ~2s.

To uninstall: `adb uninstall com.hainesy.grouptrack`

## Project layout

```
app/src/main/kotlin/com/hainesy/grouptrack/
  MainActivity.kt                     # Compose companion UI (static for now)
  extension/
    GroupTrackExtension.kt              # KarooExtension entry point, declares data types
    GapToLastDataType.kt              # "Gap to Last" field (Stage 1: test counter)
app/src/main/res/xml/extension_info.xml   # declares the extension + its DataTypes to Karoo OS
```

## Roadmap

| Stage | Scope | Status |
|-------|-------|--------|
| 1 | Skeleton + hello-world data field, build + sideload docs | ✅ this commit |
| 2 | MQTT client, group create/join, settings, position publishing | ⬜ |
| 3 | Breadcrumb trail + gap engine, graphical field, unit tests | ⬜ |
| 4 | Mechanical alert button + in-ride alerts + auto-resolve | ⬜ |
| 5 | Encryption, QR join, reconnect robustness, signed-release CI, library submission | ⬜ |

## Architecture notes

- **GPS** comes from Karoo's own `OnLocationChanged` stream via `karoo-ext` — no Google Play
  Services, no fused location (none on Karoo).
- **MQTT** (Stage 2) will use an auto-reconnecting client with an offline outbound queue;
  default broker `broker.hivemq.com:1883`, user-overridable for a private broker.
- **In-ride alerts** (Stage 4) dispatch the SDK's `InRideAlert` effect plus `PlayBeepPattern`.
