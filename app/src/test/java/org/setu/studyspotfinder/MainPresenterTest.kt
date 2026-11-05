package org.setu.studyspotfinder

import org.junit.Assert.*
import org.junit.Test
import org.setu.studyspotfinder.data.StudySpotRepository
import org.setu.studyspotfinder.models.StudySpot
import org.setu.studyspotfinder.ui.main.MainContract
import org.setu.studyspotfinder.ui.main.MainPresenter

// these run on the computer, no emulator, no android classes. thats the whole point of the presenter.
class MainPresenterTest {

    // a fake repo that just keeps a list in memory instead of a json file
    private class FakeRepository(var spots: MutableList<StudySpot>) : StudySpotRepository {
        override fun findAll() = spots.toList()
        override fun findOne(id: Long) = spots.find { it.id == id }
        override fun create(spot: StudySpot) { spots.add(spot) }
        override fun restore(spot: StudySpot) { spots.add(spot) }
        override fun update(spot: StudySpot): Boolean {
            val i = spots.indexOfFirst { it.id == spot.id }
            if (i == -1) return false
            spots[i] = spot
            return true
        }
        override fun delete(id: Long) = spots.removeAll { it.id == id }
        override fun deleteAll() { spots.clear() }
    }

    // a fake view that just remembers what the presenter told it to draw
    private class FakeView : MainContract.View {
        var spots: List<StudySpot> = emptyList()
        var empty = false
        var message: String? = null
        override fun showSpots(spots: List<StudySpot>) { this.spots = spots; empty = false }
        override fun showEmpty() { spots = emptyList(); empty = true }
        override fun showMessage(message: String) { this.message = message }
    }

    private fun presenterWith(vararg spots: StudySpot): Pair<MainPresenter, FakeView> {
        val presenter = MainPresenter(FakeRepository(spots.toMutableList()))
        val view = FakeView()
        presenter.attach(view)
        return presenter to view
    }

    @Test
    fun `search filters titles ignoring case`() {
        val (presenter, view) = presenterWith(
            StudySpot(1, "Library", "", 0.0, 0.0),
            StudySpot(2, "Cafe", "", 0.0, 0.0)
        )

        presenter.onSearchChanged("LIB")

        assertEquals(listOf("Library"), view.spots.map { it.title })
    }

    @Test
    fun `sort toggles a to z and back`() {
        val (presenter, view) = presenterWith(
            StudySpot(1, "Cafe", "", 0.0, 0.0),
            StudySpot(2, "Library", "", 0.0, 0.0)
        )

        presenter.toggleSort()
        assertEquals(listOf("Cafe", "Library"), view.spots.map { it.title })

        presenter.toggleSort()
        assertEquals(listOf("Cafe", "Library"), view.spots.map { it.title })
    }

    @Test
    fun `empty repository shows empty state`() {
        val (_, view) = presenterWith()
        assertTrue(view.empty)
    }

    @Test
    fun `delete removes the spot and refreshes`() {
        val (presenter, view) = presenterWith(
            StudySpot(1, "One", "", 0.0, 0.0),
            StudySpot(2, "Two", "", 0.0, 0.0)
        )

        presenter.deleteSpot(1)

        assertEquals(listOf("Two"), view.spots.map { it.title })
    }
}