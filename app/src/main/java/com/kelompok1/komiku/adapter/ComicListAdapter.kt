package com.kelompok1.komiku.adapter

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kelompok1.komiku.R
import com.kelompok1.komiku.model.Comic

class ComicListAdapter(
    private val comics: List<Comic>,
    private val onItemClick: (Comic) -> Unit
) : RecyclerView.Adapter<ComicListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tv_list_title)
        val tvAuthor: TextView = view.findViewById(R.id.tv_list_author)
        val tvRating: TextView = view.findViewById(R.id.tv_rating)
        val tvBadge: TextView = view.findViewById(R.id.tv_list_badge)
        val ivThumb: View = view.findViewById(R.id.iv_list_thumb)
        val tvViews: TextView = view.findViewById(R.id.tv_views)
        val tvChapters: TextView = view.findViewById(R.id.tv_chapters)
        val tvUpdateTime: TextView = view.findViewById(R.id.tv_update_time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comic_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comic = comics[position]
        holder.tvTitle.text = comic.title
        holder.tvAuthor.text = comic.author
        holder.tvRating.text = comic.rating.toString()
        holder.tvViews.text = comic.views
        holder.tvChapters.text = comic.chapter
        holder.tvUpdateTime.text = comic.lastUpdate

        // Cover gradient
        val gd = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(comic.coverColorStart, comic.coverColorEnd)
        )
        gd.cornerRadius = 8 * holder.itemView.resources.displayMetrics.density
        holder.ivThumb.background = gd

        // Badge
        if (comic.badge.isNotEmpty()) {
            holder.tvBadge.visibility = View.VISIBLE
            holder.tvBadge.text = comic.badge
            when (comic.badge) {
                "HOT" -> holder.tvBadge.setBackgroundResource(R.drawable.bg_badge_hot)
                "NEW" -> holder.tvBadge.setBackgroundResource(R.drawable.bg_badge_new)
                "UPDATE" -> holder.tvBadge.setBackgroundResource(R.drawable.bg_badge_accent)
            }
        } else {
            holder.tvBadge.visibility = View.GONE
        }

        holder.itemView.setOnClickListener { onItemClick(comic) }
    }

    override fun getItemCount() = comics.size
}
