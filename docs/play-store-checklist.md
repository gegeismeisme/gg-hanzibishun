---
title: Play Store Ready Checklist
owner: Bishun Studio
updated: 2025-12-25
---

## Product & UX

- [x] Offline stroke data bundle (`assets/characters`, `assets/learn-datas`)
- [x] Practice board with grids, hints, calligraphy demo toggle
- [x] Demo playback (once/loop) + practice correctness checks
- [x] Course catalog browser + per-level progress
- [x] Practice history / streak tracking screen
- [x] Help & Privacy entry points available in-app
- [ ] Final copy review (English/Spanish/Japanese)

## Monetization

- [x] No ads, no in-app purchases (fully free tool)

## Compliance

- [x] Privacy policy drafted (`docs/privacy-policy.md`)
- [ ] Host privacy policy on a public URL (required by Play Console)
- [ ] Update Play Console Data safety form
- [ ] Accessibility pass (content descriptions, TalkBack labels)
- [ ] Confirm permissions list (INTERNET)

## Store Assets

- [ ] Adaptive app icon (`mipmap-anydpi-v26/ic_launcher.xml`)
- [ ] Feature graphic (1024×500)
- [ ] Screenshots (phone portrait × 8, tablet × 2)
- [ ] Short description + full description
- [ ] Release notes (v1)

## Engineering

- [ ] App bundle signed with release keystore
- [ ] VersionCode/VersionName bumped
- [ ] Smoke tests on API 26/30/34 (real device + emulator)
- [ ] Pre-launch report issues triaged

## Submission Steps

1. Finish the outstanding checklist items above.
2. Run `./gradlew clean bundleRelease`, verify `app-release.aab`.
3. Upload to Play Console (internal/closed testing first).
4. Fill listing (screenshots, descriptions), content rating, Data safety, and link the privacy policy URL.
5. Promote from testing to production when metrics are stable.

Keep this file updated as tasks complete.
