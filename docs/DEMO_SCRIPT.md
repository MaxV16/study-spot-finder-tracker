# 8–10 minute demo run sheet

Assignment 2 · Study Spot Finder & Tracker · 20106742

Aim for about 8 minutes of talking with 2 minutes of slack. Practise once with a timer.
Before recording: clear app data, make sure the Maps key is in `local.properties`, and
have one photo in the emulator's gallery.

| Time | Show | Say |
|---|---|---|
| 0:00 | Cold start → splash → login screen | "Cold start. The splash uses the AndroidX SplashScreen API. There is no session yet, so `MainActivity` sends me to the auth screen instead of drawing the list." |
| 0:35 | Register a new account → lands on empty list | "Passwords are PBKDF2-hashed with a random salt in `AuthManager`; only salt and hash go into `users.json`. The account gets its own `spots_<userId>.json`, so two users on this device cannot see each other's spots." |
| 1:15 | Add a spot: title, description, category, photo, Use my location → Save | "The photo picker is OpenDocument with a persistable URI permission, so the image survives a restart. The location button uses the fused provider. On save, `StudySpot.validate()` blocks a blank name and coordinates outside −90..90 / −180..180." |
| 2:30 | Add a second spot quickly (different category) | "Same form, no id extra, so the repository creates instead of updating." |
| 3:00 | Type in search, tap a category chip, toggle A–Z | "All three live in `MainPresenter`: query, category filter and sort flag are combined on every change. The activity only renders what the presenter returns — that is the MVP split that makes the tests possible." |
| 4:00 | Tap a card → detail screen | "The detail screen reads the spot by id in `onResume`, so an edit shows up the moment I come back." |
| 4:30 | Edit from detail → change category → back | "Same form in edit mode. `update` returns false if the id is gone and the list reports it." |
| 5:00 | Swipe a row → Undo snackbar | "Swipe delete passes the whole spot to the presenter. Undo calls `restore`, which puts the same id back, not a duplicate." |
| 5:45 | Open drawer → Map | "The map frames every spot with `LatLngBounds`, and each marker's tag carries the spot id, so an info window tap opens the detail screen." |
| 6:45 | Long-press the map → add a spot there | "Long press passes lat/lng extras into the same add form, so validation and persistence are shared." |
| 7:15 | Current-location button on the map | "Runtime permission, fused location, camera animates to the fix. This is the Excellent-band location row." |
| 7:45 | Drawer → Settings → dark mode | "AppCompatDelegate persists the choice through `ThemePrefs`; the activity recreates and the night resources take over." |
| 8:20 | Log out → Back | "Logout clears the session and finishes the task with `CLEAR_TASK`, so Back cannot re-enter the list." |
| 8:45 | Close on GitHub: issues, PRs, tags, tests | "Twelve issues, each with a branch, a PR and Plan/Progress/Done comments. Tags v2.0–v2.5 point at the merge commits. Unit tests run on the JVM with no emulator." |

## Backup plan

- **Blank map tiles** — the key is missing or restricted. Show the marker code and the
  README setup step, then say the key is configured per machine in `local.properties`.
- **No location fix on the emulator** — open the emulator's Extended controls → Location,
  set a point, then press the button again. If it still fails, type coordinates and
  mention the fused provider handles the failure with a toast.
- **Photo picker empty** — drag an image into the emulator first, or skip the photo and
  say the URI is persisted with `takePersistableUriPermission`.
- **Crash or hang** — switch to the pre-recorded 60-second happy-path clip and continue
  the walkthrough over it.

## Recording checklist

- 8–10 minutes, audio audible, no notifications or personal data on screen.
- Start from a clean install so the register → empty list flow is real.
- End with the repo open on the tags/issues page.
