# OpenCode Agent Instructions

## Project Overview
- Android app with Kotlin, single module (`:app`)
- Uses Gradle with Groovy DSL (`build.gradle`)
- Direct dependency versions (no version catalog)
- Gradle 8.4 with Android Gradle Plugin 8.3.0

## Build & Development Commands
- Build: `./gradlew build`
- Run tests: `./gradlew test` (unit) or `./gradlew connectedAndroidTest` (instrumented)
- Clean: `./gradlew clean`
- Assemble APK: `./gradlew assembleDebug` or `./gradlew assembleRelease`

## Key Configuration
- Compile SDK: 34 (Android 14)
- Min SDK: 24 (Android 7.0)
- Target SDK: 34
- Java compatibility: Java 11
- Kotlin code style: official (set in `gradle.properties`)
- Build tools: 34.0.0

## Architecture
- MVVM with Repository pattern
- Room database for persistence
- Navigation component with fragments
- Single activity with three fragments: Learning, Review, Overview

## Dependencies
Direct versions in `app/build.gradle`:
- AndroidX Core KTX 1.12.0, AppCompat 1.6.1, ConstraintLayout 2.1.4
- Material Design 1.10.0
- Room 2.6.1 with kapt compiler
- Navigation 2.6.0
- Lifecycle components 2.7.0
- Coroutines Android 1.7.3
- JUnit 4.13.2 for unit tests
- AndroidX Test for instrumented tests

## Important Notes
- No ProGuard/R8 minification enabled in release builds
- View binding enabled (`viewBinding true`)
- SDK path in `local.properties` (gitignored)
- Generated files in `.gradle/`, `build/`, `.idea/` are gitignored
- Chinese README describes 考研单词学习应用 (postgraduate exam vocabulary learning app)