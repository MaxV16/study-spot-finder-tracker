package org.setu.studyspotfinder.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.setu.studyspotfinder.R
import org.setu.studyspotfinder.models.StudySpot

class StudySpotAdapter(
    private var spots: List<StudySpot>,
    private val onEdit: (StudySpot) -> Unit,
    private val onDelete: (StudySpot) -> Unit
) : RecyclerView.Adapter<StudySpotAdapter.SpotViewHolder>() {

    //holds the views for one row so we dont findViewById every time
    class SpotViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.spotTitle)

        val category: TextView = view.findViewById(R.id.spotCategory)
        val desc: TextView = view.findViewById(R.id.spotDesc)
        val coords: TextView = view.findViewById(R.id.spotCoords)
        val editButton: Button = view.findViewById(R.id.editButton)
        val deleteButton: Button = view.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpotViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_study_spot, parent, false)
        return SpotViewHolder(view)
    }

    override fun onBindViewHolder(holder: SpotViewHolder, position: Int) {
        val spot = spots[position]
        val context = holder.itemView.context

        holder.title.text = context.getString(R.string.title_format, spot.id, spot.title)
        holder.category.text = spot.category
        holder.category.visibility = if (spot.category.isEmpty()) View.GONE else View.VISIBLE
        holder.desc.text = spot.desc
        holder.coords.text = context.getString(R.string.coords_format, spot.x.toString(), spot.y.toString())

        holder.editButton.setOnClickListener { onEdit(spot) }
        holder.deleteButton.setOnClickListener { onDelete(spot) }
    }

    override fun getItemCount(): Int = spots.size

    // swaps le whole list and let the recyclerview animate the change
    fun updateSpots(newSpots: List<StudySpot>) {
        spots = newSpots
        notifyDataSetChanged()
    }
}