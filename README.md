# Study Spot Finder & Tracker

20106742

## AI Usage Declaration

Per the 70/30 rule, here's where AI helped. I went through all of it, understand
how it works, and can explain it in the walkthrough.

| Feature (commit / PR)                          | AI help | What I did                                                        |
|------------------------------------------------|---------|-------------------------------------------------------------------|
| Search box to filter spots (PR #4)             | Some    | Asked for a hint on how to filter as you type, wired it into my list |
| Unit tests for the StudySpot model (PR #6)     | Some    | Chose which cases to test, ran them, fixed a failing assertion     |
| Material toolbar + up/back (PR #8)             | Yes     | Material boilerplate from AI: I hooked up the up/back myself        |
| RecyclerView + adapter (PR #9)                 | Yes     | Adapter/card layout from AI: I wired the edit/delete clicks        |

Everything else (the model, the JSON store, the app flow, what to build) is my
own work. AI was mostly used for boilerplate that I read through and adapted.
For the commits/PRs specifically, this maps to:
- PR #4 – search/filter (commit d474893)
- PR #6 – unit tests
- PR #8 – toolbar commits 4bb6337, bfc3234, 779def5
- PR #9 – RecyclerView (pending)
