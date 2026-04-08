# Bishun-Art — Project Rules

Chinese character stroke order learning app.
Package: `com.yourstudio.hskstroke.bishun`
Play Store: Hanzi Dictionary & Strokes

## App Architecture

- Jetpack Compose + MVVM (ViewModel + StateFlow)
- Offline-first, no backend
- HSK 3.0 (7 levels, 3,090 characters)
- Google Play Billing: `hanzi_pro_lifetime` (one-time INAPP purchase)
- Localization: en, zh, es, ja — all strings in `LocalizedStrings.kt`

## Free vs Pro Feature Gating

| Feature | Free | Pro |
|---|---|---|
| HSK 1 (313 chars) | Yes | Yes |
| HSK 2-7 (2,777 chars) | No | Yes |
| Dictionary search/favorites | Yes | Yes |
| Single character practice | Yes | Yes |
| Bulk practice (saved/history) | No | Yes |
| Bold brush width | No | Yes |
| Theme/accent colors | Yes | Yes |
| Widget / notifications | Yes | Yes |

## Code Conventions

- All user-facing strings go through `LocalizedStrings.kt` — never hardcode UI text
- Adding a new string: add to data class + all 4 language instances (en, es, ja, zh)
- Room DB: `Dispatchers.IO` for all DB and file I/O
- `viewModelScope.launch(Dispatchers.IO)` for Room/JSON/file operations
- SharedPreferences reads: wrap in `runCatching`

## In-App Engagement Features

### Share App & Rate App (Settings > Support card)

Every Android app from this project family MUST include:
1. **Share App** button in Account/Settings — uses `Intent.ACTION_SEND` with Play Store link
2. **Rate App** button in Account/Settings — uses `Intent.ACTION_VIEW` with `market://details?id=<package>`
3. Both buttons must have localized strings in all supported languages

Implementation pattern:
```kotlin
// Share
val sendIntent = Intent(Intent.ACTION_SEND).apply {
    type = "text/plain"
    putExtra(Intent.EXTRA_TEXT, accountStrings.shareAppMessage)
}
context.startActivity(Intent.createChooser(sendIntent, accountStrings.shareAppButton))

// Rate
val uri = Uri.parse("market://details?id=<package>")
val fallbackUri = Uri.parse("https://play.google.com/store/apps/details?id=<package>")
val rateIntent = Intent(Intent.ACTION_VIEW, uri)
runCatching { context.startActivity(rateIntent) }
    .onFailure { context.startActivity(Intent(Intent.ACTION_VIEW, fallbackUri)) }
```

Required strings per language:
- `shareAppButton` — button label
- `shareAppMessage` — full share text with Play Store link
- `rateAppButton` — button label

## Play Store

- Support email: `qq260316514@gmail.com`
- Privacy policy: hosted on GitHub
- Pro product ID: `hanzi_pro_lifetime`
- versionCode MUST increment before every Play-facing build

## Git

- Branch: `main` for releases
- Commit format: `<type>: <summary>`
- Types: feat / fix / refactor / docs / build / chore
- Never push with failing tests
- Push with timeout: `git -c http.lowSpeedLimit=0 -c http.lowSpeedTime=60 push origin main`
