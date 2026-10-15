# Study Spot Finder & Tracker

Student ID: 20106742

An Android app for saving study spots (title, description, location, category).
Built in Kotlin with a JSON file for persistence so your spots survive closing the app.

## Features
- Add / edit / delete study spots (full CRUD)
- Search spots by title as you type
- Sort A-Z and delete-all from the options menu
- Category dropdown when adding or editing a spot
- Data saved as JSON in the app's private storage
- Material Design UI, toolbar with up/back navigation

## Project structure
app/src/main/java/org/setu/studyspotfinder/
  StudySpot.kt        - the data model
  StudySpotStore.kt   - saves/loads spots to spots.json (the persistence layer)
  StudySpotApp.kt     - Application class, creates the store once at launch
  AppData.kt          - singleton that gives every screen the store
  MainActivity.kt     - list screen (search, add button, menu, RecyclerView)
  AddEditActivity.kt  - the add / edit form
  StudySpotAdapter.kt - builds each row in the list

## How to run
Open in Android Studio and run on an emulator, or build with:
  ./gradlew :app:assembleDebug

## AI Usage Declaration

Per the 70/30 rule, here's where AI helped. I went through all of it, understand
how it works, and can explain it in the walkthrough.

| Feature (commit / PR)                          | AI help | What I did                                                          |
|------------------------------------------------|---------|---------------------------------------------------------------------|
| Search box to filter spots (PR #4)             | Some    | Asked for a hint on how to filter as you type, wired it into my list |
| Unit tests for the StudySpot model (PR #6)     | Some    | Chose which cases to test, ran them, fixed a failing assertion       |
| Material toolbar + up/back (PR #8)             | Yes     | Material boilerplate from AI: I hooked up the up/back myself         |
| RecyclerView + adapter (PR #9)                 | Yes     | Adapter/card layout from AI: I wired the edit/delete clicks          |
| Category dropdown (PR #19)                     | Yes     | Dropdown boilerplate from AI: I wired it into the model and store    |

Everything else (the model, the JSON store, the app flow, what to build) is my
own work. AI was mostly used for boilerplate that I read through and adapted.
