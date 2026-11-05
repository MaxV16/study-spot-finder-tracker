package org.setu.studyspotfinder.data

import org.setu.studyspotfinder.models.StudySpot

// wraps the existing store so the json file stays exactly the same, we just hide it behind the interface
class JsonStudySpotRepository(
    private val store: StudySpotStore
) : StudySpotRepository {

    override fun findAll(): List<StudySpot> = store.findAll()

    override fun findOne(id: Long): StudySpot? = store.findOne(id)

    override fun create(spot: StudySpot) = store.create(spot)

    override fun update(spot: StudySpot): Boolean = store.update(spot)

    override fun delete(id: Long): Boolean = store.delete(id)

    override fun deleteAll() = store.deleteAll()

    override fun restore(spot: StudySpot) = store.restore(spot)
}