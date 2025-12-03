# Hanzi Stroke Order UI Redesign Plan (v2025-12-03)
> Goal: ship a modern, legible UI that keeps our advanced tracing features while making navigation, personalization, and compliance flows obvious for Google Play review. This plan inventories the current system and breaks the redesign into auditable workstreams.

## 1. Current Architecture Snapshot
- **Single-screen Compose UI** – `app/src/main/java/com/example/bishun/ui/character/CharacterScreen.kt` (~122k LOC) renders *all* surfaces (search, canvas, practice badge, HSK/word cards, profile dialogs, help/privacy/feedback). Navigation is handled via transient dialogs triggered by `ProfileMenuAction` rather than dedicated screens.
- **State hub ViewModel** – `CharacterViewModel` owns character loading, render state, tracing quiz, course progression, Datastore-backed preferences/history, feedback logging, and language overrides. It also exposes flows for board settings and profile dialogs, resulting in a “god object” that complicates modular testing.
- **Data layer** – `data/characters` loads makemeahanzi JSON from assets/CDNs via `CharacterDataRepository`, caches to disk, and parses to `CharacterDefinition`. `WordRepository`/`HskRepository` serve dictionary & course metadata from `app/src/main/assets`. User data persists through `PracticeHistoryStore` and `UserPreferencesStore` (Datastore) plus `HskProgressStore` (SharedPreferences).
- **Rendering & tracing** – `hanzi/render` mirrors the hanzi-writer pipeline (mutation system, animation layers, `QuizActions`, `StrokeMatcher`), drawn inside `CharacterCanvas`. Pointer events (modifier `practicePointerInput`) map to board coordinates via `Positioner`.
- **Localization & docs** – `LocalizedStrings.kt` provides English, Spanish, Japanese copy for help/privacy/course text; Markdown docs under `docs/` (privacy, help-guide, stage0 inventory) act as source content but are not surfaced in-app beyond the dialogs.
- **Assets & theming** – Minimal XML resources; all theming occurs through `BishunTheme`. Adaptive icons and screenshots remain to-be-updated for Play release.

## 2. Redesign Objectives & Success Criteria
1. **Navigation clarity** – replace the single-column layout + modal dialogs with a multi-surface experience (tab bar or navigation rail) so that practice, dictionary, courses, progress, and settings are first-class routes.
2. **Focused tracing workspace** – redesign the canvas area to prioritize gesture guidance, board settings, and demo controls without crowding (responsive layout for portrait/tablet).
3. **Guided learning funnels** – expose HSK courses, practice streaks, and history in dedicated screens/cards to encourage progression and highlight offline capability for Play policy reviewers.
4. **Compliance surfaces** – refresh privacy/help/feedback UIs with branded typography/icons, highlight “offline by default” messaging, and streamline the email/log-sharing workflow.
5. **Scalable code structure** – break `CharacterScreen.kt` and `CharacterViewModel` into feature modules with state hoisted per route, enabling isolated previews/tests and future store-ready screenshots.
6. **Accessibility & localization** – ensure icon buttons have `contentDescription`, type ramps meet contrast guidelines, and the new layout supports locale overrides managed via Datastore.

## 3. Workstreams & Deliverables
### WS1 – Discovery & Information Architecture Audit
- **Inputs**: Existing flows (`CharacterScreen`, `ProfileActionDialog`, `LocalizedStrings`), docs/help content, Play checklist.
- **Tasks**: Map each feature to a future route; catalogue gestures, controls, and compliance copy; define target personas & primary tasks.
- **Deliverables**: IA diagram, feature inventory spreadsheet, prioritized storyboard, acceptance criteria for each new surface.

### WS2 – Navigation & Screen Model
- **Scope**: Introduce `NavigationHost` (e.g., `androidx.navigation.compose`) with destinations for *Practice*, *Courses*, *Progress*, *Dictionary*, *Support* (help/privacy/feedback), and *Settings*.
- **Implementation**: Split `CharacterRoute` into `HomeRoute`, `CoursesRoute`, etc.; move profile menu actions to persistent nav; define deep linking from search results to dictionary/Hsk screens.
- **Exit Criteria**: Navigating between routes preserves state (Datastore-backed), top-level actions accessible without dialogs, instrumentation tests for route transitions.

### WS3 – Design System & Layout Grid
- **Scope**: Define typography scale, color palette, spacing, and reusable components (buttons, cards, badges, dialogs). Create Canvas control tray, stat cards, and filters as composables under `ui/components`.
- **Tasks**: Audit `IconActionButton`, `PracticeSummaryBadge`, `CoursePlannerView` for reuse; align with Material 3 tokens; document variants in `docs/design-system.md`.
- **Deliverables**: Figma/Sketch kit, Compose theme extensions, storybook/previews for each component, updated `BishunTheme` with semantic colors.

### WS4 – Practice Canvas & Tracing Experience
- **Scope**: Redesign the `CharacterCanvas` layout, demo controls, board settings, and hint/HSK overlays for clarity on small screens.
- **Implementation**: Separate canvas view-state + pointer handling into `PracticeBoardScreen`; add responsive layout (two-column on ≥600dp); introduce progress ring/hint counter; ensure `CalligraphyDemo` controls don’t block strokes.
- **Dependencies**: Maintains `RenderState`/`QuizActions`; coordinate with `BoardSettings` state to persist selections.
- **Exit Criteria**: UX mockups approved, Compose implementation with preview tests, gesture instrumentation verifying start/move/end flows.

### WS5 – Courses & Progress Surfaces
- **Scope**: Transform HSK planner + badge into dedicated pages: course catalog with filters/tags, progress dashboard showing streaks/history, and course session HUD.
- **Tasks**: Move `CoursePlannerView`, `HskProgressView`, `PracticeHistorySection` out of dialogs; add grouping by `HskLevelSummary`; incorporate practice history stored by `PracticeHistoryStore`.
- **Deliverables**: `CoursesViewModel` (wrapping `HskRepository`, `HskProgressStore`), `ProgressScreen`, server-ready screenshots (per Play listing). Multi-select actions for marking learned symbols.

### WS6 – Dictionary, Help, Privacy, Feedback
- **Scope**: Build dedicated screens for dictionary (word card + TTS), help/onboarding (markdown renderer), privacy (policy + toggles), and feedback (multi-step flow with log preview).
- **Implementation**: Reuse `WordRepository` data and `TextToSpeechController`; parse `docs/help-guide.md`/`docs/privacy-policy.md` for content; restructure `FeedbackDialog` into screen with attachments and status indicator from `lastFeedbackSubmission`.
- **Exit Criteria**: All copy localized, email intents triggered from new screen, analytics toggles persisted via `UserPreferencesStore`, privacy policy accessible via `WebView` or markdown viewer.

### WS7 – Settings, Localization, & Personalization
- **Scope**: Centralize grid/stroke color/template, language override, analytics/prefetch toggles into a single settings route.
- **Tasks**: Provide previews of grid styles, stroke color pickers, language dropdown bound to `UserPreferencesStore`; add import/export for local data (practice history, feedback logs).
- **Deliverables**: `SettingsScreen`, instrumentation tests verifying Datastore writes, documentation for Play “Data safety” answers.

### WS8 – Implementation, QA, and Store Assets
- **Scope**: Harden build variants, add screenshot automation, and validate accessibility/performance before submission.
- **Tasks**: Add UI tests per route, integrate screenshot tests (Paparazzi/Shot), update adaptive icons + feature graphic + store copy, run `./gradlew bundleRelease` smoke tests, capture per-route videos for Play “App content” review.
- **Exit Criteria**: All tests green in CI, accessibility report (TalkBack, large font) recorded, release checklist updated, localization review done.

## 4. Timeline & Milestones (targeted sprints)
| Phase | Target Week | Milestone | Exit Criteria |
| --- | --- | --- | --- |
| WS1 Discovery | W49 | Feature inventory done | IA diagram signed off, backlog groomed |
| WS2 + WS3 | W50–W51 | Nav shell & design tokens in code | NavHost merged, new components previewed |
| WS4 | W52–W01 | Canvas redesign MVP | Practice route responsive, gesture tests pass |
| WS5 | W02 | Courses/progress screens | Dedicated routes live, datastore integration verified |
| WS6 | W03 | Support surfaces refreshed | Dictionary/help/privacy/feedback routes complete |
| WS7 | W04 | Settings & localization | Preferences route live, locale switch persists |
| WS8 | W05 | Release readiness | QA checklist, store assets, release bundle ready |

## 5. Dependencies & Risks
- **Monolithic code** (`CharacterScreen.kt`, `CharacterViewModel.kt`) complicates incremental rollout. *Mitigation*: create new packages (`ui/practice`, `ui/courses`, etc.) and migrate features gradually behind Nav routes.
- **Asset-driven data** (characters, HSK CSV, word JSON) must remain offline; ensure new screens read from existing repositories to avoid network regressions.
- **Feedback log / email reliance** – need fallback when no email client (already handled via toast). *Action*: keep share intent accessible from Support screen, document behavior for Play reviewers.
- **Localization debt** – only English/Spanish/Japanese strings exist. Plan includes i18n audit and ability to inject production copy before screenshots.
- **Testing coverage** – limited UI tests today. Introduce screenshot/unit tests per route plus pointer integration tests for `PracticeBoardScreen`.

## 6. Tracking & Next Steps
1. Approve this plan, assign owners per workstream, and create GitHub/YouTrack epics referencing the sections above.
2. Stand up a `ui` module structure (e.g., `ui/practice`, `ui/courses`, `ui/support`) and gradually split the current screen.
3. Produce Figma mocks for WS2–WS4 to unblock implementation Sprints; align them with `docs/help-guide.md` and `docs/privacy-policy.md` copy.
4. Kick off WS1 immediately by documenting current flows and capturing screenshots (API 26/30/34) for before/after comparisons.
5. Revisit this plan at the end of each sprint, updating statuses and adding new risks/findings directly in this file.

> **Reference files**: `MainActivity.kt`, `CharacterScreen.kt`, `CharacterViewModel.kt`, `LocalizedStrings.kt`, `data/characters/*`, `data/hsk/*`, `data/word/*`, `docs/*.md`. Keep this plan as the single source of truth for the redesign until production release.
