# Whisky Tasting Journal

A personal whisky tasting journal for Android, built with Kotlin and Jetpack Compose. Record tasting notes, distillery details, flavor profiles, and visualize them with an animated radar chart.

## Screenshots

*Coming soon*

## Features

- **Tasting List** — Browse all your tastings with name, distillery, region, date, price, and average score
- **Add / Edit / Delete** — Full CRUD with form validation
- **Distillery Autocomplete** — 100 distilleries from Scotland, Ireland, USA, Japan, and more with auto-filled country and region
- **Flavor Profile Sliders** — Rate sweetness, smokiness, fruitiness, spice, body, and finish (0–10)
- **Tasting Wheel** — Animated radar/spider chart on the detail screen
- **Search & Sort** — Filter by name, distillery, or notes; sort by date, name, distillery, or rating
- **Material 3 Theme** — Warm amber/whisky-inspired palette with dark mode support
- **Local Storage** — Room database with migrations, data persists across app restarts
- **Date Picker** — Material 3 date picker for tasting dates

## Architecture

```
UI (Compose) → ViewModel → Repository → DAO → Room Database
```

- **MVVM** pattern with `StateFlow` for reactive UI updates
- **Room** for local persistence with type converters for `LocalDate`
- **Navigation Compose** with type-safe sealed class routes
- **Single Activity** architecture

## Tech Stack

| Component       | Technology                     |
|-----------------|--------------------------------|
| Language        | Kotlin                         |
| UI              | Jetpack Compose + Material 3   |
| Architecture    | MVVM                           |
| Database        | Room                           |
| Navigation      | Navigation Compose             |
| Min SDK         | API 26 (Android 8.0)           |
| Build           | Gradle 8.5, AGP 8.3.2          |

## Project Structure

```
app/src/main/java/com/example/whiskytastingjournal/
├── MainActivity.kt
├── WhiskyApp.kt
├── data/
│   ├── converter/Converters.kt
│   ├── dao/TastingDao.kt
│   └── database/TastingDatabase.kt
├── model/
│   ├── Distillery.kt
│   └── TastingEntry.kt
├── navigation/
│   ├── AppNavigation.kt
│   └── NavRoutes.kt
├── repository/
│   └── TastingRepository.kt
└── ui/
    ├── TastingViewModel.kt
    ├── components/
    │   ├── DistilleryField.kt
    │   └── TastingWheel.kt
    ├── screens/
    │   ├── AddTastingScreen.kt
    │   ├── EditTastingScreen.kt
    │   ├── TastingDetailScreen.kt
    │   └── TastingListScreen.kt
    └── theme/
        ├── Color.kt
        ├── Theme.kt
        └── Type.kt
```

## Getting Started

1. Clone the repository
2. Open in Android Studio (Hedgehog or newer recommended)
3. Wait for Gradle sync to complete
4. Run on an emulator or device with API 26+

## Future Roadmap

- **Cloud Sync** — Firebase or REST API for cross-device sync
- **Data Export** — CSV/JSON export of tasting history
- **AI Analysis** — Automatic flavor extraction from tasting notes
- **Photo Attachment** — Capture bottle and label photos
- **Statistics Dashboard** — Aggregated stats across all tastings

## License

This project is open source and available under the [MIT License](LICENSE).
