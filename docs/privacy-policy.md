---
title: Hanzi Dictionary & Strokes - Privacy Policy
updated: 2026-01-05
---

Hanzi Dictionary & Strokes (the "App", package name `com.yourstudio.hskstroke.bishun`) is an offline-first learning tool published by `ark_go`. This document explains what information is processed, how it is stored, and how you can control it.

## 1. Data We Collect / Process

| Category | Details | Storage |
| --- | --- | --- |
| Character resources | Local JSON stroke data bundled inside the APK. | Assets only; not user data. |
| Practice history | Characters practiced, stroke progress, and history needed to resume. | Stored locally using Android DataStore; never uploaded. |
| Settings | Board options (grid, stroke color, template visibility), language override. | Stored locally via DataStore. |
| Dictionary history | Recent lookups for quick access. | Stored locally via DataStore; never uploaded. |
| Reminder settings | Daily reminder toggle, time, and reminder rules (e.g. only when incomplete). | Stored locally via DataStore. |
| Purchase status | If you choose to buy Pro, the purchase is processed by Google Play. The app stores a local "entitlement" flag and last sync time so Pro works offline. | Local DataStore only. |
| Diagnostics | Google Play may provide aggregated crash/ANR diagnostics and install statistics so we can improve stability. | Google Play Console reports; not stored in our app database. |
| Support communications | If you email us for support, we receive your email address and message content so we can respond. | Your email provider + our inbox. |

We do **not** collect names, contacts, or precise location through the App itself. If you contact us via email, you voluntarily share contact details and message content.

## 2. How Data Is Used

- Resume your latest character/course session.
- Display learning statistics and practice history on your device.
- Remember board and demo preferences.
- Schedule optional daily practice reminders (local notifications) if you enable them.
- Unlock Pro features if you purchase them, and restore them automatically when you reinstall on the same Google account.
- Diagnose crashes and performance issues via aggregated Google Play reports.

The App stores your learning data locally on your device. If you choose to buy Pro, Google Play handles the purchase and the app may connect to Google Play to check purchase status. We do not operate our own server and we do not upload your practice history or dictionary usage.

## 3. Permissions

| Permission | Reason |
| --- | --- |
| `android.permission.POST_NOTIFICATIONS` (Android 13+) | Optional. Only requested when you enable daily practice reminders. Used to display local notifications. |
| `android.permission.RECEIVE_BOOT_COMPLETED` | Restore scheduled reminders after device reboot, app update, or time changes. |
| `com.android.vending.BILLING` | Used to start and restore optional Google Play in-app purchases (Pro). |
| `android.permission.INTERNET` | Used for optional Google Play purchase and restore flows. Learning features work offline. |
| `android.permission.ACCESS_NETWORK_STATE` | Used to detect connectivity for purchase and restore flows. |
| `android.permission.WAKE_LOCK` | Used by Android components for reliable background scheduling. |
| `android.permission.FOREGROUND_SERVICE` | Used by Android components for certain background tasks. |

## 4. Your Controls

- **Reset data**: Android Settings > Apps > (this app) > Storage > Clear data.
- **Daily reminders**: Disable in the App settings, or disable notifications for the App in Android system settings.
- **Support email**: You can ask us to delete past support communications by emailing `qq260316514@gmail.com`.

## 5. Children's Privacy

The App targets general audiences, including students. We do not knowingly collect personally identifiable information from children. Parents can clear practice data at any time via system settings.

## 6. Changes

If we add cloud sync, accounts, or online services, we will update this policy, bump the `updated` date, and highlight the changes inside the in-app Privacy screen.

## 7. Contact

Email: `qq260316514@gmail.com`

---

By installing or using Hanzi Dictionary & Strokes you agree to this policy. If you disagree, uninstall the App or clear its data.
