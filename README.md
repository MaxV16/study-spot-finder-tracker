# Study Spot Finder & Tracker

Maksim Karakulin · 20106742 · Mobile Application Development 1

An Android app for saving the places you actually get work done in. Each spot has a
name, description, category, coordinates and an optional photo. Spots are stored per
account as JSON in the app's private storage, so they survive a restart and two users
on the same device never see each other's lists.

## What it does

- Add, view, edit, delete and delete-all study spots.
- Search by title as you type, filter with category chips, sort A–Z.
- Attach a photo from the device (OpenDocument, persistable permission).
- Sign up / log in / log out. Passwords are PBKDF2-hashed with a random salt and each
  account gets its own `spots_<userId>.json` file.
- See every spot on a Google Map, tap a marker for an info window, add a spot by
  long-pressing the map or by pressing the current-location button.
- Individual detail screen with the photo, description, category and coordinates.
- Navigation drawer (Spots, Map, Settings, About, Log out).
- Dark mode that persists, Material 3 widgets, splash screen and screen transitions.
- Swipe a row to delete, with an Undo snackbar that restores the same id.

## Setup

1. Android Studio with JDK 17+ and Android SDK 37 (`compileSdk 37`, `targetSdk 37`,
   `minSdk 24`).
2. Get a Google Maps API key and enable **Maps SDK for Android** for that key in the
   Google Cloud console.
3. Add the key to `local.properties` in the repo root (this file is gitignored):

   ```properties
   sdk.dir=C\:\\Users\\<you>\\AppData\\Local\\Android\\Sdk
   MAPS_API_KEY=AIza...your-key-here
   ```

   The Gradle script reads that value and injects it through the `MAPS_API_KEY` manifest
   placeholder, so the key never lands in version control.
4. Build and install:

   ```bash
   ./gradlew :app:assembleDebug
   ```

   Or open the folder in Android Studio and press Run.

Without a key the app still builds and every other screen works; the map only shows a
blank tile grid.

## Architecture

```
app/src/main/java/org/setu/studyspotfinder/
  StudySpotApp.kt              Application class, Timber + theme setup
  models/                      StudySpot (with validate()), User
  data/                        repository + storage + auth + prefs
    StudySpotRepository.kt     interface the presenter talks to
    JsonStudySpotRepository.kt JSON implementation
    StudySpotStore.kt          loads/saves the concrete json file
    AuthManager.kt             signup/login/logout, PBKDF2 hashing, session
    UserStore.kt               users.json
    AppData.kt                 wires the current user's store together
    ThemePrefs.kt              dark mode preference
    LocationHelper.kt          fused location for the map screen
  ui/                          activities, adapter, RecyclerView listener
    main/MainContract.kt       MVP view + presenter interfaces
    main/MainPresenter.kt      search, category filter, sort, delete (no Android imports)
    auth/AuthActivity.kt       login + signup tabs
    map/MapActivity.kt         markers, long-press add, current location
    detail/SpotDetailActivity.kt
    settings/SettingsActivity.kt
```

The list screen is MVP: `MainActivity` only draws, `MainPresenter` owns the query, the
category filter, the sort flag and every list operation, and it talks to
`StudySpotRepository` instead of a file. That is why the presenter tests run on the JVM
with no emulator.

Data lives in the app's private directory:

- `filesDir/users.json` — accounts (salt + hash, no plain text).
- `filesDir/spots_<userId>.json` — one spot list per account.

The UML diagram is in [`docs/uml.png`](docs/uml.png).

## Tests

```bash
./gradlew :app:testDebugUnitTest   # JVM: model, validation, presenter
./gradlew :app:lintDebug           # Android lint
./gradlew :app:assembleDebug       # build the APK
```

Current JVM tests cover:

- `StudySpotTest` — defaults, constructor, equality, `copy()`, mutability.
- `StudySpotValidationTest` — blank title, latitude/longitude range, NaN/infinity,
  accepted boundary values.
- `MainPresenterTest` — search, sort toggle, empty state, category + search + sort
  combinations, clearing the filter, delete/refresh.

## Releases

| Tag | What it marks |
|---|---|
| `v1.0`, `v1.1` | Assignment 1 (CRUD + JSON + search) |
| `v2.0-splash` | Splash screen, Timber, ViewBinding |
| `v2.1-mvp` | MVP presenter + repository split |
| `v2.2-auth` | Local accounts, session, per-user JSON |
| `v2.3-maps` | Google Map, markers, current location, add-from-map |
| `v2.4-ux` | Category chips, night mode, transitions, drawer/settings |
| `v2.5-submission` | Assignment 2 submission |

Every tag points at the merge commit of the pull request that delivered it, not at a
later HEAD.

## AI Usage Declaration

Per the 70/30 rule: I used AI for planning, boilerplate drafts and test ideas. Every
generated block was read, adapted to this codebase, built and tested by me. The feature
decisions and the final code are my own. No user data was sent to an AI tool.

| Area (commit / PR) | AI help | What I did |
|---|---|---|
| Assignment 2 issue plan and day-by-day breakdown | Yes | Chose the scope, reordered days, cut the Firebase bonus |
| Search box to filter spots (PR #4) | Some | Hint on filtering as you type, wired into the list |
| Unit tests for `StudySpot` (PR #6) | Some | Picked the cases, ran them, fixed a failing assertion |
| Material toolbar + up/back (PR #8) | Yes | Boilerplate; hooked up navigation myself |
| RecyclerView + adapter (PR #9) | Yes | Adapter/card layout drafted; wired clicks and swipe |
| Category dropdown (PR #19) | Yes | Boilerplate; wired into model and store |
| MVP presenter/repository (PR #41) | Yes | Draft split; wrote the contract, repository and wiring |
| Auth + per-user JSON (PR #44, #45) | Yes | Draft hashing/JSON code; tested flows and fixed migration |
| Maps, markers, location (PR #51, #52) | Yes | Draft map code; permissions, lifecycle and error handling mine |
| Category chips, night mode, transitions (PR #53) | Yes | Draft UI code; chose colours, checked both themes by hand |
| Validation and presenter tests (PR #54) | Yes | Test ideas; ran, debugged and adjusted the assertions |
| README and demo script (PR #54) | Yes | Draft structure; corrected every command and screen name |

Tools: DeepSeek V4 via OpenCode for the issue plan, code drafts and test ideas. No other
AI tool was used for the submitted code.
