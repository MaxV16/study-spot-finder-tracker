package org.setu.studyspotfinder.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.setu.studyspotfinder.R
import org.setu.studyspotfinder.models.StudySpot

class StudySpotAdapter( //this is the glue between list of spots and recyclerview which is the list, rcyclerview handles scroll, adapters job is to draw one row per spot
// we pass in 2 lambds(onedit/ondelete) so this file doesnt know about activities. mainactivity decides what is done when a buttons tapped.
    private var spots: List<StudySpot>,          // the spots we are currently showing
    private val onEdit: (StudySpot) -> Unit,     // takes a study spot and returns nothing (Unit) then calls it onEdit
    private val onDelete: (StudySpot) -> Unit    // takes a study spot and returns nothing
) : RecyclerView.Adapter<StudySpotAdapter.SpotViewHolder>() {

    // RecyclerView reuses (recycles) these rows instead of making new ones, thats why it scrolls so smooth.
    class SpotViewHolder(view: View) : RecyclerView.ViewHolder(view) { //holds the views of one row, so findviewbyid isnt called over and over
        //recyclver view literally recycles these rows instead of making new ones, hence le smooth scroll.
        val title: TextView = view.findViewById(R.id.spotTitle) //finds the spot title inside the row and stores it.
        val category: TextView = view.findViewById(R.id.spotCategory) //finds the spot category inside the row and stores it.
        val desc: TextView = view.findViewById(R.id.spotDesc) //finds the spot description inside the row and stores it.
        val coords: TextView = view.findViewById(R.id.spotCoords) //finds the spot coordinates inside the row and stores it.
        val editButton: Button = view.findViewById(R.id.editButton) //finds the edit button inside the row and stores it.
        val deleteButton: Button = view.findViewById(R.id.deleteButton) //finds the delete button inside the row and stores it.
    }

    // called when the RecyclerView needs a brand new row. we inflate our item_study_spot.xml into a view.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpotViewHolder { //called when the recycler view needs a new row.
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_study_spot, parent, false) //inflates the row into a view.
        return SpotViewHolder(view)
    }

    override fun onBindViewHolder(holder: SpotViewHolder, position: Int) { //called when the recycler needs to fill a row with data at this position.
        val spot = spots[position] // get the spot from the list
        val context = holder.itemView.context // get the context of the row, context is the activity.

        // title_format is "%1$d: %2$s" so this prints like "3: Library" (id then title)
        holder.title.text = context.getString(R.string.title_format, spot.id, spot.title)
        holder.category.text = spot.category //sets the category of the spot.

        holder.category.visibility = if (spot.category.isEmpty()) View.GONE else View.VISIBLE //hides the label if the category is empty.
        holder.desc.text = spot.desc //sets the description of the spot.
        holder.coords.text = context.getString(R.string.coords_format, spot.x.toString(), spot.y.toString()) //sets the coordinates of the spot.

        holder.editButton.setOnClickListener { onEdit(spot) } //when the edit button is tapped, call the onEdit lambda with the spot
        holder.deleteButton.setOnClickListener { onDelete(spot) } //when the delete button is tapped, call the onDelete lambda with the spot. hands it over to MainActivity.
    }

    override fun getItemCount(): Int = spots.size //returns the number of spots.

    // MainActivity calls this after it filters/sorts, swaps the list and then tells the RecyclerView to redraw.
    fun updateSpots(newSpots: List<StudySpot>) { //takes a list of spots and sets the spots to that list
        spots = newSpots
        notifyDataSetChanged()
    }
}
