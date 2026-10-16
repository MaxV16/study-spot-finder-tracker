package org.setu.studyspotfinder

import org.junit.Test
import org.junit.Assert.*
import org.setu.studyspotfinder.models.StudySpot

// unit tests for the StudySpot model, these run on the computer not on a device (fast)
class StudySpotTest {

    //when we make a spot with no arguments it should have sensible defaults
    @Test
    fun defaultStudySpot_hasSensibleDefaults() {
        val spot = StudySpot()

        // everything should be its empty/zero value
        assertEquals(0L, spot.id)
        assertEquals("", spot.title)
        assertEquals("", spot.desc)
        assertEquals(0.0, spot.x, 0.0)
        assertEquals(0.0, spot.y, 0.0)
    }

    // also the constructor should store whatever values we pass in
    @Test
    fun constructor_storesAllTheValues() {
        val spot = StudySpot(3L, "Library", "quiet floor", 12.5, 44.2)

        assertEquals(3L, spot.id)
        assertEquals("Library", spot.title)
        assertEquals("quiet floor", spot.desc)
        assertEquals(12.5, spot.x, 0.0)
        assertEquals(44.2, spot.y, 0.0)
    }

    // two spots with the same values should be equal (data class gives us this for free)
    @Test
    fun twoSpotsWithSameValues_areEqual() {
        val a = StudySpot(1L, "Cafe", "good wifi", 1.0, 2.0)
        val b = StudySpot(1L, "Cafe", "good wifi", 1.0, 2.0)

        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    // copy() lets us make a changed version without touching the original
    @Test
    fun copy_createsAChangedCopy() {
        val original = StudySpot(1L, "Cafe", "good wifi", 1.0, 2.0)
        val edited = original.copy(title = "New Cafe")

        assertEquals("New Cafe", edited.title)
        assertEquals("Cafe", original.title) // original must not change
        assertEquals(1L, edited.id)
    }

    //the fields are var so we can update a spot in place
    @Test
    fun fields_areMutable() {
        val spot = StudySpot()
        spot.title = "Study Room"
        spot.x = 99.9

        assertEquals("Study Room", spot.title)
        assertEquals(99.9, spot.x, 0.0)
    }
}
