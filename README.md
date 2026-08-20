# PekSeries

Android TV-series tracker: follow shows, see what airs next, and get a push when
a new episode lands.

| | |
|---|---|
| **Language** | Kotlin |
| **UI** | Jetpack Compose, Material 3 |
| **Architecture** | Clean Architecture, MVVM, multi-module |
| **DI** | Hilt (KSP) |
| **Data** | Retrofit + Gson, Room, DataStore, Firestore |
| **Backend** | Firebase Auth, Firestore, Cloud Messaging, Cloud Functions |
| **Build** | Gradle 9.4 · AGP 9.2 · Kotlin 2.2 · Java 17 · minSdk 26 · targetSdk 36 |

See [ARCHITECTURE.md](ARCHITECTURE.md) for the module graph and the reasoning
behind it.

## Getting started

**1. Clone and add your secrets**

```bash
cp secrets.properties.example secrets.properties
```

Fill in `TMDB_API_KEY` from https://www.themoviedb.org/settings/api. The signing
keys can stay empty — release builds simply assemble unsigned without them.

**2. Add Firebase config**

Download `google-services.json` from the Firebase console into `app/`. It is
gitignored, so every developer supplies their own.

**3. Build**

```bash
./gradlew assembleDevDebug
```

## Build variants

Two flavors (`dev`, `prod`) times two build types gives four variants.

| Variant | Notes |
|---|---|
| `devDebug` | Day-to-day development. Chucker + verbose logging, `-DEV` version suffix. |
| `devRelease` | Minified dev build — use this to check R8 has not broken anything. |
| `prodDebug` | Production config, debuggable. |
| `prodRelease` | What ships to Google Play. Minified, shrunk, signed when keys are present. |

By default `dev` shares its applicationId with `prod`, because the Firebase
project has only one Android client registered. To install both side by side,
register `az.pekstudios.pekseries.debug` in the Firebase console, re-download
`google-services.json`, then set in `gradle.properties`:

```properties
pekseries.devSeparateApplicationId=true
```

## Versioning

`version.properties` at the repo root is the single source of truth.

```properties
VERSION_MAJOR=1
VERSION_MINOR=10
VERSION_PATCH=1
VERSION_BUILD=61
```

`versionCode = MAJOR*100_000_000 + MINOR*100_000 + PATCH*1_000 + BUILD`, which
is monotonic and readable. CI bumps `VERSION_BUILD`; the rest are bumped by hand
when cutting a release. Every change is recorded in [CHANGELOG.md](CHANGELOG.md).

## Secrets

Nothing secret is committed. Every key resolves **from the environment first**,
falling back to the gitignored `secrets.properties`, so CI exports variables
rather than writing credentials to disk.

| Key | Purpose |
|---|---|
| `TMDB_API_KEY` | TMDB catalogue requests |
| `PEKSERIES_KEYSTORE_FILE` | Upload keystore path |
| `PEKSERIES_KEYSTORE_PASSWORD` | Keystore password |
| `PEKSERIES_KEY_ALIAS` | Signing key alias |
| `PEKSERIES_KEY_PASSWORD` | Signing key password |

Also gitignored: `google-services.json`, `firebase-key.json`, `*.jks`.

## Common commands

```bash
./gradlew assembleDevDebug          # build for development
./gradlew test                      # all unit tests
./gradlew lintDevDebug              # lint
./gradlew assembleProdRelease       # minified release build
./gradlew bundleProdRelease         # AAB for Play Store
```

Verify a deep link without waiting for a real push:

```bash
adb shell am start -a android.intent.action.VIEW -d "pekseries://show/1"
```

## CI/CD

Two GitHub Actions workflows:

| Workflow | Trigger | What it does |
|---|---|---|
| [`pr.yml`](.github/workflows/pr.yml) | PR / push to `main` | assemble `devDebug`, run all unit tests, lint, then assemble `prodRelease` to prove R8 has not broken Gson/Room reflection |
| [`release.yml`](.github/workflows/release.yml) | tag `v*`, or manual | build a **signed** `bundleProdRelease`, verify the signature, upload to Google Play |

Secrets are exported as environment variables rather than written to disk —
build-logic reads the environment before `secrets.properties`, so no credential
lands on the runner. `google-services.json` and the keystore are the exceptions
(their tools can only read files); both are materialised from base64 secrets and
deleted in an `always()` step.

### Required repository secrets

Settings → Secrets and variables → Actions.

| Secret | Used by | How to produce it |
|---|---|---|
| `TMDB_API_KEY` | both | From your TMDB account |
| `GOOGLE_SERVICES_JSON_BASE64` | both | `base64 -i app/google-services.json \| pbcopy` |
| `PEKSERIES_KEYSTORE_BASE64` | release | `base64 -i upload.jks \| pbcopy` |
| `PEKSERIES_KEYSTORE_PASSWORD` | release | Keystore password |
| `PEKSERIES_KEY_ALIAS` | release | Signing key alias |
| `PEKSERIES_KEY_PASSWORD` | release | Signing key password |
| `PLAY_SERVICE_ACCOUNT_JSON` | release | Play Console → Setup → API access → service account JSON (paste whole file) |

### Cutting a release

1. Bump `VERSION_MAJOR` / `MINOR` / `PATCH` in `version.properties` and update `CHANGELOG.md`.
2. Tag and push:

```bash
git tag v1.11.1 && git push origin v1.11.1
```

`VERSION_BUILD` is stamped from the workflow run number, so every upload gets a
strictly increasing `versionCode`. The release defaults to the **internal**
track; use the manual `workflow_dispatch` trigger to choose another.

The `mapping.txt` is uploaded to Play alongside the bundle so crash reports are
deobfuscated, and kept as a build artifact for 90 days.

## Cloud Functions

The push backend lives in `functions/`. `checkNewEpisodes` polls the TVMaze
schedule hourly and sends a high-priority data message to the `show_{id}` topic
for any episode that just aired.

```bash
cd functions && npm install
firebase deploy --only functions
```

## Testing

92 unit tests across domain, data and feature modules. Shared fakes and the
`MainDispatcherRule` live in `:core:testing`, so a module under test only needs
`testImplementation(projects.core.testing)`.

```bash
./gradlew test
```
