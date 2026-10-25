# Product-catalog

This repo contains a Jetpack Compose product catalog prototype built around the requirements in `CLAUDE.md`.

## Android module

The app is organized under `android/` with the structure required for a single-module PoC:

- `android/app`: Compose app, ViewModels, UI screens, navigation, repository setup
- `ui/theme`: central look-and-feel definitions
- `ui/components`: reusable loading/error/empty states
- `ui/home`, `ui/search`, `ui/detail`, `ui/enquiry`: screen-level UI and state

## Local run setup

1. Install Android Studio and the Android SDK.
2. Open the `android` folder in Android Studio.
3. Launch the app with the Android emulator or device.

## Notes

- Network calls use `http://10.0.2.2:3000` for emulator debug builds.
- Cleartext traffic is allowed for debug builds only.
