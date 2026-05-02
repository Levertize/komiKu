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

class BannerAdapter(private val banners: List<Comic>) :
    RecyclerView.Adapter<BannerAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tv_banner_title)
        val tvMeta: TextView = view.findViewById(R.id.tv_banner_meta)
        val bannerBg: View = view.findViewById(R.id.banner_bg)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_banner, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comic = banners[position]
        holder.tvTitle.text = comic.title
        holder.tvMeta.text = "oleh ${comic.author} · ${comic.genre.joinToString(", ")}"

        val gd = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(comic.coverColorStart, comic.coverColorEnd)
        )
        gd.cornerRadius = 18 * holder.itemView.resources.displayMetrics.density
        holder.bannerBg.background = gd
    }

    override fun getItemCount() = banners.size
}
