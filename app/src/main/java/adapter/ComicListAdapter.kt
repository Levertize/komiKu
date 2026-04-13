package com.kelompok1.komiku.adapter

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kelompok1.komiku.R
import com.kelompok1.komiku.model.Comic

class ComicListAdapter(
    private val comics: List<Comic>,
    private val onClick: (Comic) -> Unit = {}
) : RecyclerView.Adapter<ComicListAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val vThumb: View = view.findViewById(R.id.iv_list_thumb)
        val tvBadge: TextView = view.findViewById(R.id.tv_list_badge)
        val tvTitle: TextView = view.findViewById(R.id.tv_list_title)
        val tvAuthor: TextView = view.findViewById(R.id.tv_list_author)
        val llTags: LinearLayout = view.findViewById(R.id.ll_tags)
        val tvViews: TextView = view.findViewById(R.id.tv_views)
        val tvChapters: TextView = view.findViewById(R.id.tv_chapters)
        val tvRating: TextView = view.findViewById(R.id.tv_rating)
        val tvUpdate: TextView = view.findViewById(R.id.tv_update_time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comic_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comic = comics[position]
        val dp = holder.itemView.resources.displayMetrics.density

        // Cover gradient sebagai background
        val gradient = GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(comic.coverColorStart, comic.coverColorEnd)
        )
        gradient.cornerRadius = 10f * dp  // ← tambah/update ini
        holder.vThumb.background = gradient

        // Badge
        if (comic.badge.isNotEmpty()) {
            holder.tvBadge.visibility = View.VISIBLE
            holder.tvBadge.text = comic.badge
            val color = when (comic.badge) {
                "HOT"    -> 0xFFF43F5E.toInt()
                "NEW"    -> 0xFF10B981.toInt()
                "UPDATE" -> 0xFFF59E0B.toInt()
                else     -> 0xFF7C5CFC.toInt()
            }
            val bg = GradientDrawable().also { it.setColor(color); it.cornerRadius = 3f * dp }
            holder.tvBadge.background = bg
        } else {
            holder.tvBadge.visibility = View.GONE
        }

        holder.tvTitle.text = comic.title
        holder.tvAuthor.text = comic.author
        holder.tvViews.text = comic.views
        holder.tvChapters.text = comic.chapter
        holder.tvRating.text = comic.rating.toString()
        holder.tvUpdate.text = comic.lastUpdate

        // Tags
        holder.llTags.removeAllViews()
        val tags = listOf(comic.format) + comic.genre.take(2)
        tags.forEach { tag ->
            val tv = TextView(holder.itemView.context).apply {
                text = tag
                textSize = 7.5f
                val isFormat = tag == comic.format
                setTextColor(if (isFormat) 0xFFC084FC.toInt() else 0x73F0EEF8.toInt())
                val bg = GradientDrawable().also {
                    it.cornerRadius = 4f * dp
                    it.setColor(if (isFormat) 0x14C084FC.toInt() else 0xFF1A1A2E.toInt())
                }
                background = bg
                setPadding((6*dp).toInt(), (2*dp).toInt(), (6*dp).toInt(), (2*dp).toInt())
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { p -> p.marginEnd = (4*dp).toInt() }
            }
            holder.llTags.addView(tv)
        }

        holder.itemView.setOnClickListener { onClick(comic) }
    }

    override fun getItemCount() = comics.size
}
