package com.kelompok1.komiku.adapter

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kelompok1.komiku.R
import com.kelompok1.komiku.model.Comic

class ComicGridAdapter(
    private val comics: List<Comic>,
    private val onClick: (Comic) -> Unit = {}
) : RecyclerView.Adapter<ComicGridAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // iv_comic_thumb sekarang pakai View biasa, background = gradient
        val vThumb: View = view.findViewById(R.id.iv_comic_thumb)
        val tvBadge: TextView = view.findViewById(R.id.tv_badge)
        val tvTitle: TextView = view.findViewById(R.id.tv_comic_title)
        val tvChapter: TextView = view.findViewById(R.id.tv_chapter)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comic_grid, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comic = comics[position]
        val dp = holder.itemView.resources.displayMetrics.density

        // Gradient cover — set sebagai BACKGROUND, bukan image
        val gradient = GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(comic.coverColorStart, comic.coverColorEnd)
        )
        gradient.cornerRadius = 10f * dp
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
            val bg = GradientDrawable().also {
                it.setColor(color)
                it.cornerRadius = 4f * dp
            }
            holder.tvBadge.background = bg
        } else {
            holder.tvBadge.visibility = View.GONE
        }

        holder.tvTitle.text = comic.title
        holder.tvChapter.text = comic.chapter
        holder.itemView.setOnClickListener { onClick(comic) }
    }

    override fun getItemCount() = comics.size
}
