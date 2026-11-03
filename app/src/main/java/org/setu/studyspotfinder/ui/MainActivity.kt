package org.setu.studyspotfinder.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import android.app.AlertDialog
import android.view.Menu
import android.view.MenuItem
import org.setu.studyspotfinder.R
import org.setu.studyspotfinder.data.AppData
import timber.log.Timber


// MainActivity is the home screen aka the launcher, it shows the list of spots, the search box and the menu.
// everything here is built in kotlin instead of an xml layout, doing the layout by hand was annoying ngl but atleast its all in one file.
class MainActivity : AppCompatActivity() {

    // refs to the views we make below, kept up here so all the funcs can reach em
    private lateinit var searchBox: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyText: TextView
    private var sortAZ = false
    private lateinit var spotAdapter: StudySpotAdapter

    // onCreate runs once when the screen is first made, its where we build the ui
    override fun onCreate(savedInstanceState: Bundle?) {
        // must be before super.onCreate or the splash gets skipped
        installSplashScreen()

        super.onCreate(savedInstanceState)

        Timber.i("MainActivity created")
        createUserInterface()
    }

    // onResume runs everytime the screen comes back to the front, like after you save a spot and come back
    override fun onResume() {
        super.onResume()

        // isInitialized stops a crash on the very first pass when the recyclerview isnt built yet
        if (::recyclerView.isInitialized) {
            displaySpots()
        }
    }

    // build the whole screen top to bottom in here
    private fun createUserInterface() {

        // grab our 16dp so we arent hardcoding numbers
        val pad = resources.getDimensionPixelSize(R.dimen.screen_padding)

        // a vertical linear layout so everything stacks top to bottom in order
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
        }

        // the top bar
        val toolbar = MaterialToolbar(this).apply {
            title = getString(R.string.main_title)
            setBackgroundColor(context.getColor(R.color.spot_primary))
            setTitleTextColor(context.getColor(R.color.white))
        }

        // the Add button, opens the form with no id wich means make a new one
        val addButton = MaterialButton(this).apply {
            text = getString(R.string.add_spot)
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
                // after every keystroke we redraw, thats what makes it filter as you type
                override fun afterTextChanged(s: Editable?) {
                    displaySpots()
                }
            })
        }

        // the material wrapper that gives the box its floating hint
        val searchLayout = TextInputLayout(this).apply {
            hint = getString(R.string.search_hint)
            addView(searchBox)
        }

        // shown instead of the list when there are no spots, hidden for now
        emptyText = TextView(this).apply {
            text = getString(R.string.empty_list)
            textSize = 18f
            setPadding(0, 40, 0, 40)
            visibility = View.GONE
        }

        // the adapter knows how to draw one spot, the recyclerView just scrolls them.
        // we hand it 2 lambdas so it doesnt need to know about activities, main decides what the buttons do.
        // onEdit opens the form with the spot id so it loads that one, onDelete removes it then refreshes the list
        spotAdapter = StudySpotAdapter(
            emptyList(),
            onEdit = { spot ->
                val intent = Intent(this@MainActivity, AddEditActivity::class.java)
                intent.putExtra("id", spot.id)
                startActivity(intent)
            },
            onDelete = { spot ->
                AppData.studySpots.delete(spot.id)
                displaySpots()
            }
        )

        // the list itself, the layout manager stacks the rows vertically
        recyclerView = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = spotAdapter
        }

        // adding all these views one by one by hand was the annoying part, an xml layout woulda been less code
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

        displaySpots()
    }


    // build the little three dot menu in the toolbar
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    // android calls this when you tap a menu item, so we check wich one it was and run the matching code
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            // same as the Add button, opens a blank form
            R.id.action_add -> {
                startActivity(Intent(this, AddEditActivity::class.java))
                true
            }
            // flip the a-z flag and redraw, tapping again flips it back
            R.id.action_sort -> {
                sortAZ = !sortAZ
                displaySpots()
                true
            }
            R.id.action_delete_all -> {
                deleteAllSpots()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // ask first so you cant nuke everything by accident
    private fun deleteAllSpots() {
        AlertDialog.Builder(this)
            .setTitle("Delete all spots?")
            .setMessage("This removes every study spot and cant be undone.")
            .setPositiveButton("Delete") { _, _ ->
                AppData.studySpots.deleteAll()
                displaySpots()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // reads the spots, filters by the search text, sorts if needed, then hands the list to the adapter
    private fun displaySpots() {

        val query = searchBox.text.toString()

        val marks = AppData.studySpots.findAll()
            // keep only the spots whose title contains what you typed, ignoring capitals
            .filter { it.title.contains(query, ignoreCase = true) }
            // if sortAZ is on sort by title, else leave em as they are
            .let { if (sortAZ) it.sortedBy { spot -> spot.title.lowercase() } else it }

        // nothing to show means show the msg and hide the list, and the other way round
        emptyText.visibility = if (marks.isEmpty()) View.VISIBLE else View.GONE
        recyclerView.visibility = if (marks.isEmpty()) View.GONE else View.VISIBLE

        spotAdapter.updateSpots(marks)
    }
}
