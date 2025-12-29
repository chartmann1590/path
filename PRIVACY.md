# Privacy Policy

**Last Updated**: January 2025

## Introduction

**Path** ("we", "our", or "the app") is committed to protecting your privacy. This Privacy Policy explains how we handle information when you use our Bible study application.

## Core Privacy Principles

Path is built with privacy as a fundamental design principle:

1. **No Accounts Required**: You don't need to create an account or provide any personal information to use Path.
2. **Local-Only Storage**: All your study data (notes, progress, preferences) is stored exclusively on your device.
3. **No Cloud Sync**: We don't sync, upload, or transmit your study data to any servers.
4. **Minimal Analytics**: We use Google Analytics to understand app usage patterns and improve the app experience.

## Information We Collect

### Google Analytics

Path uses **Google Firebase Analytics** to collect anonymous usage data to help us understand how the app is used and improve the user experience. This includes:

- **App Events**: Such as chapters read, searches performed, notes created, favorites added
- **Screen Views**: Which screens users visit
- **Feature Usage**: Which features are used most frequently
- **Crash Reports**: Information about app crashes and errors (via Firebase Crashlytics)

**What we don't collect via Analytics:**
- Personal identification information (name, email, phone number)
- Your notes, highlights, or study content
- Your reading progress or personal preferences
- Location data
- Contact information

Analytics data is collected anonymously and aggregated. It helps us understand general usage patterns but cannot be used to identify individual users.

### Google AdMob

Path displays **Google AdMob advertisements** to support app development. AdMob may collect:

- **Advertising ID**: Device advertising identifier (can be reset in device settings)
- **App Information**: App name and version
- **Device Information**: Device type, operating system version
- **Ad Interaction Data**: Whether ads were viewed or clicked

AdMob uses this information to show relevant ads and measure ad performance. You can opt out of personalized ads in your device's Google account settings.

### Information We Don't Collect

Path does **not** collect, store, or transmit:

- Personal identification information (name, email, phone number)
- Your notes, highlights, or study content
- Your reading progress or personal preferences (stored locally only)
- Location data
- Contact information

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

### Google Firebase

Path uses Google Firebase services:

- **Firebase Analytics**: Collects anonymous usage analytics (see "Information We Collect" above)
- **Firebase Crashlytics**: Collects crash reports and error logs to help us fix bugs
- **Firebase Performance Monitoring**: Monitors app performance metrics

Firebase is operated by Google and subject to [Google's Privacy Policy](https://policies.google.com/privacy). Analytics data is collected anonymously and cannot be used to identify you personally.

### Google AdMob

Path displays advertisements through Google AdMob. AdMob may collect advertising identifiers and device information to show relevant ads. AdMob is subject to [Google's Privacy Policy](https://policies.google.com/privacy) and [AdMob's policies](https://support.google.com/admob/answer/6128543).

You can opt out of personalized ads:
- **Android**: Settings → Google → Ads → Opt out of Ads Personalization

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

Path is designed to be safe for users of all ages. While we collect anonymous analytics data and display ads, we do not collect personal identification information. For users under 13 (or applicable age in your jurisdiction), we recommend parental supervision when using apps with advertising. We comply with applicable children's privacy regulations including COPPA.

## Changes to This Privacy Policy

We may update this Privacy Policy from time to time. We will notify you of any changes by updating the "Last Updated" date at the top of this policy. Since we don't collect contact information, we cannot notify you directly, but we encourage you to review this policy periodically.

## Your Rights

### Your Study Data

- **Access**: All your study data (notes, progress, preferences) is accessible through the app interface
- **Deletion**: Uninstall the app or clear app data to delete all local study data
- **Portability**: Export features allow you to export your notes
- **Correction**: You can edit or delete notes and preferences directly in the app

### Analytics and Advertising Data

- **Opt Out of Personalized Ads**: You can opt out in your device's Google account settings (Settings → Google → Ads → Opt out of Ads Personalization)
- **Reset Advertising ID**: You can reset your device's advertising ID in Android settings
- **Analytics**: Analytics data is collected anonymously and cannot be directly deleted, but it cannot be used to identify you personally

## Contact

If you have questions about this Privacy Policy or Path's privacy practices, please open an issue on our GitHub repository.

## Compliance

Path is designed to comply with:

- General Data Protection Regulation (GDPR)
- California Consumer Privacy Act (CCPA)
- Children's Online Privacy Protection Act (COPPA)
- Other applicable privacy regulations

We collect only anonymous analytics data and do not collect personal identification information. Your study data (notes, progress, preferences) remains stored locally on your device and is never transmitted to our servers.

---

**Summary**: 
- **Your Study Data**: All notes, progress, highlights, and preferences are stored locally on your device and never transmitted to our servers.
- **Analytics**: We use Google Firebase Analytics to collect anonymous usage data to improve the app. This data cannot identify you personally.
- **Advertising**: Path displays Google AdMob ads. You can opt out of personalized ads in your device settings.
- **No Personal Information**: We do not collect names, emails, phone numbers, or any personal identification information.

