package com.kelompok1.komiku

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.kelompok1.komiku.adapter.ReadingPageAdapter
import com.kelompok1.komiku.databinding.ActivityReadingBinding

class ReadingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReadingBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        binding = ActivityReadingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ambil data dari Intent
        val title = intent.getStringExtra("comic_title") ?: "Judul Komik"
        val chapter = intent.getStringExtra("comic_chapter") ?: "Chapter 1"
        val colorStart = intent.getIntExtra("color_start", 0xFF1A0E35.toInt())
        val colorEnd = intent.getIntExtra("color_end", 0xFF0D1840.toInt())
        val totalPages = 24

        // Setup topbar
        binding.tvReadComicTitle.text = title
        binding.tvReadChapter.text = "$chapter · Hal. 1/$totalPages"

        // Setup RecyclerView
        val adapter = ReadingPageAdapter(totalPages, colorStart, colorEnd)
        binding.rvPages.apply {
            layoutManager = LinearLayoutManager(this@ReadingActivity)
            this.adapter = adapter
        }

        // Tombol back
        binding.btnReadBack.setOnClickListener { finish() }

        // Tombol prev/next chapter (placeholder)
        binding.btnPrevChapter.setOnClickListener {
            binding.tvReadChapter.text = "Chapter sebelumnya · Hal. 1/$totalPages"
        }
        binding.btnNextChapter.setOnClickListener {
            binding.tvReadChapter.text = "Chapter berikutnya · Hal. 1/$totalPages"
        }
        // Force dark di reading mode
        window.statusBarColor = android.graphics.Color.parseColor("#07070F")
        window.navigationBarColor = android.graphics.Color.parseColor("#07070F")
    }

}