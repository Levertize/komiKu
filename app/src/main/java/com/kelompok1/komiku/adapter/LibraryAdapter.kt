package com.kelompok1.komiku.adapter

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kelompok1.komiku.R
import com.kelompok1.komiku.model.LibraryComicJoin

class LibraryAdapter(
    private val items: List<LibraryComicJoin>,
    private val onClick: (LibraryComicJoin) -> Unit
) : RecyclerView.Adapter<LibraryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tv_lib_title)
        val tvProgress: TextView = view.findViewById(R.id.tv_lib_chapter)
        val progressBar: ProgressBar = view.findViewById(R.id.pb_lib_progress)
        val ivThumb: View = view.findViewById(R.id.iv_lib_thumb)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_library_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvTitle.text = item.comic.title
        
        // Calculate progress
        val progress = if (item.library.totalChapter > 0) 
            ((item.library.currentChapter.toFloat() / item.library.totalChapter) * 100).toInt() 
            else 0
        val progressText = "Ch. ${item.library.currentChapter} / ${item.library.totalChapter}"
        
        holder.tvProgress.text = progressText
        holder.progressBar.progress = progress

        val gd = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(item.comic.coverColorStart, item.comic.coverColorEnd)
        )
        gd.cornerRadius = 18 * holder.itemView.resources.displayMetrics.density
        holder.ivThumb.background = gd

        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = items.size
}
