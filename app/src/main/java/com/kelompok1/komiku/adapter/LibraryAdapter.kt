package com.kelompok1.komiku.adapter

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kelompok1.komiku.R
import com.kelompok1.komiku.model.Comic
import com.kelompok1.komiku.model.LibraryComicJoin
import java.io.File

class LibraryAdapter(
    private val items: List<LibraryComicJoin>,
    private val onDeleteClick: (LibraryComicJoin) -> Unit,
    private val onItemClick: (LibraryComicJoin) -> Unit
) : RecyclerView.Adapter<LibraryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tv_lib_title)
        val tvChapter: TextView = view.findViewById(R.id.tv_lib_chapter)
        val progressBar: ProgressBar = view.findViewById(R.id.pb_lib_progress)
        val ivThumb: ImageView = view.findViewById(R.id.iv_lib_thumb)
        val btnDelete: View = view.findViewById(R.id.btn_delete_lib)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_library_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val comic = item.comic
        val lib = item.library

        holder.tvTitle.text = comic.title
        holder.tvChapter.text = "Ch. ${lib.currentChapter} / ${lib.totalChapter}"
        
        val progress = if (lib.totalChapter > 0) {
            (lib.currentChapter.toFloat() / lib.totalChapter * 100).toInt()
        } else 0
        holder.progressBar.progress = progress

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

        holder.btnDelete.setOnClickListener { onDeleteClick(item) }
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    private fun setGradientCover(holder: ViewHolder, comic: Comic) {
        val gd = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(comic.coverColorStart, comic.coverColorEnd)
        )
        gd.cornerRadius = 14 * holder.itemView.resources.displayMetrics.density
        holder.ivThumb.setImageDrawable(gd)
    }

    override fun getItemCount() = items.size
}
