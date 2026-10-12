package org.setu.studyspotfinder

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class AddEditActivity : AppCompatActivity() {

    private lateinit var titleInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var xInput: EditText
    private lateinit var yInput: EditText

    private lateinit var titleLayout: TextInputLayout
    private lateinit var descriptionLayout: TextInputLayout

    private lateinit var xLayout: TextInputLayout

    private lateinit var yLayout: TextInputLayout

    private var editingId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createUserInterface()

        editingId = intent.getLongExtra("id", -1L)

        if (editingId != -1L) {
            loadExistingMark(editingId!!)
        }

        supportActionBar?.title = if (editingId == -1L) "Add Spot" else "Edit Spot"

    }


    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun createUserInterface() {

            val root = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(32, 32, 32, 32)
            }

            val toolbar = MaterialToolbar(this).apply {
                title = "Add Spot"
            }

            root.addView(
                toolbar,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )

            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)

            titleInput = TextInputEditText(this)
            descriptionInput = TextInputEditText(this)

            xInput = TextInputEditText(this).apply {
                inputType =
                    android.text.InputType.TYPE_CLASS_NUMBER or
                            android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            }

            yInput = TextInputEditText(this).apply {
                inputType =
                    android.text.InputType.TYPE_CLASS_NUMBER or
                            android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            }

            titleLayout = TextInputLayout(this).apply {
                hint = "Title"
                addView(titleInput)
            }

            descriptionLayout = TextInputLayout(this).apply {
                hint = "Description"
                addView(descriptionInput)
            }

            xLayout = TextInputLayout(this).apply {
                hint = "X coordinate"
                addView(xInput)
            }

            yLayout = TextInputLayout(this).apply {
                hint = "Y coordinate"
                addView(yInput)
            }

            val saveButton = MaterialButton(this).apply {
                text = "Save"
                setOnClickListener {
                    saveMark()
                }
            }

            val cancelButton = MaterialButton(this).apply {
                text = "Cancel"
                setOnClickListener {
                    finish()
                }
            }

            root.addView(titleLayout)
            root.addView(descriptionLayout)
            root.addView(xLayout)
            root.addView(yLayout)
            root.addView(saveButton)
            root.addView(cancelButton)

            setContentView(root)
        }

    private fun loadExistingMark(id: Long) {

        val mark = AppData.studySpots.findOne(id)

        if (mark == null) {
            Toast.makeText(
                this,
                "Mark not found",
                Toast.LENGTH_SHORT
            ).show()

            finish()
            return
        }

        titleInput.setText(mark.title)
        descriptionInput.setText(mark.desc)
        xInput.setText(mark.x.toString())
        yInput.setText(mark.y.toString())
    }

    private fun saveMark() {

        val title = titleInput.text.toString().trim()
        val description = descriptionInput.text.toString().trim()

        if (title.isEmpty()) {
            titleLayout.error = "Title is required"
            return
        }

        val x = xInput.text.toString().toDoubleOrNull()

        if (x == null) {
            xLayout.error = "Enter a valid number"
            return
        }

        val y = yInput.text.toString().toDoubleOrNull()

        if (y == null) {
            yLayout.error = "Enter a valid number"
            return
        }

        if (editingId == null || editingId == -1L) {

            val mark = StudySpot(
                title = title,
                desc = description,
                x = x,
                y = y
            )

            AppData.studySpots.create(mark)

            Toast.makeText(
                this,
                "Mark created",
                Toast.LENGTH_SHORT
            ).show()

        } else {

            val mark = StudySpot(
                id = editingId!!,
                title = title,
                desc = description,
                x = x,
                y = y
            )

            AppData.studySpots.update(mark)

            Toast.makeText(
                this,
                "Mark updated",
                Toast.LENGTH_SHORT
            ).show()
        }

        finish()
    }
}
