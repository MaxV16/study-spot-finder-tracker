package org.setu.studyspotfinder.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.setu.studyspotfinder.R
import org.setu.studyspotfinder.models.StudySpot

// the adapter is the glue between our list of spots and the RecyclerView.
// the RecyclerView handles the scrolling, the adapter's job is to draw ONE row per spot.
// we pass in two lambdas (onEdit/onDelete) so this file doesnt need to know about activities,
// MainActivity decides what actually happens when a button is tapped.
class StudySpotAdapter(
    private var spots: List<StudySpot>,          // the spots we are currently showing
    private val onEdit: (StudySpot) -> Unit,     // what to run when Edit is tapped
    private val onDelete: (StudySpot) -> Unit    // what to run when Delete is tapped
) : RecyclerView.Adapter<StudySpotAdapter.SpotViewHolder>() {

    // a ViewHolder just holds the views of one row so we don't call findViewById over and over while scrolling.
    // RecyclerView reuses (recycles) these rows instead of making new ones, thats why it scrolls so smooth.
    class SpotViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.spotTitle)
        val category: TextView = view.findViewById(R.id.spotCategory)
        val desc: TextView = view.findViewById(R.id.spotDesc)
        val coords: TextView = view.findViewById(R.id.spotCoords)
        val editButton: Button = view.findViewById(R.id.editButton)
        val deleteButton: Button = view.findViewById(R.id.deleteButton)
    }

    // called when the RecyclerView needs a brand new row. we inflate our item_study_spot.xml into a view.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpotViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_study_spot, parent, false)
        return SpotViewHolder(view)
    }

    // called to fill a row with the data of the spot at this position. this runs a lot while scrolling.
    override fun onBindViewHolder(holder: SpotViewHolder, position: Int) {
        val spot = spots[position]
        val context = holder.itemView.context

        // title_format is "%1$d: %2$s" so this prints like "3: Library" (id then title)
        holder.title.text = context.getString(R.string.title_format, spot.id, spot.title)
        holder.category.text = spot.category
        // no category? hide the label completely so we dont show an empty line
        holder.category.visibility = if (spot.category.isEmpty()) View.GONE else View.VISIBLE
        holder.desc.text = spot.desc
        holder.coords.text = context.getString(R.string.coords_format, spot.x.toString(), spot.y.toString())

        // hand the spot back up to MainActivity through the lambdas
        holder.editButton.setOnClickListener { onEdit(spot) }
        holder.deleteButton.setOnClickListener { onDelete(spot) }
    }

    // how many rows to draw
    override fun getItemCount(): Int = spots.size

    // MainActivity calls this after it filters/sorts. swap the list then tell the RecyclerView to redraw.
    fun updateSpots(newSpots: List<StudySpot>) {
        spots = newSpots
        notifyDataSetChanged()
    }
}
