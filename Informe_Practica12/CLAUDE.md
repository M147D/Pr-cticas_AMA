# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build the project
./gradlew build

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install debug APK to connected device
./gradlew installDebug

# Run unit tests
./gradlew test

# Run instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest

# Clean build artifacts
./gradlew clean
```

## Project Overview

Android attendance tracking system ("Sistema de Control de Asistencia - EPN") built with:
- **Kotlin 2.0.21** with Jetpack Compose (no XML layouts)
- **Firebase** (Auth, Firestore, Cloud Messaging)
- **Material Design 3**
- **Min SDK 24**, Target SDK 36

## Architecture

### Navigation & State Flow

The app uses a single-Activity architecture with Compose-based navigation managed in `MainActivity.kt:238-616` via the `AppNavigation()` composable. Screen state is tracked with a simple string-based `pantalla` variable.

Key navigation flow:
- `welcome` → `login`/`register` → `home` → `user`/`admin`/`rating`/`charts`/`animations`

### Access Control System

Role-based access control is implemented in `app/src/main/java/com/example/ama_practica09/accesscontrol/`:
- `AccessControlManager` - Validates screen access based on user role (USER/ADMIN)
- `ProtectedRoute` - Composable wrapper that enforces access control
- `SecureScreen` - Applies `FLAG_SECURE` to admin screens

Access validation returns `AccessResult.Granted`, `AccessResult.Denied`, or `AccessResult.RequiresAuth`.

### Session Management

Located in `app/src/main/java/com/example/ama_practica09/session/`:
- `SessionManager` - Handles session lifecycle with Firebase Auth and DataStore
- `SessionState` - Sealed class: `Active`, `Inactive`, `Loading`, `Error`
- Sessions persist across app restarts via DataStore

### Reactive Data Flow

Business logic uses Kotlin Coroutines with StateFlow/SharedFlow:
- `flow/PolicyEngine` - Combines multiple data sources reactively
- `flow/AuthSource`, `LocationSource`, `TimeSource` - Individual data source flows
- `rules/PolicyRules` - Composable business rules with AND/OR/NOT combinators

### Firebase Integration

Configured in `app/src/main/java/com/example/ama_practica09/firebase/`:
- `FirebaseConfig` - Singleton for Firebase initialization and FCM token management
- `MyFirebaseMessagingService` - Handles incoming FCM messages
- `NotificationService` - Local notification management with channels

Firebase is optional - the app gracefully handles missing/unavailable Firebase services.

### Data Layer

- `data/UsuarioRepository` - User data management
- `models/` - Data classes: `Usuario`, `Rating`, `RegistroAcceso`, `Ubicacion`
- `rating/RatingManager` - Firestore-backed rating system

## Key Patterns

1. **Singleton objects for services** - `FirebaseConfig`, `PolicyRules`, data sources
2. **Extension functions** - Used for data class utilities (see `MainActivity.kt:87-94`)
3. **Sealed classes for state** - `SessionState`, `AccessResult`, `AuthState`
4. **Composable business rules** - Rules in `rules/` return Boolean and can be combined

## Project Structure

```
app/src/main/java/com/example/ama_practica09/
├── accesscontrol/   # Role-based access control
├── animations/      # UI animations and transitions
├── auth/            # Login, Register, Google Sign-In
├── charts/          # Data visualization
├── data/            # Repositories
├── firebase/        # FCM and Firebase config
├── flow/            # Reactive data sources
├── models/          # Data classes
├── rating/          # Rating system
├── rules/           # Business logic rules
├── session/         # Session persistence
├── viewmodel/       # ViewModels
└── ui/theme/        # Compose theming
```

## Configuration Files

- `app/google-services.json` - Firebase configuration (required for Firebase features)
- `gradle/libs.versions.toml` - Dependency version catalog
- `app/proguard-rules.pro` - ProGuard rules (minification disabled by default)
