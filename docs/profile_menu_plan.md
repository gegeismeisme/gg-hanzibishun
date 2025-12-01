## Avatar Menu Functional Roadmap

| Menu Item | Purpose | Implementation Notes |
|-----------|---------|----------------------|
| Courses…  | Quick entry to curated lessons (e.g., “Basic Strokes”, “HSK1”). | Add a new `CoursesScreen`; menu click navigates to it. Courses reference local JSON so future bundles can add content offline. |
| Progress… | Shows learned characters, streaks, mistake stats. | Persist `PracticeState` history in Room/Datastore. Menu opens a dashboard with charts and “continue learning” CTA. |
| Dict…     | Bridge to the expanded dictionary (word definitions, radicals, examples). | Reuse `WordRepository`; build a searchable list with filters. When opened from menu, pass current query for deep-linking. |
| Help…     | Onboarding, gesture tips, FAQ. | Static Markdown rendered via Compose; include video/GIF tutorials for tracing gestures. |
| Privacy…  | Compliance hub (privacy policy, data usage statement). | Embed locally hosted HTML; provide toggles for analytics/diagnostics (when added). |
| Feedback… | Collect bug reports & feature requests. | Lightweight form writing to local cache + email intent or future backend API. Include log attach toggle. |

### Next Steps
1. **Routing:** Introduce a simple navigation host so each item can open a dedicated screen/bottom sheet.
2. **Persistence:** Add Datastore/Room layer to capture practice history for the “Progress…” card.
3. **UX polish:** Display badges on the avatar whenever new lessons or announcements exist; highlight required Play Store disclosures under “Privacy…”.
