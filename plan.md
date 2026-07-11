# Path App Feature Expansion Plan

## Phase 1: Design Refresh & Delight (1-2 days)

**Goal:** Modernize the visual identity and add micro-delight moments.

| Feature | What | Files |
|---------|------|-------|
| **Typography overhaul** | Add 4-5 custom type styles (display, headline, subtitle, body, caption) | `ui/theme/Type.kt` |
| **Reading themes** | Light, Sepia, Dark, AMOLED backgrounds in ReaderScreen | `UserPreferences.kt`, `ReaderScreen.kt`, `SettingsScreen.kt`, new `ui/theme/ReaderTheme.kt` |
| **Lottie celebrations** | Confetti animation on chapter completion, streak milestones | Add Lottie dependency, new `ui/components/CelebrationOverlay.kt` |
| **Haptic feedback** | Subtle haptics on favorite, chapter complete, quiz correct answer | Various screen files |
| **Screen transitions** | Shared element / slide transitions between screens | `PathApp.kt` (NavHost animations) |
| **Better streak card** | Animated gradient, glow effect, pulse on increment | `HomeScreen.kt`, `StreakScreen.kt` |

### New dependencies: `io.github.kevinnzou:compose-lottie` (Lottie for Compose)

### New files:
- `ui/components/CelebrationOverlay.kt` — Confetti/celebration Lottie animation
- `ui/theme/ReaderTheme.kt` — Reading background themes enum + colors

### Modified files:
- `ui/theme/Type.kt` — Full typography scale
- `ui/theme/Color.kt` — Sepia/AMOLED palette colors
- `ui/theme/Theme.kt` — Integrate reader theme support
- `data/preferences/UserPreferences.kt` — Add `readerTheme` preference
- `ui/screens/ReaderScreen.kt` — Apply reader background, trigger celebration on completion, haptic feedback
- `ui/screens/HomeScreen.kt` — Animated streak card, pulse effect
- `ui/screens/StreakScreen.kt` — Enhanced gradient card with glow
- `ui/screens/SettingsScreen.kt` — Reader theme picker section
- `ui/screens/QuizScreen.kt` — Haptic on correct/wrong, celebration on perfect score
- `ui/PathApp.kt` — Screen transition animations (slide/fade)
- `app/build.gradle.kts` — Add Lottie dependency

---

## Phase 2: Achievements & Badges (1-2 days)

**Goal:** Duolingo-style achievement system that rewards consistency.

### New files:
- `data/local/entity/AchievementEntity.kt`
- `data/local/dao/AchievementDao.kt`
- `data/repository/AchievementRepository.kt`
- `ui/screens/AchievementsScreen.kt`
- `ui/screens/AchievementsViewModel.kt`
- `ui/components/AchievementPopup.kt` — Overlay that shows when unlocked

### Modified files:
- `data/local/PathDatabase.kt` — Bump to v5, add AchievementEntity + DAO
- `ui/Screen.kt` — Add `achievements` route
- `ui/PathApp.kt` — Wire achievements screen + popup observer
- `ui/screens/HomeScreen.kt` — Badge icon in top bar
- `data/repository/PathRepository.kt` — Trigger achievement checks on chapter complete, note save, etc.
- `ui/screens/ReaderScreen.kt` — Trigger achievement check on completion
- `ui/screens/StreakScreen.kt` — Trigger achievement check on streak update

### Achievement definitions (20 total):
- **Reading**: First Chapter / Chapter Veteran (10) / Chapter Scholar (50) / Chapter Master (100) / Chapter Legend (500) / First Book / Biblical Scholar (full Bible)
- **Streak**: Spark (3) / Flame (7) / Blaze (30) / Inferno (100) / Eternal Fire (365)
- **Notes**: First Note / Scribe (10) / Author (50)
- **Quizzes**: First Quiz / Quiz Ace (5 perfect)
- **Discovery**: Seeker (first search) / Collector (10 favorites)

---

## Phase 3: Verse Highlights & Shareable Images (1-2 days)

**Goal:** Richer verse interaction and beautiful sharing.

### New files:
- `data/local/entity/HighlightEntity.kt` — verseId, color (enum)
- `data/local/entity/CollectionEntity.kt` — id, name, createdAt
- `data/local/entity/CollectionMemberEntity.kt` — collectionId, verseId
- `data/local/dao/HighlightDao.kt`
- `data/local/dao/CollectionDao.kt`
- `ui/components/VerseShareCard.kt` — Canvas composable for shareable image
- `ui/components/HighlightPicker.kt` — Color picker popup for verse highlighting

### Modified files:
- `data/local/PathDatabase.kt` — Bump to v6, add entities + DAOs
- `ui/screens/ReaderScreen.kt` — Long-press verse shows highlight + note + favorite menu
- `ui/screens/FavoritesScreen.kt` — Collection tabs, filter by collection
- `data/repository/PathRepository.kt` — Highlight + collection operations
- `data/preferences/UserPreferences.kt` — Default highlight color preference

---

## Phase 4: Reading Plans (2-3 days)

**Goal:** Guided study experiences beyond sequential reading.

### New files:
- `data/local/entity/ReadingPlanEntity.kt`
- `data/local/entity/PlanDayEntity.kt`
- `data/local/entity/PlanProgressEntity.kt`
- `data/local/dao/PlanDao.kt`
- `data/repository/PlanRepository.kt`
- `ui/screens/PlansScreen.kt` — Plan picker with previews
- `ui/screens/PlansViewModel.kt`
- `ui/screens/PlanDetailScreen.kt` — Day-by-day plan view
- `ui/screens/PlanDetailViewModel.kt`
- `app/src/main/assets/plans/` — JSON files for each plan

### Modified files:
- `data/local/PathDatabase.kt` — Bump to v7, add plan entities
- `ui/Screen.kt` — Add `plans`, `planDetail/{planId}` routes
- `ui/PathApp.kt` — Wire plan screens
- `ui/screens/HomeScreen.kt` — "Today's Plan" card with CTA
- `ui/screens/SettingsScreen.kt` — Reset plan option

### 4 built-in plans:
1. **New Believer** (30 days, key verses)
2. **Chronological** (365 days)
3. **Gospels Focus** (90 days, Matthew-John)
4. **Psalms & Proverbs** (150 days)

---

## Phase 5: Verse Memory Flashcards (1-2 days)

**Goal:** Spaced repetition memorization tool.

### New files:
- `data/local/entity/MemoryVerseEntity.kt` — verseId, box (1-5), nextReview, correctCount
- `data/local/dao/MemoryVerseDao.kt`
- `data/repository/MemoryRepository.kt`
- `ui/screens/MemoryScreen.kt` — Flashcard UI with flip animation + swipe gestures
- `ui/screens/MemoryViewModel.kt`
- `ui/components/FlashCard.kt` — Flip-animated card composable

### Modified files:
- `data/local/PathDatabase.kt` — Bump to v8, add memory entity
- `ui/Screen.kt` — Add `memory` route
- `ui/PathApp.kt` — Wire memory screen
- `ui/screens/ReaderScreen.kt` — "Add to Memory" option on verse long-press
- `ui/screens/HomeScreen.kt` — "Review Memory Verses" quick action
- `notifications/ReminderScheduler.kt` — Optional memory review notification
- `data/preferences/UserPreferences.kt` — Memory reminder toggle

### Leitner box intervals:
- Box 1 = daily
- Box 2 = every 3 days
- Box 3 = weekly
- Box 4 = every 2 weeks
- Box 5 = monthly

---

## Phase 6: Enhanced Stats & Heatmap (1-2 days)

**Goal:** Beautiful data visualization of reading habits.

### New files:
- `ui/components/ReadingHeatmap.kt` — GitHub-style contribution grid (365 days)
- `ui/components/WeeklyChart.kt` — Bar chart for day-of-week reading
- `ui/screens/StatsScreen.kt` — New dedicated stats dashboard
- `ui/screens/StatsViewModel.kt`

### Modified files:
- `data/repository/PathRepository.kt` — Add stats queries (daily reading counts, time-of-day analysis)
- `ui/Screen.kt` — Add `stats` route
- `ui/PathApp.kt` — Wire stats screen
- `ui/screens/StreakScreen.kt` — Link to full stats screen
- `ui/screens/HomeScreen.kt` — Mini heatmap preview card

---

## Execution Order & Estimated Effort

| Phase | Focus | Est. Time | Key Deliverable |
|-------|-------|-----------|-----------------|
| 1 | Design Refresh & Delight | 1-2 days | Beautiful animations, 4 reader themes, celebrations |
| 2 | Achievements & Badges | 1-2 days | 20 achievements with unlock popups |
| 3 | Highlights & Share Images | 1-2 days | Color highlights, collections, shareable verse cards |
| 4 | Reading Plans | 2-3 days | 4 built-in plans with daily tracking |
| 5 | Memory Flashcards | 1-2 days | Spaced repetition system with flip cards |
| 6 | Stats & Heatmap | 1-2 days | GitHub-style heatmap and charts |

**Total estimate: ~9-13 days** working in 10-15 minute chunks.
