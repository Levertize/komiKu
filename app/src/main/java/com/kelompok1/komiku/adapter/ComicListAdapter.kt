package com.kelompok1.komiku.adapter

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kelompok1.komiku.R
import com.kelompok1.komiku.model.Comic
import java.io.File

class ComicListAdapter(
    private val comics: List<Comic>,
    private val onBookmarkClick: ((Comic) -> Unit)? = null,
    private val onItemClick: (Comic) -> Unit
) : RecyclerView.Adapter<ComicListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tv_list_title)
        val tvAuthor: TextView = view.findViewById(R.id.tv_list_author)
        val tvRating: TextView = view.findViewById(R.id.tv_rating)
        val tvBadge: TextView = view.findViewById(R.id.tv_list_badge)
        val ivThumb: ImageView = view.findViewById(R.id.iv_list_thumb)
        val tvViews: TextView = view.findViewById(R.id.tv_views)
        val tvChapters: TextView = view.findViewById(R.id.tv_chapters)
        val tvUpdateTime: TextView = view.findViewById(R.id.tv_update_time)
        val btnBookmark: View = view.findViewById(R.id.btn_list_bookmark)
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
        holder.tvUpdateTime.text = formatTime(comic.lastUpdate)

        // Cover handling: Prefer image path, fallback to gradient
        if (!comic.coverPath.isNullOrEmpty()) {
            val file = File(comic.coverPath)
            if (file.exists()) {
                Glide.with(holder.itemView.context)
                    .load(file)
                    .into(holder.ivThumb)
            } else {
                setGradientCover(holder, comic)
            }
        } else {
            setGradientCover(holder, comic)
        }

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

        if (onBookmarkClick == null) {
            holder.btnBookmark.visibility = View.GONE
        } else {
            holder.btnBookmark.visibility = View.VISIBLE
            holder.btnBookmark.setOnClickListener {
                onBookmarkClick.invoke(comic)
            }
        }

        holder.itemView.setOnClickListener { onItemClick(comic) }
    }

    private fun setGradientCover(holder: ViewHolder, comic: Comic) {
        val gd = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(comic.coverColorStart, comic.coverColorEnd)
        )
        gd.cornerRadius = 8 * holder.itemView.resources.displayMetrics.density
        holder.ivThumb.setImageDrawable(gd)
    }

    private fun formatTime(timestampStr: String): String {
        val timestamp = timestampStr.toLongOrNull() ?: return timestampStr
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < 60 * 1000 -> "Baru saja"
            diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)} menit lalu"
            diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)} jam lalu"
            diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)} hari lalu"
            else -> "1 minggu lalu"
        }
    }

    override fun getItemCount() = comics.size
}
