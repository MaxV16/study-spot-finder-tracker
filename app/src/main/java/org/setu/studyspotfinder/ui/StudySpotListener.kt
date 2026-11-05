package org.setu.studyspotfinder.ui

import org.setu.studyspotfinder.models.StudySpot

// the row talks to the screen through this, so the adapter never knows about activities.
// lab 06 wants edit/delete to go through a listener instead of buttons in the row.
interface StudySpotListener {
    fun onSpotClicked(spot: StudySpot)
    fun onEdit(spot: StudySpot)
    fun onDelete(spot: StudySpot)
}
