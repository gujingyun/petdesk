# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is an Android desktop pet application (桌宠管理APP) - an AI-powered virtual pet that displays as a floating window on the device, featuring chat, task execution, and desktop organization capabilities.

## Build Commands

```bash
# Build debug APK
gradle assembleDebug

# Build release APK
gradle assembleRelease

# Clean build
gradle clean

# Run unit tests
gradle test

# Run single test class
gradle :app:testDebugUnitTest --tests "com.petdesk.domain.usecase.*"

# Run single test
gradle :app:testDebugUnitTest --tests "com.petdesk.domain.usecase.GetPetStateUseCaseTest"
```

## Architecture

The project uses **MVVM + Clean Architecture** with three distinct layers:

### Layer Structure
```
com.petdesk/
├── presentation/     # UI layer (Compose, ViewModels)
├── domain/          # Business logic (use cases, repository interfaces, models)
├── data/            # Data layer (repository implementations, data sources)
├── di/              # Hilt dependency injection modules
├── service/         # Android system services
└── utils/           # Utility classes
```

### Data Flow
- **Presentation → Domain → Data**: UI triggers use cases, which call repository interfaces
- **State Management**: ViewModels expose `StateFlow` for UI state
- **Dependency Injection**: Hilt modules provide dependencies at application level

### Key Components

**System Services:**
- `FloatingWindowService` - Manages the floating pet window overlay
- `DesktopAccessibilityService` - Handles accessibility for desktop organization features

**Domain Layer:**
- Repository interfaces in `domain/repository/`
- Use cases in `domain/usecase/`
- Models in `domain/model/` (PetState, PetPosition, PetSize, Permission)

**Data Layer:**
- Repository implementations in `data/repository/`
- Local data sources in `data/local/`

## Tech Stack

- **Language**: Kotlin 2.0.0
- **UI**: Jetpack Compose with Material 3
- **DI**: Hilt 2.51
- **Network**: Retrofit 2.11 + OkHttp 4.12
- **Database**: Room 2.6.1
- **Async**: Kotlin Coroutines + Flow
- **Images**: Glide 4.16
- **Animation**: Lottie 6.0

## Important Permissions

The app requires several special permissions:
- `SYSTEM_ALERT_WINDOW` - For floating window
- `FOREGROUND_SERVICE` - For background service
- `BIND_ACCESSIBILITY_SERVICE` - For desktop organization (defined in accessibility_service_config.xml)
- `PACKAGE_USAGE_STATS` - For app usage statistics
- `QUERY_ALL_PACKAGES` - For listing installed apps

## Current Development Status

- **W1 (Project Setup)**: Completed
- **W2 (Floating Window)**: Completed
- **W3 (Permission Management)**: In progress (10%)

See `TASKS.md` for detailed task breakdown.

## Code Patterns

- Use `@HiltViewModel` annotation for ViewModels
- Use `@AndroidEntryPoint` for activities/services
- Follow sealed class pattern for UI states
- Repository implementations should handle data source fallback logic

## Testing

Test files are located alongside source files with `Test` suffix:
- Unit tests in `src/test/java/com/petdesk/`
- Android tests in `src/androidTest/java/com/petdesk/`
