package org.setu.studyspotfinder

import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import org.setu.studyspotfinder.models.StudySpot

// checks the rules that guard a save, so a bad latitude can never reach the json file or the map
class StudySpotValidationTest {

    private fun validSpot() = StudySpot(
        title = "Library",
        desc = "Quiet floor",
        x = 53.3498,
        y = -6.2603,
        category = "Library"
    )

    @Test
    fun `valid spot passes`() {
        validSpot().validate()
    }

    @Test
    fun `blank title is rejected`() {
        val spot = validSpot().copy(title = "   ")
        try {
            spot.validate()
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertEquals("Name must not be empty.", e.message)
        }
    }

    @Test
    fun `latitude above 90 is rejected`() {
        val spot = validSpot().copy(x = 90.1)
        try {
            spot.validate()
            fail("Expected IllegalArgumentException")
        } catch (_: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun `longitude below -180 is rejected`() {
        val spot = validSpot().copy(y = -180.1)
        try {
            spot.validate()
            fail("Expected IllegalArgumentException")
        } catch (_: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun `NaN latitude is rejected`() {
        val spot = validSpot().copy(x = Double.NaN)
        try {
            spot.validate()
            fail("Expected IllegalArgumentException")
        } catch (_: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun `infinite longitude is rejected`() {
        val spot = validSpot().copy(y = Double.POSITIVE_INFINITY)
        try {
            spot.validate()
            fail("Expected IllegalArgumentException")
        } catch (_: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun `boundary values are accepted`() {
        // the corners of the map are legal, only values past them are not
        validSpot().copy(x = -90.0, y = -180.0).validate()
        validSpot().copy(x = 90.0, y = 180.0).validate()
    }
}
