package org.setu.studyspotfinder

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import com.google.android.material.appbar.MaterialToolbar
class MainActivity : AppCompatActivity() {

    private lateinit var listLayout: LinearLayout
    private lateinit var searchBox: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createUserInterface()
    }

    override fun onResume() {
        super.onResume()

        if (::listLayout.isInitialized) {
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

        val addButton = Button(this).apply {
            text = "Add Mark"
            setOnClickListener {
                val intent = Intent(this@MainActivity, AddEditActivity::class.java)
                startActivity(intent)
            }
        }

        // the search box up top, when you type in it the list filters itself
        searchBox = EditText(this).apply {
            hint = "Search spots..."
            setSingleLine()

            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    displayMarks()
                }
            })
        }

        listLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        root.addView(
            toolbar,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        root.addView(
            searchBox,
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

        root.addView(
            listLayout,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        setContentView(root)
        setSupportActionBar(toolbar)
        displayMarks()
    }

    private fun displayMarks() {

        listLayout.removeAllViews()

        val query = searchBox.text.toString()

        val marks = AppData.studySpots.findAll().filter {
            it.title.contains(query, ignoreCase = true)
        }
        if (marks.isEmpty()) {

            val emptyText = TextView(this).apply {
                text = "No placed spots yet."
                textSize = 18f
                setPadding(0, 40, 0, 40)
            }

            listLayout.addView(emptyText)

            return
        }

        for (mark in marks) {

            val markLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, 20, 0, 20)
            }

            val markTitle = TextView(this).apply {
                text = "${mark.id}: ${mark.title}"
                textSize = 20f
            }

            val markDescription = TextView(this).apply {
                text = mark.desc
                textSize = 16f
            }

            val coordinates = TextView(this).apply {
                text = "X: ${mark.x}, Y: ${mark.y}"
                textSize = 14f
            }

            val editButton = Button(this).apply {
                text = "Edit"

                setOnClickListener {
                    val intent = Intent(
                        this@MainActivity,
                        AddEditActivity::class.java
                    )

                    intent.putExtra("id", mark.id)

                    startActivity(intent)
                }
            }

            val deleteButton = Button(this).apply {
                text = "Delete"

                setOnClickListener {
                    AppData.studySpots.delete(mark.id)
                    displayMarks()
                }
            }

            markLayout.addView(markTitle)
            markLayout.addView(markDescription)
            markLayout.addView(coordinates)
            markLayout.addView(editButton)
            markLayout.addView(deleteButton)

            listLayout.addView(markLayout)
        }
    }
}
