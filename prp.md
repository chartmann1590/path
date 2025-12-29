# 📘 Product Requirements Plan (PRP)

## Product Name (Working Title)
**Path**

---

## One-Sentence Description
A calm, mobile-first Bible study app that helps Christians stay focused and consistent through short daily study plans, gentle encouragement, and optional AI-powered insights.

---

## Mission
Help new believers and long-time Christians build a consistent Bible study habit by removing friction, reducing overwhelm, and encouraging daily engagement in short, meaningful sessions.

---

## Target Audience

### Primary Users
- New believers who want structure and guidance
- Long-time Christians who struggle with consistency or focus

### User Pain Points
- Difficulty staying focused during Bible study
- Losing motivation after a few days
- Feeling overwhelmed by where to start
- Inconsistent daily habits

### Usage Context
- Short study sessions (3–10 minutes)
- Mostly on **Android phones during the day**
- Personal, devotional, distraction-free use

---

## Platform
- **Android mobile app**
- Mobile-first
- Offline-first
- No accounts or login required

---

## User Journey / Flow

1. User downloads the app from the Google Play Store
2. On first launch, user sets preferences:
   - Bible translation
   - Daily study pace
   - Reminder time
3. App defaults to a **simple sequential Bible reading plan**
4. Each day:
   - User opens the app
   - Sees Verse of the Day and “Today’s Study”
   - Reads the assigned passage
   - Optionally writes notes or highlights verses
   - Marks the study complete
5. App:
   - Logs progress
   - Updates streaks
   - Shows gentle encouragement
6. User can switch study plans at any time
7. Optional: share verses or export notes

---

## Core Features (v1)

### 1. Daily Study Plans

**Default Plan**
- Simple sequential plan: Bible in order
- One chapter per day (configurable pace)

**Alternate Plans**
- **Book-based:** user selects a book (e.g., John, Psalms)
- **Topic-based:** curated topics (faith, anxiety, forgiveness, gratitude)
- **Devotional-style:** short passage + reflection prompt  
  - Optional AI explanation if enabled

---

### 2. Progress & Motivation
- Reading progress tracking (chapters + books completed)
- Daily streaks
- Daily goals
- Gentle encouragement messages (Duolingo-inspired, non-pushy)

---

### 3. Notes & Highlights
- Highlight verses
- Write notes linked to specific passages
- Fully local storage

---

### 4. Progress Dashboard
- Chapters completed
- Books completed
- Current study plan
- Streak and goal status

---

### 5. Verse of the Day
- Daily featured verse on home screen
- Shareable via system share

---

## Optional AI (User-Controlled, OFF by Default)

### AI Philosophy
- Privacy-first
- Fully optional
- User-owned infrastructure

### Configuration
- User can enable AI in settings
- User provides:
  - Ollama server URL (e.g., `http://LAN-IP:11434`)
  - Model name (string)

### AI Capabilities (when enabled)
- Explain selected Bible passages
- Generate summaries or reflection prompts

### AI Constraints
- App must function fully without AI
- AI UI must be hidden or disabled if not configured
- AI layer must fail gracefully (no crashes)

---

## Data Storage (Local-Only)

Stored on device:
- Reading progress (chapters/books completed)
- Streaks and daily goals
- Notes and highlights
- Study preferences:
  - Translation
  - Pace
  - Reminder time
- AI configuration:
  - Ollama URL
  - Model name
  - Enabled flag

---

## Accounts & Authentication
- **No accounts**
- **No login**
- No cloud sync in v1

---

## Sharing
- Share verse text via system share
- Export notes (text format)

---

## Design & UX Vibe
- Modern app-store polish (not “church bulletin” style)
- Calm, devotional, peaceful tone
- Slightly gamified:
  - Streaks
  - Gentle encouragement
- Optimized for short daily sessions
- Minimal distractions

---

## Inspirations
- **Duolingo** (habit-building, streaks, motivation)
- Meditation / devotional apps (calm, focused UX)

---

## Future Feature Ideas
- iOS version
- Encrypted local backups
- Widgets (Verse of the Day, streak)
- Audio Bible integration
- Adaptive study plans
- Optional small-group mode (opt-in)
- Advanced AI study prompts
- Additional translations

---

## Non-Goals (v1)
- No social feeds or comments
- No accounts or community features
- No ads
- No cloud dependency

---

## Success Criteria (v1)
- App launches quickly and works offline
- User can complete a daily study in under 5 minutes
- Progress and streaks persist reliably
- AI features remain optional and stable
