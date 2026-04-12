package com.kelompok1.komiku.adapter

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kelompok1.komiku.R
import com.kelompok1.komiku.data.DummyData

class LibraryAdapter(
    private val items: List<DummyData.LibraryComic>,
    private val onClick: (DummyData.LibraryComic) -> Unit = {}
) : RecyclerView.Adapter<LibraryAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val vThumb: View = view.findViewById(R.id.iv_lib_thumb)
        val tvTitle: TextView = view.findViewById(R.id.tv_lib_title)
        val progressBar: ProgressBar = view.findViewById(R.id.pb_lib_progress)
        val tvChapter: TextView = view.findViewById(R.id.tv_lib_chapter)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_library_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val dp = holder.itemView.resources.displayMetrics.density

        // Cover gradient sebagai background
        val gradient = GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(item.comic.coverColorStart, item.comic.coverColorEnd)
        )
        holder.vThumb.background = gradient

        holder.tvTitle.text = item.comic.title
        holder.progressBar.progress = item.progress
        holder.tvChapter.text = item.progressText

        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = items.size
}
