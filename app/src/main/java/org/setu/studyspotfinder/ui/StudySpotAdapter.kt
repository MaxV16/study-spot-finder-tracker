package org.setu.studyspotfinder.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import coil.load
import org.setu.studyspotfinder.R
import org.setu.studyspotfinder.databinding.ItemStudySpotBinding
import org.setu.studyspotfinder.models.StudySpot

// draws one row per spot. instead of holding buttons it now takes one listener,
// so the adapter still doesnt know what clicking edit or delete actually does.
class StudySpotAdapter(
    private var spots: List<StudySpot>,
    private val listener: StudySpotListener
) : RecyclerView.Adapter<StudySpotAdapter.SpotViewHolder>() {

    // viewbinding means no more findviewbyid for every row
    inner class SpotViewHolder(
        val binding: ItemStudySpotBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpotViewHolder {
        val binding = ItemStudySpotBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SpotViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SpotViewHolder, position: Int) {
        val spot = spots[position]
        val context = holder.itemView.context

        // coil handles both file uris and content uris, setimageresource would choke on a uri string
        val image = spot.imageUri
        if (image.isNullOrBlank()) {
            holder.binding.spotImage.visibility = View.GONE
        } else {
            holder.binding.spotImage.visibility = View.VISIBLE
            holder.binding.spotImage.load(image) {
                crossfade(true)
                placeholder(android.R.drawable.ic_menu_gallery)
                error(android.R.drawable.ic_menu_gallery)
            }
        }

        holder.binding.spotTitle.text =
            context.getString(R.string.title_format, spot.id, spot.title)
        holder.binding.spotCategory.text = spot.category
        holder.binding.spotCategory.visibility =
            if (spot.category.isBlank()) View.GONE else View.VISIBLE
        holder.binding.spotDesc.text = spot.desc
        holder.binding.spotCoords.text =
            context.getString(R.string.coords_format, spot.x.toString(), spot.y.toString())

        // the whole card is the tap target now
        holder.binding.root.setOnClickListener { listener.onSpotClicked(spot) }
        holder.binding.spotMenu.setOnClickListener { showMenu(it, spot) }
    }

    // the little three dot menu on the row
    private fun showMenu(anchor: View, spot: StudySpot) {
        PopupMenu(anchor.context, anchor).apply {
            menuInflater.inflate(R.menu.menu_spot_item, menu)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_view -> listener.onSpotClicked(spot)
                    R.id.action_edit -> listener.onEdit(spot)
                    R.id.action_delete -> listener.onDelete(spot)
                    else -> return@setOnMenuItemClickListener false
                }
                true
            }
        }.show()
    }

    // needed by the swipe callback later, it only gets a position
    fun spotAt(position: Int): StudySpot? = spots.getOrNull(position)

    fun updateSpots(newSpots: List<StudySpot>) {
        spots = newSpots
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = spots.size
}