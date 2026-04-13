package com.kelompok1.komiku.adapter

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kelompok1.komiku.R

class ReadingPageAdapter(
    private val totalPages: Int,
    private val colorStart: Int,
    private val colorEnd: Int
) : RecyclerView.Adapter<ReadingPageAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val pageBg: View = view.findViewById(R.id.page_bg)
        val tvPageNum: TextView = view.findViewById(R.id.tv_page_num)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reading_page, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dp = holder.itemView.resources.displayMetrics.density
        val gradient = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(colorStart, colorEnd)
        )
        holder.pageBg.background = gradient
        holder.tvPageNum.text = "${position + 1} / $totalPages"
    }

    override fun getItemCount() = totalPages
}