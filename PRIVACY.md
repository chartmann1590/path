# Privacy Policy

**Last Updated**: [Date]

## Introduction

**Path** ("we", "our", or "the app") is committed to protecting your privacy. This Privacy Policy explains how we handle information when you use our Bible study application.

## Core Privacy Principles

Path is built with privacy as a fundamental design principle:

1. **No Accounts Required**: You don't need to create an account or provide any personal information to use Path.
2. **Local-Only Storage**: All your data is stored exclusively on your device.
3. **No Cloud Sync**: We don't sync, upload, or transmit your data to any servers.
4. **No Tracking**: We don't collect analytics, usage data, or any information about how you use the app.

## Information We Don't Collect

Path does **not** collect, store, or transmit:

- Personal identification information (name, email, phone number)
- Usage analytics or app behavior data
- Device identifiers or advertising IDs
- Location data
- Contact information
- Any data that leaves your device

## Information Stored Locally

All data is stored **only on your device** using Android's local storage mechanisms:

### Data Types Stored Locally

- **Reading Progress**: Chapters and books you've completed, completion dates
- **Study Streaks**: Your daily study streak count and goal status
- **Notes**: Personal notes you write about Bible passages
- **Highlights**: Verses you've highlighted
- **Preferences**: 
  - Bible translation selection
  - Daily study pace settings
  - Reminder time preferences
  - Text-to-speech voice selection
- **AI Configuration** (if enabled):
  - Ollama server URL (your self-hosted server)
  - Selected AI model name
  - AI enabled/disabled status

### Storage Location

All data is stored in:
- **Room Database**: Reading progress, notes, highlights (`/data/data/com.path.app/databases/`)
- **DataStore Preferences**: User settings and preferences (`/data/data/com.path.app/shared_prefs/`)

This data is **private to your device** and is not accessible to other apps or services unless you explicitly share it.

## Optional AI Features

If you choose to enable AI features:

- **Self-Hosted Only**: AI connects only to your self-hosted Ollama server
- **No Third-Party Services**: We don't use cloud-based AI services
- **Your Data, Your Control**: Any data sent to your Ollama server is under your control
- **Optional**: AI features are completely optional and disabled by default

When you use AI features, verse text is sent to **your** Ollama server (the URL you provide). We have no access to this communication or any data processed by your server.

## Network Usage

Path uses network connectivity only for:

1. **Bible Text Fetching**: When Bible chapters aren't cached locally, the app fetches them from [bible-api.com](https://bible-api.com/) using public API endpoints. Only the Bible translation and chapter reference are sent (e.g., "John 3:16").
2. **Optional AI**: If enabled, verse text is sent to your self-hosted Ollama server.

No personal information is transmitted in either case.

## Third-Party Services

### Bible API

Path uses [bible-api.com](https://bible-api.com/) to fetch Bible text when not available locally. This is a public API that doesn't require authentication. We only send book, chapter, and verse references (e.g., "John 3:16"). Please review their privacy policy if you have concerns.

### Your Ollama Server

If you enable AI features, Path connects to your self-hosted Ollama server. Any data sent to your server is under your control and subject to your server's privacy practices.

## Data Deletion

Since all data is stored locally on your device:

- **Uninstall the App**: Uninstalling Path will delete all app data from your device
- **Clear App Data**: You can clear app data through Android Settings → Apps → Path → Storage → Clear Data
- **Export Before Deletion**: You can export your notes before uninstalling (feature in development)

## Children's Privacy

Path does not collect any information from anyone, including children. Since no data is collected or transmitted, we comply with all applicable children's privacy regulations.

## Changes to This Privacy Policy

We may update this Privacy Policy from time to time. We will notify you of any changes by updating the "Last Updated" date at the top of this policy. Since we don't collect contact information, we cannot notify you directly, but we encourage you to review this policy periodically.

## Your Rights

Since Path doesn't collect or transmit your data:

- **Access**: All your data is accessible through the app interface
- **Deletion**: Uninstall the app or clear app data to delete everything
- **Portability**: Export features (in development) will allow you to export your notes
- **Correction**: You can edit or delete notes and preferences directly in the app

## Contact

If you have questions about this Privacy Policy or Path's privacy practices, please open an issue on our GitHub repository.

## Compliance

Path is designed to comply with:

- General Data Protection Regulation (GDPR)
- California Consumer Privacy Act (CCPA)
- Children's Online Privacy Protection Act (COPPA)
- Other applicable privacy regulations

Since we don't collect, store, or transmit personal data, Path inherently complies with these regulations by design.

---

**Summary**: Path stores everything locally on your device. We don't collect, track, or transmit any information. Your Bible study data is private to you.

