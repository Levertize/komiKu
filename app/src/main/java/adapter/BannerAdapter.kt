package com.kelompok1.komiku.adapter

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kelompok1.komiku.R
import com.kelompok1.komiku.model.Comic

class BannerAdapter(
    private val banners: List<Comic>
) : RecyclerView.Adapter<BannerAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val bannerBg: View = view.findViewById(R.id.banner_bg)
        val tvTitle: TextView = view.findViewById(R.id.tv_banner_title)
        val tvMeta: TextView = view.findViewById(R.id.tv_banner_meta)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_banner, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comic = banners[position]
        val density = holder.itemView.resources.displayMetrics.density

        // Set gradient background sesuai warna komik
        val gradient = GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(comic.coverColorStart, comic.coverColorEnd)
        )
        gradient.cornerRadius = 16f * density
        holder.bannerBg.background = gradient

        // Judul + chapter
        holder.tvTitle.text = "${comic.title}\n${comic.chapter}"

        // Author + update
        holder.tvMeta.text = "${comic.author} · ${comic.lastUpdate}"
    }

    override fun getItemCount() = banners.size
}
