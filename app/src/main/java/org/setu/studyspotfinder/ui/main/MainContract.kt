package org.setu.studyspotfinder.ui.main

import org.setu.studyspotfinder.models.StudySpot

// the contract is the deal between the screen and the presenter.
// the activity only knows these three drawing methods, the presenter only knows these actions.
interface MainContract {

    interface View {
        fun showSpots(spots: List<StudySpot>)
        fun showEmpty()
        fun showMessage(message: String)
    }

    interface Presenter {
        fun attach(view: View)
        fun detach()
        fun refresh()
        fun onSearchChanged(query: String)
        fun toggleSort()
        fun deleteSpot(id: Long)
        fun deleteAll()
        fun restoreSpot(spot: StudySpot)
    }
}