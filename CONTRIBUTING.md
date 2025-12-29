# Contributing to Path

Thank you for your interest in contributing to Path! This document provides guidelines and instructions for contributing to the project.

## Code of Conduct

- Be respectful and considerate in all interactions
- Welcome newcomers and help them get started
- Focus on constructive feedback
- Respect different viewpoints and experiences

## How to Contribute

### Reporting Bugs

If you find a bug, please open an issue with:

1. **Clear title and description**
2. **Steps to reproduce** the bug
3. **Expected behavior** vs. **actual behavior**
4. **Device information** (Android version, device model)
5. **Screenshots** (if applicable)
6. **Logs** (if available, use `adb logcat`)

### Suggesting Features

We welcome feature suggestions! Please open an issue with:

1. **Clear description** of the feature
2. **Use case** - why would this be useful?
3. **How it fits** with Path's mission (calm, devotional, offline-first)
4. **Mockups or examples** (if applicable)

### Pull Requests

1. **Fork the repository**
2. **Create a feature branch** from `main`:
   ```bash
   git checkout -b feature/your-feature-name
   ```
3. **Make your changes** following our development guidelines
4. **Test your changes** thoroughly
5. **Commit your changes** with clear, descriptive commit messages
6. **Push to your fork** and open a Pull Request

## Development Guidelines

### Work Style

Path follows an incremental development approach:

- **Work in 10-15 minute chunks**
- **After each chunk, report**:
  1. What changed (files)
  2. What works (how to verify)
  3. What's next (next smallest slice)
- **Never introduce more than 1 dependency per chunk**
- **Prefer incremental changes over rewrites**

### Architecture Principles

1. **Offline-First**: All features must work without internet
2. **No Accounts**: No login or authentication systems
3. **Local Storage Only**: Use Room database or DataStore
4. **AI is Optional**: AI features must be off by default and fail gracefully
5. **Calm UX**: Keep UI peaceful and uncluttered

### Code Style

- **Language**: Kotlin
- **Formatting**: Follow Android Kotlin Style Guide
- **Naming**: Use descriptive names, follow Kotlin conventions
- **Comments**: Comment complex logic, not obvious code
- **No TODOs**: Prefer minimal working versions over placeholder TODOs

### Project Structure

```
app/src/main/java/com/path/app/
├── ui/                    # Presentation layer (Compose)
│   ├── screens/          # Screen composables + ViewModels
│   ├── components/       # Reusable UI components
│   └── theme/           # Material 3 theming
├── data/                  # Data layer
│   ├── local/           # Room database, DAOs, entities
│   ├── repository/      # Repository interfaces and implementations
│   ├── remote/          # Retrofit API services
│   └── preferences/     # DataStore preferences
├── domain/               # Domain models
│   └── model/          # Business logic models
└── widget/              # Glance widgets
```

### Adding a New Feature

1. **Create the screen** in `ui/screens/`
2. **Create the ViewModel** in the same directory
3. **Add repository methods** if needed in `data/repository/`
4. **Add database entities/DAOs** if storing new data
5. **Add navigation route** in `ui/Screen.kt`
6. **Wire up in PathApp.kt** (DI and navigation)

### Testing

- **Test manually** on a device or emulator
- **Test offline** functionality
- **Test AI features** (if applicable) with and without Ollama configured
- **Test edge cases** (empty states, network failures, etc.)

### Dependencies

- **Minimize dependencies**: Only add what's necessary
- **Use stable versions**: Avoid alpha/beta dependencies
- **Document why**: If adding a new dependency, explain why in PR description

## Build Order Priority

Features should be built in this order (per project rules):

1. ✅ Home screen (Verse of Day, Today's Study CTA, streak/progress)
2. ✅ Reader screen (passage text + completion)
3. ✅ Local persistence (progress/streaks/preferences)
4. ✅ Notes + highlights
5. ✅ Progress dashboard
6. ✅ Settings (translation/pace/reminders + AI config)
7. ⏳ Plan switching (sequential default + book/topic/devotional)
8. ⏳ Sharing (share verse, export notes)

When contributing, consider which features are highest priority.

## Commit Messages

Write clear, descriptive commit messages:

```
Good: "Add note editing functionality to NotesScreen"
Good: "Fix streak calculation bug when crossing month boundary"
Bad: "fix"
Bad: "updates"
```

## Pull Request Process

1. **Ensure your code follows** the development guidelines
2. **Test thoroughly** on a real device or emulator
3. **Update documentation** if you've changed behavior
4. **Keep PRs focused** - one feature or bug fix per PR
5. **Respond to feedback** promptly and constructively

### PR Checklist

- [ ] Code follows project style guidelines
- [ ] Changes work offline
- [ ] No new dependencies added (or justified if added)
- [ ] Tested on Android 8.0+ (API 26+)
- [ ] AI features fail gracefully if not configured (if applicable)
- [ ] No placeholder TODOs left behind
- [ ] Documentation updated if needed

## Questions?

If you have questions about contributing:

1. Check existing issues and PRs
2. Review [CLAUDE.md](CLAUDE.md) for development details
3. Review [prp.md](prp.md) for product requirements
4. Open a discussion issue

## License

By contributing, you agree that your contributions will be licensed under the same MIT License that covers the project.

---

Thank you for helping make Path better! 🙏

