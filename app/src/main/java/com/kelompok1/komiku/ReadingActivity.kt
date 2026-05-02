package com.kelompok1.komiku

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.kelompok1.komiku.adapter.ReadingPageAdapter
import com.kelompok1.komiku.data.DummyData
import com.kelompok1.komiku.databinding.ActivityReadingBinding

class ReadingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReadingBinding

    companion object {
        const val EXTRA_COMIC_TITLE   = "comic_title"
        const val EXTRA_CHAPTER_TITLE = "chapter_title"
        const val EXTRA_CHAPTER_ID    = "chapter_id"
        const val EXTRA_COMIC_ID      = "comic_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityReadingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handle icon colors in system bars
        val isDark = (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
        WindowInsetsControllerCompat(window, binding.root).apply {
            isAppearanceLightStatusBars = !isDark
            isAppearanceLightNavigationBars = !isDark
        }

        // Setup dynamic insets for Top and Bottom bars
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            val navBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            
            binding.topInsetView.layoutParams.height = statusBars.top
            binding.bottomInsetView.layoutParams.height = navBars.bottom
            
            // Adjust scroll thumb top margin to status bar
            val params = binding.scrollThumb.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
            params.topMargin = statusBars.top + (16 * resources.displayMetrics.density).toInt()
            binding.scrollThumb.layoutParams = params
            
            insets
        }

        val comicTitle   = intent.getStringExtra(EXTRA_COMIC_TITLE) ?: ""
        val chapterTitle = intent.getStringExtra(EXTRA_CHAPTER_TITLE) ?: ""
        val chapterId    = intent.getIntExtra(EXTRA_CHAPTER_ID, 1)
        val comicId      = intent.getIntExtra(EXTRA_COMIC_ID, 1)

        // Setup top bar info
        binding.tvReadComicTitle.text = comicTitle
        binding.tvReadChapter.text = chapterTitle

        // Setup pages
        val pages = DummyData.getDummyPages(chapterId)
        binding.rvPages.layoutManager = LinearLayoutManager(this)
        binding.rvPages.adapter = ReadingPageAdapter(pages)

        // Custom scrollbar behavior
        binding.rvPages.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                val offset = recyclerView.computeVerticalScrollOffset()
                val extent = recyclerView.computeVerticalScrollExtent()
                val range = recyclerView.computeVerticalScrollRange()

                if (range > extent) {
                    // Total height of the scrollable area for the thumb
                    val totalHeight = recyclerView.height - binding.readTopbar.height - binding.readBottombar.height
                    val thumbHeight = binding.scrollThumb.height
                    val scrollRange = totalHeight - thumbHeight
                    
                    val percentage = offset.toFloat() / (range - extent)
                    binding.scrollThumb.translationY = (binding.readTopbar.height + (percentage * scrollRange)).toFloat()
                }
            }
        })

        // Tombol back
        binding.btnReadBack.setOnClickListener { finish() }

        // Prev/Next chapter
        setupChapterNav(comicId, chapterId)
    }

    private fun setupChapterNav(comicId: Int, currentChapterId: Int) {
        val chapters = DummyData.getChaptersByComicId(comicId)
        val currentIndex = chapters.indexOfFirst { it.id == currentChapterId }

        binding.btnPrevChapter.setOnClickListener {
            if (currentIndex < chapters.size - 1) {
                val prev = chapters[currentIndex + 1]
                restartWithChapter(comicId, prev.id, prev.title)
            }
        }

        binding.btnNextChapter.setOnClickListener {
            if (currentIndex > 0) {
                val next = chapters[currentIndex - 1]
                restartWithChapter(comicId, next.id, next.title)
            }
        }
    }

    private fun restartWithChapter(comicId: Int, chapterId: Int, chapterTitle: String) {
        val comic = DummyData.getComicById(comicId) ?: return
        val intent = intent.apply {
            putExtra(EXTRA_COMIC_TITLE, comic.title)
            putExtra(EXTRA_CHAPTER_TITLE, chapterTitle)
            putExtra(EXTRA_CHAPTER_ID, chapterId)
            putExtra(EXTRA_COMIC_ID, comicId)
        }
        finish()
        startActivity(intent)
    }
}
