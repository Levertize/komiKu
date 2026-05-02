package com.kelompok1.komiku.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kelompok1.komiku.R
import com.kelompok1.komiku.model.Chapter

class ChapterAdapter(
    private val chapters: List<Chapter>,
    private val onReadClick: (Chapter) -> Unit
) : RecyclerView.Adapter<ChapterAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNumber: TextView = view.findViewById(R.id.tv_chapter_number)
        val tvTitle: TextView = view.findViewById(R.id.tv_chapter_title)
        val tvDate: TextView = view.findViewById(R.id.tv_chapter_date)
        val tvRead: TextView = view.findViewById(R.id.tv_read_chapter)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chapter, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ch = chapters[position]
        holder.tvNumber.text = "${ch.number}"
        holder.tvTitle.text = ch.title
        holder.tvDate.text = ch.uploadDate
        holder.tvRead.setOnClickListener { onReadClick(ch) }
        holder.itemView.setOnClickListener { onReadClick(ch) }
    }

    override fun getItemCount() = chapters.size
}
