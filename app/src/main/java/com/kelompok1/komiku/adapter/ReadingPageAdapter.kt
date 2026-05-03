package com.kelompok1.komiku.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kelompok1.komiku.R
import java.io.File

class ReadingPageAdapter(private val imagePaths: List<String>) :
    RecyclerView.Adapter<ReadingPageAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivPage: ImageView = view.findViewById(R.id.iv_reading_page)
        val tvPageNum: TextView = view.findViewById(R.id.tv_page_num)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reading_page, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val path = imagePaths[position]

        if (path.startsWith("dummy_color_")) {
            // Mock behavior for dummy pages
            val colorStr = path.replace("dummy_color_", "")
            holder.ivPage.setImageDrawable(null)
            holder.ivPage.setBackgroundColor(Color.parseColor(colorStr))
        } else {
            val file = File(path)
            if (file.exists()) {
                holder.ivPage.setBackgroundColor(Color.TRANSPARENT)
                Glide.with(holder.itemView.context)
                    .load(file)
                    .into(holder.ivPage)
            }
        }

        holder.tvPageNum.text = "Halaman ${position + 1}"
    }

    override fun getItemCount() = imagePaths.size
}
