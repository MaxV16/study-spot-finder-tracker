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


// this is the main screen, it shows all the spots plus the search bar and the menu
class MainActivity : AppCompatActivity() {

    // the search field at the top of the screen
    private lateinit var searchBox: EditText
    // the scrolling list of spots
    private lateinit var recyclerView: RecyclerView
    // the text we show when there is nothing to show
    private lateinit var emptyText: TextView
    // keeps track of if we are sorting a-z or not, starts off
    private var sortAZ = false
    // the adapter that draws each row
    private lateinit var spotAdapter: StudySpotAdapter

    // runs when the screen is first created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // build all the views in code (this screen has no xml layout)
        createUserInterface()
    }

    // runs every time we come back to this screen (e.g after adding or editing)
    override fun onResume() {
        super.onResume()

        // only redraw if the views are actually built yet, stops a crash
        if (::recyclerView.isInitialized) {
            displaySpots()
        }
    }

    // this is where we build the whole screen by hand
    private fun createUserInterface() {

        // grab the padding size from dimens so it isnt hardcoded
        val pad = resources.getDimensionPixelSize(R.dimen.screen_padding)

        // the root layout is a vertical linear layout, everything stacks top to bottom
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
        }

        // the toolbar at the top with the app name
        val toolbar = MaterialToolbar(this).apply {
            title = getString(R.string.main_title)
            setBackgroundColor(context.getColor(R.color.spot_primary))
            setTitleTextColor(context.getColor(R.color.white))
        }

        // the button that takes us to the add screen
        val addButton = MaterialButton(this).apply {
            text = getString(R.string.add_spot)
            setOnClickListener {
                // jumping to the form with no id means its a brand new spot
                val intent = Intent(this@MainActivity, AddEditActivity::class.java)
                startActivity(intent)
            }
        }

        // the search box up top, when you type in it the list filters itself
        searchBox = TextInputEditText(this).apply {
            setSingleLine()

            // watch the text and redraw the list every time it changes
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    displaySpots()
                }
            })
        }

        // wrap the search box in a material text field so we get the nice floating hint
        val searchLayout = TextInputLayout(this).apply {
            hint = getString(R.string.search_hint)
            addView(searchBox)
        }

        // the "nothing here yet" text, hidden until we need it
        emptyText = TextView(this).apply {
            text = getString(R.string.empty_list)
            textSize = 18f
            setPadding(0, 40, 0, 40)
            visibility = View.GONE
        }

        // the adapter knows how to draw one spot, the recyclerView just scrolls them
        spotAdapter = StudySpotAdapter(
            emptyList(),
            // when edit is tapped on a row we open the form with that spots id
            onEdit = { spot ->
                val intent = Intent(this@MainActivity, AddEditActivity::class.java)
                intent.putExtra("id", spot.id)
                startActivity(intent)
            },
            // when delete is tapped we remove it from the store and redraw
            onDelete = { spot ->
                AppData.studySpots.delete(spot.id)
                displaySpots()
            }
        )

        // the recyclerview is the scrolling list, we just give it the adapter
        recyclerView = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = spotAdapter
        }

        // now add all the views to the root, in the order we want them on screen
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

        // finally put the root on the screen
        setContentView(root)
        setSupportActionBar(toolbar)

        // draw the list for the first time
        displaySpots()
    }


    // build the little three dot menu in the toolbar
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    // android calls this when you tap a menu item, so we check which one it was and run the matching code
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            // add opens the empty form
            R.id.action_add -> {
                startActivity(Intent(this, AddEditActivity::class.java))
                true
            }
            // sort flips the flag and redraws, tapping it again flips it back
            R.id.action_sort -> {
                sortAZ = !sortAZ
                displaySpots()
                true
            }
            // delete all asks first (see below) before wiping anything
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
                displaySpots()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // reads all the spots, filters by the search, sorts if needed, then shows them
    private fun displaySpots() {

        // whatever the user has typed into the search box
        val query = searchBox.text.toString()

        // get everything from the store then narrow it down
        val spots = AppData.studySpots.findAll()
            // keep only spots whose title contains what was typed (ignoring upper/lower case)
            .filter { it.title.contains(query, ignoreCase = true) }
            // if sort is on, sort a-z by lowercasing the title first so it sorts fairly
            .let { if (sortAZ) it.sortedBy { spot -> spot.title.lowercase() } else it }

        // if there is nothing to show, show the empty text and hide the list (and the other way around)
        emptyText.visibility = if (spots.isEmpty()) View.VISIBLE else View.GONE
        recyclerView.visibility = if (spots.isEmpty()) View.GONE else View.VISIBLE

        // hand the filtered list over to the adapter
        spotAdapter.updateSpots(spots)
    }
}
