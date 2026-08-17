# Changelog

All notable changes to the PekSeries project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.8.3] - 2026-06-29
### Added
- **Optimized Network Layer:** Reimplemented `SeriesRepository` methods using `coroutineScope` and `async/await` to perform parallel API requests.
- **Request Throttling:** Implemented `chunked(5)` batching for TVMaze API requests to prevent `429 Too Many Requests` errors while maintaining high performance.

### Fixed
- **Episode Metadata:** Fixed an issue where episode release dates and times were showing as "TBA" in `DetailScreen` by correctly mapping `airdate` and `airstamp` fields in `SeriesRepository`.

## [1.8.2] - 2026-06-29
### Fixed
- **Google Authentication:** Fixed an issue where logging out and attempting to log back in via Google bypassed the account picker.
- Forced `GoogleSignInClient.signOut()` invocation during the logout process to clear the locally cached Google account session.

### Changed
- Updated `LoginViewModel` logout method signature to require `Context` for correct Google session clearing.

## [1.8.1] - 2026-06-29
### Added
- **Local Notification History:** Refactored `NotificationsViewModel` to observe data directly from the Room database, ensuring instant UI updates.
- **Custom Branding:** Configured custom app icon for push notifications via `AndroidManifest` metadata.

### Changed
- **Performance:** Decoupled notification history from Firestore, reducing network latency and dependency on internet connection for viewing history.

## [1.8.0] - 2026-06-29
### Added
- **Notification History:** Integrated Room Database to store all incoming FCM push notifications locally.
- **Persistence Layer:** Added `NotificationEntity` and `NotificationDao` to manage notification history.
- **Background Sync:** Updated `PekFirebaseMessagingService` to perform silent database writes on the IO thread upon receiving new messages.

## [1.7.3] - 2026-06-28
### Added
- **Accurate Watch Time Calculation:** Replaced hardcoded "45 minutes per episode" logic with dynamic calculation based on real `runtime` data fetched from the TVMaze API.
- **Data Model Update:** Added `runtime` field to `Episode` and `TvMazeEpisodeDto` models.

### Changed
- **Profile Stats Engine:** Refactored `ProfileViewModel` to compute total viewing time using real-time episode metadata, providing accurate user statistics.
- **Data Concurrency:** Optimized repository data fetching using `async/await` to process series data in parallel, improving overall UI responsiveness.

## [1.7.2] - 2026-06-28
### Added
- **Smart Filter Panel:** Introduced a `ModalBottomSheet` on the Home screen to encapsulate Genre, Type, and Year filters, significantly improving the screen's real estate.
- **Filter Indicators:** Added dynamic visual cues (a yellow dot and tinted `FilterList` icon) to inform the user when custom filters are actively applied.
- Added a "Clear All" button inside the filter sheet for quick resets.

### Changed
- Refactored `HomeScreen` layout to prioritize content discovery and reduce top-bar clutter.

## [1.7.1] - 2026-06-28
### Added
- **Watchlist Swiping:** Implemented `HorizontalPager` in `WatchlistScreen` allowing smooth swipe navigation between "Upcoming" and "Subscriptions" tabs.
- **Smart Shimmer Effect:** Added a custom lightweight `Modifier.shimmerEffect()`. The shimmer is displayed only on the initial load to improve UX.
- **Pull-to-Refresh:** Integrated Material 3 `PullToRefreshBox` allowing manual list updates without triggering the initial shimmer state.

### Changed
- **Network Optimization:** Refactored `WatchlistViewModel` to fetch upcoming episodes and user subscriptions concurrently using Kotlin Coroutines `async/await`, significantly reducing the total load time.

## [1.7.0] - 2026-06-24
### Added
- **Cloud Push Notifications:** Implemented `PekFirebaseMessagingService` to receive real-time push notifications using Firebase Cloud Messaging (FCM).
- **Notification Deep Linking:** Clicking on a new episode notification now automatically navigates the user directly to that specific show's `DetailScreen`.
- **FCM Topic Synchronization:** Added `syncSubscriptionsWithFcm()` in the repository to automatically sync the user's Firestore watchlist with FCM topics (e.g., `show_{id}`).
- **Runtime Permissions:** Added `POST_NOTIFICATIONS` permission request in `MainActivity` to fully support Android 13 (API 33) and above.
- **Serverless Backend:** Deployed a Firebase Cloud Function (`checkNewEpisodes`) via Google Cloud Shell that checks the TVMaze API hourly and triggers topic-based push notifications.

### Changed
- **Notification Architecture:** Migrated the episode release tracking from a client-side "pull" model (local background work) to a highly efficient server-side "push" model.
- Updated `PekSeriesApp` navigation and `MainActivity` to accept and process `showId` arguments from incoming intents.

### Removed
- Removed local alarm scheduling logic: completely deleted `AlarmReceiver.kt`, `PekAlarmManager.kt`, and `BootReceiver.kt`.
- Removed `SCHEDULE_EXACT_ALARM` permission from `AndroidManifest.xml` to strictly comply with updated Google Play Store policies.

## [1.6.0] - 2026-05-12
### Added
- **Multi-module Architecture:** Completely restructured the project into independent modules to improve build speed, scalability, and code isolation.
- Created core functional modules: `:core:network`, `:core:model`, `:core:ui`, and `:core:work`.
- Extracted UI screens into fully isolated feature modules: `:feature:search`, `:feature:watchlist`, `:feature:profile`, `:feature:home`, and `:feature:auth`.

### Changed
- The `:app` module now acts solely as an application shell, handling only DI initialization (`PekApplication`), the entry point (`MainActivity`), and global Navigation (`PekSeriesApp`).
- Fixed Hilt ViewModel injection crashes by migrating from `viewModel()` to `hiltViewModel()` across all Jetpack Compose screens.

### Removed
- Removed the `USE_EXACT_ALARM` permission from the manifest to strictly comply with Google Play Store policies (the app now safely relies solely on `SCHEDULE_EXACT_ALARM`).

## [1.5.0] - 2026-05-10
### Added
- **Dagger Hilt (Dependency Injection):** Migrated the app to a modern DI architecture. Created `PekApplication` (`@HiltAndroidApp`) and extracted `NetworkModule` to manage Retrofit and OkHttp clients.
- **KSP Support:** Migrated from the deprecated KAPT to KSP (Kotlin Symbol Processing), significantly improving project compilation time.
- **Developer Tools:** Integrated `Chucker` for on-device network inspection and `Timber` for smart, lifecycle-aware logging across the app.

### Changed
- **Core Architecture Refactoring:** Made `SeriesRepository` a `@Singleton` that receives dependencies automatically via Hilt, removing hardcoded instantiations and simplifying future testing.
- **ViewModels Update:** Refactored all ViewModels (e.g., `WatchlistViewModel`) to use `@HiltViewModel` and injected them into Jetpack Compose screens using `hiltViewModel()`.
- **Background Process Optimization:** Updated the system `AlarmReceiver` with `@AndroidEntryPoint` to support field injection.
- **Build System Modernization:** Cleaned up `build.gradle.kts` files, removing the deprecated `kotlinOptions` syntax. The project now strictly uses Java 17 via the unified `kotlin { jvmToolchain(17) }` block. Fully migrated plugin and dependency version management to `libs.versions.toml`.
- Updated `compileSdk` and `targetSdk` to 36.

### Removed
- The deprecated `NetworkClient` singleton class was completely removed as its responsibilities were taken over by `NetworkModule`.

## [1.4.1] - 2026-05-06
### Fixed
- **Critical Background Process Fix:** Fixed the "silent kill" (BroadcastReceiver Timeout) issue caused by the Android OS during background update checks. Implemented the `goAsync()` mechanism to safely keep the process alive.
- **API Rate Limit Bypass:** Added network request throttling (`delay(300)`) in the `SeriesRepository` checking loop to prevent `429 Too Many Requests` errors from TVMaze servers. The app no longer misses episode releases due to server limits.

## [1.4.0] - 2026-05-04
### Added
- **New Notification System:** Completely transitioned from `WorkManager` to exact alarms using `AlarmManager` (`setExactAndAllowWhileIdle`).
- Added a `BootReceiver` to automatically restore background checks after a device reboot.
- The interactive toggle on the Profile screen now physically cancels/starts system alarms via `PekAlarmManager.cancelAlarm()`.

### Changed
- Improved compatibility with aggressive power-saving modes (Doze Mode / App Standby), especially for Samsung devices (One UI).
- Updated the app manifest: added `SCHEDULE_EXACT_ALARM` and `RECEIVE_BOOT_COMPLETED` permissions.

### Removed
- Removed the deprecated `NewEpisodeWorker.kt` file and all related WorkManager dependencies from the UI.

## [1.3.0] - 2026-04-20
### Added
- **Watchlist Screen:** Added "Upcoming" and "Subscriptions" tabs.
- Implemented tracking logic for already sent notifications via `SharedPreferences` (the `notified_episodes` set) to prevent duplicate spam.
- Added background calculation for episode release times, converting `airstamp` (UTC) to the user's local timezone.

## [1.2.1] - 2026-04-10
### Fixed
- Fixed Firebase authentication error (ApiException: 10 / DeveloperError) by updating SHA-1 certificates and the `google-services.json` file.

## [1.2.0] - 2026-04-05
### Added
- **Firebase Integration:** Implemented Google Sign-In authentication.
- **Profile Screen:** Added a profile UI displaying the user's avatar, name, and email.
- Implemented a user statistics card (number of tracked shows, episodes, and hours spent watching).
- Added a Log Out button.

### Changed
- Updated the app's color theme (added `PekYellow` and `CardBg`).

## [1.1.0] - 2026-03-20
### Added
- **Database API Integration:** Configured REST API interactions using Retrofit.
- Integrated the **TVMaze API** to fetch accurate schedules and show metadata.
- Integrated the **TMDB API** to load high-quality posters and translate genres.

## [1.0.0] - 2026-03-01
### Added
- Initialized the Android project using Jetpack Compose.
- Implemented the MVI (Model-View-Intent) architectural pattern.
- Configured dependency management using BOM (Bill of Materials) files.
- Created basic navigation and the main screen framework (`MainActivity`).