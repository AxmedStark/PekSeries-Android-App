# Changelog

All notable changes to the PekSeries project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.11.4] - 2026-08-20
### Fixed
- **Cloud Function Crashed On Every Streaming Episode:** `/schedule` returns the show on `ep.show`, but `/schedule/web` nests it under `ep._embedded.show`. The function merged both feeds and read `ep.show.id` unconditionally, so the first web entry threw `TypeError: Cannot read properties of undefined (reading 'id')` and the outer catch aborted the whole hourly run. Confirmed in production logs at 05:12 and 06:12 on 2026-08-20, and reproduced against live TVMaze data: of 267 scheduled entries the old code processed 138 before crashing, leaving **129 streaming episodes that could never produce a notification**. The show is now resolved from either shape.
- **One Bad Send Aborted The Remaining Notifications:** `admin.messaging().send()` was awaited unguarded inside the loop, so a single failure discarded every notification still queued for that hour. Each send is now individually guarded and logged.

## [1.11.3] - 2026-08-20
### Fixed
- **Push Notifications Stopped Entirely After A Reinstall:** FCM binds topic subscriptions to the *registration token*, not to the account. A reinstall (or cleared data, or a periodic rotation) issues a new token that starts with **no** subscriptions, and FCM does not carry the old ones across. `onNewToken` only wrote a log line, and the sole re-subscribe path was an explicit sign-in whose result was discarded — so all topics were orphaned silently and permanently.
  - `TopicSynchronizer` now reconciles topics whenever the user becomes signed in, which covers every app start rather than only an interactive login. `subscribeToTopic` is idempotent, so repeating it is free.
  - `onNewToken` now triggers reconciliation on an application-scoped coroutine, so it survives the service callback.
  - The sign-in path logs a reconciliation failure instead of discarding it.
  - Reconciliation respects the push toggle: with notifications off, the reconciled state is "unsubscribed from everything".

### Added
- Six regression tests around `FcmTopicSynchronizer` covering signed-out no-op, disabled-preference handling, failure not propagating out of `Application.onCreate`, idempotent `start()`, and reconciliation firing on the signed-in transition.

## [1.11.2] - 2026-08-18
### Added
- **CI on GitHub Actions:** `pr.yml` runs on every pull request and push to `main` — assembles `devDebug`, runs all 93 unit tests, lints, and then assembles `prodRelease` so R8 breakage is caught in CI rather than after a store upload. Test results are published as a check, and reports are uploaded on failure.
- **Release Pipeline:** `release.yml` triggers on a `v*` tag (or manually with a track selector), builds a signed `bundleProdRelease`, verifies the signature, and uploads to Google Play with the R8 `mapping.txt` so crash reports deobfuscate. `VERSION_BUILD` is stamped from the run number, guaranteeing a strictly increasing `versionCode`.
- **CI Documentation:** README now lists every required repository secret, how to generate it, and the release procedure.

### Notes
- Secrets reach the build as environment variables, never files — build-logic reads the environment before `secrets.properties`. Verified locally by building with `secrets.properties` removed entirely and only `TMDB_API_KEY` exported. `google-services.json` and the keystore must be files, so they are decoded from base64 secrets and deleted in an `always()` cleanup step.
- Signing was verified end to end with a throwaway keystore: `apksigner` confirms the APK carries an APK Signature Scheme v2 signature.
- The bundle signature check uses `jarsigner -verify | grep "jar verified"`. Testing against a real unsigned bundle showed that neither the filename (an unsigned `.aab` keeps the same name) nor the exit code (`jarsigner` exits 0 and prints "no manifest.") distinguishes signed from unsigned.

## [1.11.1] - 2026-08-18
### Fixed
- **Push Toggle Froze The UI:** the switch renders from the preferences Flow, but `setPushEnabled` reconciled FCM topics *first* — a Firestore read plus one round trip per subscribed show — and only wrote the preference afterwards, while the switch was disabled throughout. The preference is now written first so the switch moves instantly; topic reconciliation continues in the background and rolls the preference back only if it fails.
- **Login Screen Flashed On Every Launch:** `isUserLoggedIn` was seeded with `false` and only corrected once the auth listener fired, so an already-signed-in user saw the login screen for a moment on every cold start. Introduced in 1.10.1 when the one-shot `currentUser` read was replaced by an `AuthStateListener`; now seeded synchronously via `AuthRepository.isSignedInNow()`.
- **Profile Name Off-Centre:** the edit button pushed the name to the left. A leading spacer matching the icon's 48dp touch target keeps the name centred with the button beside it. Long names now ellipsize instead of wrapping.
- **Log Out Button Floated Mid-Page:** moved into the Scaffold's `bottomBar`, so it stays pinned to the bottom instead of trailing the scrolling content.
- **Home Avatar Did Nothing:** `HomeScreen` already had an `onNavigateToProfile` handler on the avatar, but `PekSeriesApp` never passed one, so the default no-op ran. Tapping it now opens the Profile tab.
- **Filter Dropdowns Covered The Screen:** the year list renders nearly 40 entries with no height limit. Capped to five rows; Material's menu content already scrolls.
- **Home Greeting Ignored Profile Edits:** `HomeScreen` read `FirebaseAuth` directly, so a name or photo customised on the profile screen never appeared in the greeting. It now reads the same `UserProfileRepository` as the profile screen, and `:feature:home` no longer depends on `firebase-auth` at all.

## [1.11.0] - 2026-08-18
### Added
- **Test Suite:** 92 unit tests across 13 classes, covering the domain algebra, error mapping, DTO mappers, DAO behaviour, preference persistence, and every ViewModel including its failure paths. The project previously had zero real tests — only 24 generated stubs asserting `2 + 2 == 4`.
- **`:core:testing` Module:** shared hand-written fakes (`FakeShowRepository`, `FakeSubscriptionRepository`, `FakeAuthRepository`, `FakeUserProfileRepository`), `TestData` builders, and a `MainDispatcherRule`. A module under test now declares a single `testImplementation(projects.core.testing)`; the feature convention plugin wires it automatically.
- **Regression Tests For Fixed Bugs:** each significant bug from 1.10.x is pinned by a test — search debouncing to a single request, watchlist surfacing rather than swallowing failures, empty-vs-error being distinguishable, the push toggle reaching the topic layer, `CancellationException` being rethrown, and the watch-stats runtime fallback.
- **Documentation:** a real `README.md` (setup, variants, versioning, secrets, commands) and `ARCHITECTURE.md` covering the module graph, dependency rules, the error-handling model, the notification flow, and the AGP 9 convention-plugin caveat. Both include Mermaid diagrams.

### Changed
- **Robolectric:** upgraded to 4.16.1 for SDK 36 support, then pinned tests to `@Config(sdk = [35])` because emulating SDK 36 requires Java 21 while the toolchain is Java 17.
- **Test Configuration:** `failOnNoDiscoveredTests` is disabled in the library convention plugin, so an aggregate `./gradlew test` does not fail on modules that legitimately carry no unit tests.
- Library modules now set `testOptions.unitTests.isIncludeAndroidResources = true` for Robolectric.

## [1.10.1] - 2026-08-18
### Fixed
- **Push Notifications Did Nothing When Tapped:** `showNotification()` built a notification with no `setContentIntent`, so there was no `PendingIntent` anywhere in the app. Because the Cloud Function sends *data-only* messages, FCM never auto-displayed a notification either, meaning the `showId` extra `MainActivity` read was never populated by anyone. Deep linking was non-functional in every case, not merely on the foreground path.
- **Warm-Start Taps Were Ignored:** `MainActivity` only read the intent in `onCreate`. Added `launchMode="singleTop"` plus an `onNewIntent` override, so tapping a push while the app is already running now navigates.
- **Notifications Could Be Delayed For Hours:** the Cloud Function sent data-only messages at default priority, which Doze defers. Added `android: { priority: "high" }`.
- **Duplicate Push Notifications:** the function ran every 60 minutes but looked back 65, so anything airing in the 5-minute overlap was notified twice. The lookback now matches the schedule.
- **Push Toggle Did Nothing:** the profile switch wrote a `SharedPreferences` boolean that nothing ever read. Since delivery is driven by FCM topics, it now subscribes/unsubscribes every topic, persists only after the topic change succeeds, and is disabled while in flight.
- **Notifications Stacked Unboundedly:** each push used `Random.nextInt()` as its id. Now keyed on the show, so a newer episode replaces the previous entry.
- **Wrong Notification Icon:** used `ic_launcher_foreground`, which renders as a white blob in the status bar. Switched to the existing `ic_notification`.
- **Unscoped Database Write:** the FCM service wrote history from a bare `CoroutineScope(Dispatchers.IO)` that could be killed mid-insert when the process was torn down.
- **Stale Login State:** `LoginViewModel` read `auth.currentUser` once at construction, so the flag could drift after a token expiry. Now backed by an `AuthStateListener`.
- **Cloud Function Log Bug:** a template literal in double quotes never interpolated, logging a literal `${notificationsSent}`.

### Added
- **Profile Editing:** display name and photo can be changed and are stored in DataStore, overriding the Google/email values and surviving re-login. Clearing an override restores the provider's value. Name changes are mirrored to Firebase on a best-effort basis.
- **Email Auth Hardening:** new `AuthRepository` in the domain layer. Adds client-side email/password validation, a loading state, disabled controls while submitting, password reset, and email verification on signup. Firebase auth exceptions map to typed `DataError.Auth` cases, so users see "That email or password is not right" instead of a raw exception string.
- **Deep-Link Contract:** `pekseries://show/{showId}`, declared in the manifest and built by `:core:work` as an implicit intent, avoiding a `core -> app` dependency. Links tapped while signed out are held and honoured after login.

### Removed
- All 24 `ExampleUnitTest`/`ExampleInstrumentedTest` stub files. They asserted `2 + 2 == 4`, and were additionally triggering an internal lint FIR crash during parallel analysis.

## [1.10.0] - 2026-08-18
### Added
- **Domain Layer:** New `:core:domain` module holding `PekResult`, a typed `DataError`, the repository interfaces and `GetWatchStatsUseCase`. Feature modules now depend on `:core:domain` instead of `:core:network`, so a screen can be built against an interface rather than a concrete Retrofit/Firestore class.
- **Data Layer:** New `:core:data` module implementing those interfaces. `:core:network` is now purely Retrofit APIs and DTOs, with base URLs moved into `BuildConfig`.
- **Typed Error Handling:** Repositories return `PekResult` instead of swallowing exceptions. `DataError` distinguishes Network, RateLimited, NotFound, Unauthenticated and Server, so the UI can finally tell "nothing found" apart from "the request failed".
- **Shared State Components:** `PekLoadingView`, `PekErrorView` and `PekEmptyView` in `:core:ui`, with one `DataError.toUserMessage()` mapping so every screen words the same failure identically.
- **Firebase via Hilt:** `FirebaseModule` provides Auth, Firestore and Messaging, replacing `getInstance()` calls in repository field initialisers that made unit testing impossible.

### Fixed
- **Home Screen Rendered Nothing On Failure:** `HomeScreen`'s `when` ended in `else -> Unit`, so Loading, Empty and Error all drew a blank screen. The branch is now exhaustive with a retry action.
- **Watchlist Swallowed Every Error:** `loadData` caught exceptions into an empty block, silently leaving stale lists on screen. Failures are now surfaced with a retry.
- **Search Race Condition:** Replaced the manual `searchJob?.cancel()` + `delay(500)` with `debounce` + `flatMapLatest`, so a slow response for an earlier query can no longer land after a newer one and overwrite its results.
- **Coroutine Cancellation:** The old repository caught bare `Exception`, which swallowed `CancellationException` and broke structured concurrency. `runCatchingData` rethrows it.
- **Inconsistent Watch Stats:** Profile and Watchlist each computed hours differently — one summed real episode `runtime`, the other multiplied by a hardcoded 45 minutes — so the same user saw two different figures. Both now use `GetWatchStatsUseCase`.

### Removed
- **Dead Watched-Episodes Feature:** `markEpisodeAsWatched` had no callers, so nothing ever wrote to `watched_episodes`, yet `getWatchedEpisodeIdsFromFirebase()` was called *inside* the per-show loop — 15 identical Firestore queries per home screen load, all to compute an `isWatched` flag that was always false.
- **Superseded Firestore Notification Methods:** `saveNotification`, `getNotifications`, `clearNotifications` and the `PekNotification`/`EpisodeNotificationData` types, all unused since notification history moved to Room in 1.8.0.
- Deleted the 452-line `SeriesRepository` god object, including ~70 lines of commented-out dead code.

## [1.9.0] - 2026-08-18
### Added
- **Convention Plugins:** Introduced a `build-logic` included build exposing `pekseries.android.{application,library,library.compose,feature,hilt,room,firebase}`. Module build files dropped from ~35 lines of duplicated `android {}` config to under 15 lines of real dependencies.
- **Build Flavors:** Added a `dev`/`prod` flavor dimension. `dev` carries a `-DEV` versionName suffix and an `ENVIRONMENT` BuildConfig field; an opt-in `pekseries.devSeparateApplicationId` property enables side-by-side installs once a `.debug` client is registered in Firebase.
- **Externalised Versioning:** Moved versionCode/versionName into a committed `version.properties`, so CI can cut a release by editing one line instead of rewriting Kotlin.
- **Secret Management:** Replaced `local.properties` with `secrets.properties` (gitignored) plus a committed `secrets.properties.example`. Every key resolves from the environment first, so CI never writes a credential to disk.
- **Release Signing:** Added an environment-driven signing config. When credentials are absent the config is skipped, so release builds still assemble unsigned for local verification rather than failing.
- **R8 Configuration:** Wrote real ProGuard rules, with module-owned `consumer-rules.pro` in `:core:model`, `:core:network` and `:feature:notifications` so keep rules travel with the code they protect.

### Fixed
- **Release Build Never Compiled:** `NetworkModule` imports `ChuckerInterceptor` unconditionally, but `:core:network` declared Chucker as `debugImplementation` only. Added the matching `releaseImplementation(chucker-release)` no-op artifact. Prior to this, every release variant failed at `compileReleaseKotlin`.
- **R8 Would Have Silently Broken All API Parsing:** Minification was enabled against an empty rules file. Every DTO matches JSON by field name with no `@SerializedName`, so R8 renaming would have yielded objects full of nulls — visible only in release. Verified against `mapping.txt`: 2320 DTO members retained their names, zero Gson-facing members renamed.

### Changed
- **Build Performance:** Enabled the Gradle configuration cache and dropped the deprecated `configureondemand`. Incremental `assembleProdRelease` went from 1m47s to 1s on a warm cache.
- **Dependency Hygiene:** Removed unused Media3/ExoPlayer, AppCompat, Material and WorkManager entries from the version catalog. Renamed the opaque `library`/`library-no-op` Chucker aliases to `chucker-debug`/`chucker-release`.
- **Type-safe Project Accessors:** Modules are now referenced as `projects.core.model` rather than `project(":core:model")`.
- **Scoped .gitignore:** Replaced a blanket `*.json` rule, which would have swallowed Room schemas and CI configs, with targeted `google-services.json` entries.

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