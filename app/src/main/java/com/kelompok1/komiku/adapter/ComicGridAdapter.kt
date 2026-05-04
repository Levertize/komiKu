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

class ComicGridAdapter(
    private val comics: List<Comic>,
    private val onItemClick: ((Comic) -> Unit)? = null
) : RecyclerView.Adapter<ComicGridAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tv_comic_title)
        val tvChapter: TextView = view.findViewById(R.id.tv_chapter)
        val ivThumb: ImageView = view.findViewById(R.id.iv_comic_thumb)
        val tvBadge: TextView = view.findViewById(R.id.tv_badge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comic_grid, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comic = comics[position]
        holder.tvTitle.text = comic.title
        holder.tvChapter.text = "${comic.format} · ${comic.chapter}"

        // Cover handling
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

        holder.itemView.setOnClickListener { onItemClick?.invoke(comic) }
    }

    private fun setGradientCover(holder: ViewHolder, comic: Comic) {
        val gd = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(comic.coverColorStart, comic.coverColorEnd)
        )
        gd.cornerRadius = 11 * holder.itemView.resources.displayMetrics.density
        holder.ivThumb.setImageDrawable(gd)
    }

    override fun getItemCount() = comics.size
}
