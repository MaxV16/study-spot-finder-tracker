package org.setu.studyspotfinder.ui.main

import org.setu.studyspotfinder.data.StudySpotRepository

// owns the query, the sort flag and the list logic now, no android imports at all so its easy to test
class MainPresenter(
    private val repository: StudySpotRepository
) : MainContract.Presenter {

    private var view: MainContract.View? = null
    private var query: String = ""
    private var sortAZ: Boolean = false

    override fun attach(view: MainContract.View) {
        this.view = view
        refresh()
    }

    override fun detach() {
        view = null
    }

    override fun onSearchChanged(query: String) {
        this.query = query
        refresh()
    }

    override fun toggleSort() {
        sortAZ = !sortAZ
        refresh()
    }

    override fun refresh() {
        val spots = repository.findAll()
            .filter { it.title.contains(query, ignoreCase = true) }
            .let { if (sortAZ) it.sortedBy { spot -> spot.title.lowercase() } else it }

        val currentView = view ?: return
        if (spots.isEmpty()) currentView.showEmpty() else currentView.showSpots(spots)
    }

    override fun deleteSpot(id: Long) {
        repository.delete(id)
        refresh()
    }

    override fun deleteAll() {
        repository.deleteAll()
        refresh()
    }
}