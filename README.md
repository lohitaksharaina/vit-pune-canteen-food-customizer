# VIT Pune Canteen Order, Customiser & Chef Portal

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Design-Material%203-7D5260?logo=materialdesign&logoColor=white)](https://m3.material.io/)
[![Firebase](https://img.shields.io/badge/Backend-Firebase%20Firestore-FFCA28?logo=firebase&logoColor=black)](https://firebase.google.com/)

A modern Android application built for Vishwakarma Institute of Technology (VIT) Pune canteen operations. It combines customer menu browsing and dietary customisations with a real-time Kitchen Display System (KDS), counter bill generation, and kitchen queue capacity management.

---

## 📌 Key Features

- **🍽️ Live Menu & Dietary Customiser**:
  - Interactive ingredient exclusion chips (e.g. *No Onion*, *No Garlic*, *No Capsicum*).
  - Custom culinary preparation notes for the kitchen staff (e.g., *"Make extra crispy"*, *"Less spicy"*).
  - Spice level preferences (Mild, Normal, Extra Fiery).
  - Real-time Veg 🟢 / Non-Veg 🔴 filters and category tabs.

- **👨‍🍳 Kitchen Display System (Chef Mode)**:
  - Real-time Kitchen Order Ticket (KOT) workflow: `RECEIVED` → `ACCEPTED_PREPARING` → `READY_FOR_COUNTER` → `COMPLETED_SETTLED`.
  - Prominent high-contrast alert badges for excluded ingredients and chef notes.
  - Urgency indicators tracking elapsed ticket waiting time.
  - Filter tabs by status with live badge counts and search by order token or table.

- **⏱️ 30-Order Kitchen Capacity Guard**:
  - Dynamic safety valve monitoring active orders in preparation.
  - Automatically activates an amber **Waitlist Notice Banner** when active orders reach 30, preventing kitchen overload.

- **🧾 Digital Counter Bill Generation**:
  - Generates on-device digital bills with unique Order Token numbers (`#ORD-101`), table numbers, and timestamps.
  - Detailed itemization with specific customisations and 5% GST tax breakdown.
  - Counter payment workflow designed for physical cash / UPI payment at the counter.

- **🧹 7-Day Automatic Data Retention**:
  - Automatically prunes completed and settled counter orders older than 7 days to maintain database hygiene and privacy.

- **🔄 Multi-Role Dual Switching**:
  - Instant role switching between **Student Mode**, **Chef KDS**, and **Canteen Admin** for rapid on-site testing and operations.

---

## 🛠️ Tech Stack & Architecture

- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3 Design
- **Architecture**: MVVM (Model-View-ViewModel) with unidirectional data flow
- **Language**: Kotlin 2.0+
- **Asynchronous & Reactive**: Kotlin Coroutines & `StateFlow` / `Flow`
- **Cloud Backend**: Firebase Firestore (Real-time synchronization across ordering devices and kitchen terminals)
- **Local State**: StateFlow-backed repository with offline resilience
- **Testing**: JUnit 4 & Robolectric (Unit tests for authentication, capacity guards, and KDS state machine)

---

## 👥 Demo Accounts

The app includes built-in demo profiles with one-tap sign-in:

| Role | Username | Password | Access / Capabilities |
| :--- | :--- | :--- | :--- |
| **Student** | `student` | `vit_student` or `student` | Browse menu, customize dishes, cart, counter bill generation |
| **Chef / Cook** | `cook` (or `chef`) | `vit_cook` or `cook` | Kitchen Display System (KDS), accept & ready tickets, counter settlement |
| **Admin** | `admin` | `vit_admin` or `admin` | Full canteen dashboard, revenue metrics, stock availability toggles |

---

## 🚀 Getting Started

### Prerequisites
- [Android Studio Ladybug | 2024.2+](https://developer.android.com/studio) or newer
- JDK 17 or JDK 21
- Android SDK (API 36 / Android 15 support)

### Installation & Run

1. **Clone the repository**:
   ```bash
   git clone https://github.com/<YOUR_GITHUB_USERNAME>/vit-pune-canteen-customizer.git
   cd vit-pune-canteen-customizer
   ```

2. **Open in Android Studio**:
   - Open Android Studio, select **Open**, and choose the cloned repository folder.
   - Allow Gradle to sync dependencies.

3. **Configure Environment Keys**:
   - Copy `.env.example` to `.env`:
     ```bash
     cp .env.example .env
     ```
   - (Optional) Set your `GEMINI_API_KEY` in `.env` if leveraging AI assistance features.

4. **Build & Run**:
   - Select an Android emulator or connect a physical device via USB debugging.
   - Click **Run** (`Shift + F10`) or assemble the APK via terminal:
     ```bash
     ./gradlew assembleDebug
     ```
   - Generated APK location: `app/build/outputs/apk/debug/app-debug.apk`.

---

## 🧪 Running Unit Tests

Run the comprehensive Robolectric and unit test suite via Gradle:

```bash
./gradlew testDebugUnitTest
```

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
