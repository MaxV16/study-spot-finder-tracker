package org.setu.studyspotfinder.data

import org.setu.studyspotfinder.models.StudySpot

// one interface so the presenters dont care where the spots come from.
// right now its json on the phone, later it could be firebase and nothing else changes.
interface StudySpotRepository {
    fun findAll(): List<StudySpot>
    fun findOne(id: Long): StudySpot?
    fun create(spot: StudySpot)
    fun update(spot: StudySpot): Boolean
    fun delete(id: Long): Boolean
    fun deleteAll()
}