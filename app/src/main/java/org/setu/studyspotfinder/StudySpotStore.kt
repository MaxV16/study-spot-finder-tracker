package org.setu.studyspotfinder

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

// this store replaces the placedmarklist that was memory only, this is json stored so ez persistence.
// but instead of keeping the marks in memory it saves them to a json file
// that way they are still there after the app is closed
class StudySpotStore(context: Context) {

    // this is the file we save everything to, its in the apps private storage
    private val file = java.io.File(context.filesDir, "marks.json")

    // read the file and turn it back into a list of marks
    fun findAll(): List<StudySpot> {
        if (!file.exists()) return emptyList()
        val arr = JSONArray(file.readText())
        val list = ArrayList<StudySpot>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            list.add(
                StudySpot(
                    id = obj.getLong("id"),
                    title = obj.getString("title"),
                    desc = obj.getString("desc"),
                    x = obj.getDouble("x"),
                    y = obj.getDouble("y")
                )
            )
        }
        return list
    }

    // add a new mark, we give it the next free id ourselfs
    fun create(mark: StudySpot) {
        val list = findAll().toMutableList()
        mark.id = (list.maxOfOrNull { it.id } ?: 0L) + 1
        list.add(mark)
        save(list)
    }

    // update a mark thats allready saved
    fun update(mark: StudySpot): Boolean {
        val list = findAll().toMutableList()
        val index = list.indexOfFirst { it.id == mark.id }
        if (index == -1) return false
        list[index] = mark
        save(list)
        return true
    }

    // removes a mark by its id
    fun delete(id: Long): Boolean {
        val list = findAll().toMutableList()
        val removed = list.removeAll { it.id == id }
        if (removed) save(list)
        return removed
    }

    // wipes every mark out, used by the delete all menu option
    fun deleteAll() {
        save(emptyList())
    }

    // find one mark by its id
    fun findOne(id: Long): StudySpot? {
        return findAll().find { it.id == id }
    }

    // turn the whole list into json and write it to the file
    private fun save(list: List<StudySpot>) {
        val arr = JSONArray()
        for (mark in list) {
            val obj = JSONObject()
            obj.put("id", mark.id)
            obj.put("title", mark.title)
            obj.put("desc", mark.desc)
            obj.put("x", mark.x)
            obj.put("y", mark.y)
            arr.put(obj)
        }
        file.writeText(arr.toString())
    }
}