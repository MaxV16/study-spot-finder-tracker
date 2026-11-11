package org.setu.studyspotfinder.ui


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.ArrayAdapter
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import coil.load
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import org.setu.studyspotfinder.R
import org.setu.studyspotfinder.data.AppData
import org.setu.studyspotfinder.data.LocationHelper
import org.setu.studyspotfinder.models.StudySpot

// this one screen does double duty, it adds a spot and edits one.
// how does it know wich? the id extra in the intent, no id = add, an id = edit that spot.
class AddEditActivity : AppCompatActivity() {

    // the actual input boxes
    private lateinit var titleInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var xInput: EditText
    private lateinit var yInput: EditText

    // the material wrappers around em, they give the floating label and the red error text
    private lateinit var titleLayout: TextInputLayout
    private lateinit var descriptionLayout: TextInputLayout

    private lateinit var xLayout: TextInputLayout

    private lateinit var yLayout: TextInputLayout
    private lateinit var categoryInput: MaterialAutoCompleteTextView
    private lateinit var categoryLayout: TextInputLayout

    // fills the lat/lon boxes from the gps
    private lateinit var useLocationButton: MaterialButton

    // only asks when the user actually taps the button, not when the screen opens
    private val locationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                fillCurrentLocation()
            } else {
                Toast.makeText(this, R.string.location_denied, Toast.LENGTH_LONG).show()
            }
        }

    // the photo preview and the uri the user picked, null means no photo
    private lateinit var imagePreview: ImageView
    private var selectedImageUri: Uri? = null

    // opendocument gives us a uri we can keep after a restart, getcontent does not
    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            if (uri != null) {
                try {
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: SecurityException) {
                    // some gallery apps dont allow persistable access, the photo still shows for now
                }

                selectedImageUri = uri
                imagePreview.load(uri)
            }
        }

    // null means we are adding, if its set we are editing that spot
    private var editingId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createUserInterface()

        // read the id main passed in, nobody passed one so we get -1L back.
        // -1L means no id, so we know we are adding a brand new spot.
        editingId = intent.getLongExtra("id", -1L)

        // when the map long press starts us off we get lat/lng extras.
        // only use them for a new spot, never for an edit or we'd move the old one.
        val startLat = intent.getDoubleExtra("lat", Double.NaN)
        val startLng = intent.getDoubleExtra("lng", Double.NaN)

        if (editingId == -1L && !startLat.isNaN() && !startLng.isNaN()) {
            xInput.setText(startLat.toString())
            yInput.setText(startLng.toString())
        }

        // if we do have a real id, fill the form with that spots current values
        if (editingId != -1L) {
            loadExistingSpot(editingId!!)
        }

        supportActionBar?.title =
            if (editingId == -1L) getString(R.string.add_spot) else getString(R.string.edit_spot)
    }


    // the up arrow in the toolbar, finish() closes this screen and goes back to MainActivity
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    // build the form
    private fun createUserInterface() {

        val pad = resources.getDimensionPixelSize(R.dimen.screen_padding)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
        }

        val toolbar = MaterialToolbar(this).apply {
            title = getString(R.string.add_spot)
            setBackgroundColor(context.getColor(R.color.spot_primary))
            setTitleTextColor(context.getColor(R.color.white))
        }

        root.addView(
            toolbar,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        setSupportActionBar(toolbar)
        // this shows the back/up arrow in the bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        titleInput = TextInputEditText(this)
        descriptionInput = TextInputEditText(this)

        // x and y only take numbers with decimals so the keyboard and validation match up
        xInput = TextInputEditText(this).apply {
            inputType =
                InputType.TYPE_CLASS_NUMBER or
                        InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

        yInput = TextInputEditText(this).apply {
            inputType =
                InputType.TYPE_CLASS_NUMBER or
                        InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

        // each input gets wrapped in a TextInputLayout, thats what gives it the floating hint
        titleLayout = TextInputLayout(this).apply {
            hint = getString(R.string.title_hint)
            addView(titleInput)
        }

        descriptionLayout = TextInputLayout(this).apply {
            hint = getString(R.string.description_hint)
            addView(descriptionInput)
        }

        xLayout = TextInputLayout(this).apply {
            hint = getString(R.string.x_hint)
            addView(xInput)
        }

        yLayout = TextInputLayout(this).apply {
            hint = getString(R.string.y_hint)
            addView(yInput)
        }

        // button sits right under the two coordinate boxes
        useLocationButton = MaterialButton(this).apply {
            text = getString(R.string.use_my_location)
            setOnClickListener {
                val granted = ContextCompat.checkSelfPermission(
                    this@AddEditActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                if (granted) {
                    fillCurrentLocation()
                } else {
                    locationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }
        }

        // the category dropdown, we pull the options out of res/values/arrays.xml
        val categories = resources.getStringArray(R.array.categories)

        categoryInput = MaterialAutoCompleteTextView(this).apply {
            // TYPE_NULL stops the normal keyboard popping up, we want the dropdown instead
            inputType = InputType.TYPE_NULL
            // the adapter feeds the options to the dropdown
            setAdapter(ArrayAdapter(this@AddEditActivity, android.R.layout.simple_list_item_1, categories))
        }

        categoryLayout = TextInputLayout(this).apply {
            hint = getString(R.string.category_hint)
            // this draws the little dropdown arrow on the end of the field
            endIconMode = TextInputLayout.END_ICON_DROPDOWN_MENU
            addView(categoryInput)
        }

        // the photo preview, starts with the gallery icon so there is always something there
        imagePreview = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (180 * resources.displayMetrics.density).toInt()
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
            setImageResource(android.R.drawable.ic_menu_gallery)
        }

        val pickButton = MaterialButton(this).apply {
            text = getString(R.string.choose_photo)
            setOnClickListener { imagePicker.launch(arrayOf("image/*")) }
        }

        // Save runs saveSpot(), Cancel just closes the screen
        val saveButton = MaterialButton(this).apply {
            text = getString(R.string.save)
            setOnClickListener {
                saveSpot()
            }
        }

        val cancelButton = MaterialButton(this).apply {
            text = getString(R.string.cancel)
            setOnClickListener {
                finish()
            }
        }

        root.addView(titleLayout)
        root.addView(descriptionLayout)
        root.addView(xLayout)
        root.addView(yLayout)
        root.addView(useLocationButton)
        root.addView(categoryLayout)
        root.addView(imagePreview)
        root.addView(pickButton)
        root.addView(saveButton)
        root.addView(cancelButton)

        setContentView(root)
    }

    // asks the fused provider for one fix and drops it into x/y
    private fun fillCurrentLocation() {
        LocationHelper(this).getCurrentLocation { location ->
            if (location == null) {
                Toast.makeText(this, R.string.location_unavailable, Toast.LENGTH_LONG).show()
                return@getCurrentLocation
            }

            xInput.setText(location.latitude.toString())
            yInput.setText(location.longitude.toString())
        }
    }

    // grab the spot from the store and push its values into the fields
    private fun loadExistingSpot(id: Long) {

        val spot = AppData.studySpots.findOne(id)

        // unlikely but if the spot is gone, eg deleted somewhere else, tell the user and bail out
        if (spot == null) {
            Toast.makeText(
                this,
                getString(R.string.spot_not_found),
                Toast.LENGTH_SHORT
            ).show()

            finish()
            return
        }

        titleInput.setText(spot.title)
        descriptionInput.setText(spot.desc)
        // numbers get turned into text to go in the box
        xInput.setText(spot.x.toString())
        yInput.setText(spot.y.toString())
        // the false here means dont run the dropdown filter, we just wanna set the value
        categoryInput.setText(spot.category, false)

        // put the saved photo back in the preview if there is one
        selectedImageUri = spot.imageUri?.let(Uri::parse)

        if (selectedImageUri != null) {
            imagePreview.load(selectedImageUri)
        } else {
            imagePreview.setImageResource(android.R.drawable.ic_menu_gallery)
        }
    }

    // runs when Save is tapped, validate eveything then either create or update
    private fun saveSpot() {

        // trim removes accidental spaces at the start/end
        val title = titleInput.text.toString().trim()
        val description = descriptionInput.text.toString().trim()

        // no title means show the error under the box and stop
        if (title.isEmpty()) {
            titleLayout.error = getString(R.string.err_title_required)
            return
        }

        titleLayout.error = null

        // toDoubleOrNull gives us the number, or null if what was typed isnt a number
        val x = xInput.text.toString().toDoubleOrNull()

        // lat has to be between -90 and 90, anything else is not on earth
        if (x == null || x !in -90.0..90.0) {
            xLayout.error = getString(R.string.err_latitude)
            return
        }

        xLayout.error = null

        val y = yInput.text.toString().toDoubleOrNull()

        // lng goes from -180 to 180
        if (y == null || y !in -180.0..180.0) {
            yLayout.error = getString(R.string.err_longitude)
            return
        }

        yLayout.error = null

        val spot = StudySpot(
            id = editingId?.takeIf { it != -1L } ?: 0L,
            title = title,
            desc = description,
            x = x,
            y = y,
            category = categoryInput.text.toString(),
            imageUri = selectedImageUri?.toString()
        )

        // the model checks the rules again, this catches anything the form missed
        try {
            spot.validate()
        } catch (e: IllegalArgumentException) {
            Toast.makeText(this, e.message ?: "Check the entered values.", Toast.LENGTH_LONG).show()
            return
        }

        // no editingId means its a new spot so create it, the store gives it an id
        if (editingId == null || editingId == -1L) {

            AppData.studySpots.create(spot)

            Toast.makeText(
                this,
                getString(R.string.spot_created),
                Toast.LENGTH_SHORT
            ).show()

        } else {

            // we have an id so its an edit, the id is already set above
            AppData.studySpots.update(spot)

            Toast.makeText(
                this,
                getString(R.string.spot_updated),
                Toast.LENGTH_SHORT
            ).show()
        }

        // close the form, we go back to MainActivity wich refreshes the list in onResume
        finish()
    }
}
