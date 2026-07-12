# Path - Bible Study App

<div align="center">
  <img src="images/icon.png" alt="Path App Icon" width="200" height="200">
  
  [![Watch Promo Video](https://img.shields.io/badge/YouTube-Watch%20Promo%20Video-red?style=for-the-badge&logo=youtube)](https://youtube.com/shorts/H8feTTxo0aQ?feature=share)
</div>

A calm, mobile-first Bible study app that helps Christians stay focused and consistent through short daily study plans, gentle encouragement, and optional AI-powered insights.

## 📱 About

**Path** is an Android Bible study app designed to help new believers and long-time Christians build a consistent Bible reading habit. The app removes friction, reduces overwhelm, and encourages daily engagement through short, meaningful study sessions.

### Key Features

- **Daily Study Plans**: Sequential reading, book-based, topic-based, and devotional-style plans
- **Progress Tracking**: Reading progress, daily streaks, and gentle encouragement (Duolingo-inspired)
- **Notes & Highlights**: Write notes and highlight verses, all stored locally
- **Verse of the Day**: Daily featured verse on the home screen
- **Offline-First**: Works completely offline with no accounts required
- **Optional AI Integration**: Choose remote Ollama or private on-device Gemma 4 with LiteRT-LM (off by default)
- **Text-to-Speech**: Listen to Bible passages with customizable voices
- **Home Screen Widgets**: Verse of the Day, progress tracking, and encouragement widgets

## 🎯 Mission

Help Christians build a consistent Bible study habit by removing friction, reducing overwhelm, and encouraging daily engagement in short, meaningful sessions (3-10 minutes).

## 🛠️ Technology Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with Manual Dependency Injection
- **Database**: Room (local persistence)
- **Preferences**: DataStore
- **Navigation**: Android Navigation Component
- **Widgets**: Glance AppWidget
- **Network**: Retrofit (for Bible API and optional Ollama integration)
- **On-device AI**: Google LiteRT-LM with downloadable Gemma 4 E2B/E4B models

## 📋 Requirements

- Android 8.0 (API level 26) or higher
- Android Studio Hedgehog or later
- JDK 17 or later

## 🚀 Getting Started

### Prerequisites

1. Clone this repository
2. Open the project in Android Studio
3. Sync Gradle files

### Building the App

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install debug build to connected device/emulator
./gradlew installDebug

# Clean build artifacts
./gradlew clean
```

### Running Tests

```bash
# Run unit tests
./gradlew test

# Run instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest
```

## 📖 Usage

### First Launch

1. On first launch, the app will prompt you to set preferences:
   - Bible translation (default: WEB)
   - Daily study pace
   - Reminder time (optional)

2. The app defaults to a simple sequential Bible reading plan

### Daily Study Flow

1. Open the app to see the Verse of the Day and "Today's Study"
2. Tap "Start Reading" to view the assigned passage
3. Read the chapter, optionally add notes or highlights
4. Mark the study complete to update your streak
5. View your progress on the home screen

### Optional AI Setup

If you want to use AI-powered verse explanations:

1. Open Settings > AI Insights
2. Choose one provider:
   - **Remote Ollama**: Enter your Ollama server URL (e.g., `http://192.168.1.100:11434`), fetch models, and select one
   - **On-device Gemma 4**: Choose Gemma 4 E2B or E4B, download the model, then tap Test to initialize it with LiteRT-LM
3. Use "Explain with AI" in the reader screen, AI summaries in favorites/roadmap, or generated quizzes

On-device Gemma 4 downloads are large. E2B is about 2.6 GB and is the recommended mobile default; E4B is about 3.7 GB and may need more storage and memory.

**Note**: AI features are completely optional and the app works fully without them.

## 📸 Screenshots

<div align="center">
  <img src="screenshots/home.png" alt="Home Screen" width="350">
  
  <img src="screenshots/reader.png" alt="Reader Screen" width="350">
  
  <img src="screenshots/search.png" alt="Search Screen" width="350">
  
  <img src="screenshots/favorites.png" alt="Favorites Screen" width="350">
  
  <img src="screenshots/settings.png" alt="Settings Screen" width="350">
  
  <img src="screenshots/ai_summary.png" alt="AI Summary" width="350">
  
  <img src="screenshots/progress.png" alt="Progress Screen" width="350">
</div>

## 🏗️ Architecture

### Pattern: MVVM with Manual DI

- **UI**: Jetpack Compose with declarative UI
- **State Management**: StateFlow + Kotlin Flows
- **Navigation**: Android Navigation Component
- **Dependency Injection**: Manual DI in `PathApp.kt` (no Hilt/Dagger)
- **Database**: Room for local persistence
- **Preferences**: DataStore for settings

### Project Structure

```
app/src/main/java/com/path/app/
├── ui/                    # Presentation layer
│   ├── screens/          # Main screens with ViewModels
│   ├── components/       # Reusable UI components
│   └── theme/           # Material 3 theming
├── data/                  # Data layer
│   ├── local/           # Room database, DAOs, entities
│   ├── repository/      # Data abstraction
│   ├── remote/          # Retrofit API services
│   └── preferences/     # UserPreferences (DataStore)
├── domain/               # Domain models
│   └── model/          # Domain models (Verse, Chapter)
├── widget/              # Glance widgets
└── notifications/       # Study reminders
```

## 🔒 Privacy

**Path** is designed with privacy as a core principle:

- **No accounts required**: No login, no cloud sync
- **Local-only storage**: All data (notes, progress, preferences) stored on your device
- **Optional AI**: AI can stay fully on-device with Gemma 4 and LiteRT-LM, or connect only to your configured Ollama server
- **Analytics & Ads**: Uses Google Analytics for app usage insights and displays Google AdMob ads
  - **Note for developers**: When building from source, you can disable analytics/ads in debug builds by modifying `FirebaseManager.kt` and using test ad unit IDs

See [PRIVACY.md](PRIVACY.md) for detailed privacy information.

## 🤝 Contributing

We welcome contributions! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines on how to contribute to this project.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Bible text sourced from [bible-api.com](https://bible-api.com/)
- Inspired by Duolingo's habit-building approach
- Built with modern Android development best practices

## 🗺️ Roadmap

- [x] Sharing (share verse, export notes) - ✅ Implemented
- [x] Home screen widgets (Verse of the Day, Progress, Encouragement) - ✅ Implemented
- [x] Local backups (export/restore notes, progress, favorites) - ✅ Implemented
- [x] Text-to-Speech (TTS) - ✅ Implemented with customizable voices
- [ ] Plan switching (book-based, topic-based, devotional plans)
- [ ] Encrypted local backups
- [ ] iOS version
- [ ] Pre-recorded audio Bible integration (professional narrations)

---

**Path** - Building consistent Bible study habits, one day at a time.

