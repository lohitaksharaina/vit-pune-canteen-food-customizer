# Antigravity Workspace Configuration: VIT Pune Canteen

## Project Overview
- **Name**: VIT Pune Canteen Order, Customiser & Chef Portal (`vit-pune-canteen`)
- **Type**: Android (Jetpack Compose / Kotlin) application with Firebase & Room / local sync.
- **Key Features**:
  - Live Grill & Buffet Menu customization (ingredient exclusion chips & chef notes).
  - Kitchen Display System (Chef Mode) & Counter Settlement.
  - 30-Order kitchen queue capacity guard with dynamic waitlist banner.
  - On-device digital counter bill generation with taxes and tokens.
  - 7-day automatic data cleanup for settled orders.

## Project Structure
- `app/src/main/java/`: Main application source code (UI, ViewModels, Repository, Models).
- `app/build.gradle.kts`: Android application build configuration.
- `firebase.json` & `firestore.rules`: Firebase configuration for cloud synchronization.
- `.env.example`: Template for Gemini API and cloud configuration keys.

## Development Guidelines
- Always verify Compose UI states with both Customer Mode and Chef Mode in mind.
- Maintain ingredient exclusion consistency across customer customization modal and chef ticket alert badges.
- Ensure capacity checks (active orders < 30) are enforced before order submission.
