package org.setu.studyspotfinder

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import android.app.AlertDialog
import android.view.Menu
import android.view.MenuItem


class MainActivity : AppCompatActivity() {

    private lateinit var searchBox: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyText: TextView
    private var sortAZ = false
    private lateinit var spotAdapter: StudySpotAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createUserInterface()
    }

    override fun onResume() {
        super.onResume()

        if (::recyclerView.isInitialized) {
            displayMarks()
        }
    }

    private fun createUserInterface() {

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        val toolbar = MaterialToolbar(this).apply {
            title = "Study Spots"
        }

        val addButton = MaterialButton(this).apply {
            text = "Add Mark"
            setOnClickListener {
                val intent = Intent(this@MainActivity, AddEditActivity::class.java)
                startActivity(intent)
            }
        }

        // the search box up top, when you type in it the list filters itself
        searchBox = TextInputEditText(this).apply {
            setSingleLine()

            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    displayMarks()
                }
            })
        }

        val searchLayout = TextInputLayout(this).apply {
            hint = "Search spots"
            addView(searchBox)
        }

        emptyText = TextView(this).apply {
            text = "No placed spots yet."
            textSize = 18f
            setPadding(0, 40, 0, 40)
            visibility = View.GONE
        }

        // the adapter knows how to draw one spot, the recyclerView just scrolls them
        spotAdapter = StudySpotAdapter(
            emptyList(),
            onEdit = { spot ->
                val intent = Intent(this@MainActivity, AddEditActivity::class.java)
                intent.putExtra("id", spot.id)
                startActivity(intent)
            },
            onDelete = { spot ->
                AppData.studySpots.delete(spot.id)
                displayMarks()
            }
        )

        recyclerView = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = spotAdapter
        }

        root.addView(
            toolbar,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        root.addView(
            searchLayout,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        root.addView(
            addButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        // weight = 1 makes the list fill the rest of the screen and scroll
        root.addView(
            recyclerView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        root.addView(emptyText)

        setContentView(root)
        setSupportActionBar(toolbar)

        displayMarks()
    }


    // build the little three dot menu in the toolbar
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    // //android calls this when you tap a menu item, so we check which one it was and run the matching code.
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add -> {
                startActivity(Intent(this, AddEditActivity::class.java))
                true
            }
            R.id.action_sort -> {
                sortAZ = !sortAZ
                displayMarks()
                true
            }
            R.id.action_delete_all -> {
                deleteAllSpots()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // asks first so you cant nuke everything by accident, been there done that. rip 1st year assignments :/
    private fun deleteAllSpots() {
        AlertDialog.Builder(this)
            .setTitle("Delete all spots?")
            .setMessage("This removes every study spot and cant be undone.")
            .setPositiveButton("Delete") { _, _ ->
                AppData.studySpots.deleteAll()
                displayMarks()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun displayMarks() {

        val query = searchBox.text.toString()

        val marks = AppData.studySpots.findAll()
            .filter { it.title.contains(query, ignoreCase = true) }
            .let { if (sortAZ) it.sortedBy { spot -> spot.title.lowercase() } else it }

        emptyText.visibility = if (marks.isEmpty()) View.VISIBLE else View.GONE
        recyclerView.visibility = if (marks.isEmpty()) View.GONE else View.VISIBLE

        spotAdapter.updateSpots(marks)
    }
}