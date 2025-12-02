---
title: Play Store Ready Checklist
owner: Bishun Studio
updated: 2025-12-02
---

## Product & UX

- [x] Offline stroke data bundle (`assets/characters`, `assets/learn-datas`)
- [x] Practice board with grids, hints, calligraphy demo toggle
- [x] Course resume chip (resume/skip/restart/exit)
- [x] Dictionary word card + TextToSpeech button
- [ ] Course catalog browser & onboarding flow
- [ ] Persistent practice history / streak tracking screen
- [ ] Help & Privacy entry points wired from avatar menu

## Compliance

- [x] Privacy policy drafted (`docs/privacy-policy.md`)
- [x] Help/onboarding copy (`docs/help-guide.md`)
- [ ] In-app Privacy dialog updated with new policy link
- [ ] Accessibility pass (content descriptions, TalkBack labels)
- [ ] Confirm permissions list (INTERNET optional, no location/contacts)

## Store Assets

- [ ] Adaptive app icon (`mipmap-anydpi-v26/ic_launcher.xml`)
- [ ] Feature graphic (1024×500)
- [ ] Screenshots (phone portrait × 8, tablet × 2)
- [ ] Short & full description text
- [ ] Release notes (initial v1.0.0)

## Engineering

- [ ] App bundle signed with release keystore
- [ ] VersionCode/VersionName bumped (e.g., 100000 / 1.0.0)
- [ ] Crash-free smoke tests on API 26–35
- [ ] Pre-launch report mitigations logged

## Submission Steps

1. Finish outstanding UX/compliance tasks above.
2. Update avatar menu so **Help** links to `help-guide` content and **Privacy** links to `privacy-policy`.
3. Run `./gradlew clean bundleRelease`, verify `app-release.aab`.
4. Fill Play Console listing (use docs for privacy + support contact).
5. Upload bundle, create test track release notes, answer content rating & data safety questionnaires, attach privacy link.

Keep this list updated as tasks complete. When every checkbox is ✅, we’re safe to submit to Closed/Beta tracks.
