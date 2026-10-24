# AETHER Launcher — Android Source Project

A clean Android/Kotlin starter project for the AETHER dark-emerald launcher design.

## What is included

- Android app source in `app/src/main/java/com/aether/launcher/MainActivity.kt`
- Android manifest, theme, app name, and launcher icon
- Root and app Gradle build files
- `.gitignore`
- GitHub Actions workflow at `.github/workflows/android.yml`
- A workflow that installs Java 17, Android SDK platform/build-tools 35.0.0, and Gradle 8.9, then builds and uploads a debug APK

## Build the APK from GitHub on a phone

1. Download this ZIP and extract it.
2. Create a **new empty GitHub repository** named `AETHER-Launcher` (do not upload this into the Zalith fork).
3. Upload the extracted **contents** so that `settings.gradle.kts`, `build.gradle.kts`, `app/`, and `.github/` are at the repository root. Do not upload the ZIP itself as the only file, and do not leave everything inside an extra nested folder.
4. Open the repository's **Actions** tab. The workflow named **Build AETHER Android APK** runs after a push. You can also open it and choose **Run workflow**.
5. When the run is green, open the run, find **Artifacts**, and download `AETHER-debug-apk`. Extract that artifact to get `app-debug.apk`.

## Current functionality and limitations

This is a starter UI, **not yet a functioning Minecraft Java launcher**. The player name is saved locally on the device. Play, Versions, and Settings are clearly labelled placeholders. This project does not yet download Minecraft files, install a Java runtime, render/run the game, provide game controls, or authenticate a Microsoft account.

## Build configuration

The project uses Android Gradle Plugin 8.7.3, Gradle 8.9, Kotlin Android plugin 2.0.21, Java 17, and Android SDK 35. The GitHub Actions workflow explicitly installs the required Gradle and Android SDK versions instead of relying on a preinstalled system Gradle.
