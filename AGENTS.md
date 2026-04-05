# OpenCode Agent Instructions

## Project Overview
- Android app with Kotlin, single module (`:app`)
- Uses Gradle with Kotlin DSL (`*.gradle.kts`)
- Version catalog in `gradle/libs.versions.toml`

## Build & Development Commands
- Build: `./gradlew build`
- Run tests: `./gradlew test` (unit) or `./gradlew connectedAndroidTest` (instrumented)
- Clean: `./gradlew clean`
- Assemble APK: `./gradlew assembleDebug` or `./gradlew assembleRelease`

## Key Configuration
- Compile SDK: 36 (Android 14)
- Min SDK: 24 (Android 7.0)
- Target SDK: 36
- Java compatibility: Java 11
- Kotlin code style: official (set in `gradle.properties`)

## Project Structure
- Main entry: `app/src/main/java/com/example/words/MainActivity.kt`
- Resources: `app/src/main/res/`
- Tests: `app/src/test/` (unit) and `app/src/androidTest/` (instrumented)

## Dependencies
Managed via version catalog (`gradle/libs.versions.toml`):
- AndroidX Core KTX, AppCompat, Activity, ConstraintLayout
- Material Design
- JUnit 4 for unit tests
- AndroidX Test for instrumented tests

## Important Notes
- Uses Android Gradle Plugin 9.1.0
- No ProGuard/R8 minification enabled in release builds
- Single activity architecture with edge-to-edge design
- Generated files in `.gradle/`, `build/`, and `.idea/` are gitignored
- Local properties (`local.properties`) contains SDK paths, gitignored