package com.kelompok1.komiku

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.kelompok1.komiku.adapter.ReadingPageAdapter
import com.kelompok1.komiku.database.KomiKuDatabase
import com.kelompok1.komiku.databinding.ActivityReadingBinding
import com.kelompok1.komiku.model.Chapter
import com.kelompok1.komiku.model.Library
import com.kelompok1.komiku.repository.ChapterRepository
import com.kelompok1.komiku.repository.ComicRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ReadingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReadingBinding
    private lateinit var chapterRepository: ChapterRepository
    private lateinit var comicRepository: ComicRepository

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

        val database = KomiKuDatabase.getDatabase(this)
        chapterRepository = ChapterRepository(database.chapterDao())
        comicRepository = ComicRepository(database.comicDao())

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
            
            val params = binding.scrollThumb.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
            params.topMargin = statusBars.top + (16 * resources.displayMetrics.density).toInt()
            binding.scrollThumb.layoutParams = params
            
            insets
        }

        val comicTitle   = intent.getStringExtra(EXTRA_COMIC_TITLE) ?: ""
        val chapterTitle = intent.getStringExtra(EXTRA_CHAPTER_TITLE) ?: ""
        val chapterId    = intent.getIntExtra(EXTRA_CHAPTER_ID, 1)
        val comicId      = intent.getIntExtra(EXTRA_COMIC_ID, 1)

        binding.tvReadComicTitle.text = comicTitle
        binding.tvReadChapter.text = chapterTitle

        binding.rvPages.layoutManager = LinearLayoutManager(this)
        
        loadChapterData(chapterId)
        updateReadingHistory(comicId, chapterId)

        binding.rvPages.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                val offset = recyclerView.computeVerticalScrollOffset()
                val extent = recyclerView.computeVerticalScrollExtent()
                val range = recyclerView.computeVerticalScrollRange()

                if (range > extent) {
                    val totalHeight = recyclerView.height - binding.readTopbar.height - binding.readBottombar.height
                    val thumbHeight = binding.scrollThumb.height
                    val scrollRange = totalHeight - thumbHeight
                    val percentage = offset.toFloat() / (range - extent)
                    binding.scrollThumb.translationY = (binding.readTopbar.height + (percentage * scrollRange))
                }
            }
        })

        binding.btnReadBack.setOnClickListener { finish() }
        setupChapterNav(comicId, chapterId)
    }

    private fun loadChapterData(chapterId: Int) {
        lifecycleScope.launch {
            val chapter = chapterRepository.getChapterById(chapterId)
            if (chapter != null) {
                if (chapter.imagePaths.isEmpty()) {
                    // Fallback to unified dummy pages if no real images
                    val dummyPages = listOf(
                        "dummy_color_#1A1A2E",
                        "dummy_color_#7C5CFC",
                        "dummy_color_#C084FC",
                        "dummy_color_#13131F",
                        "dummy_color_#0D0D14"
                    )
                    binding.rvPages.adapter = ReadingPageAdapter(dummyPages)
                } else {
                    binding.rvPages.adapter = ReadingPageAdapter(chapter.imagePaths)
                }
            }
        }
    }

    private fun setupChapterNav(comicId: Int, currentChapterId: Int) {
        lifecycleScope.launch {
            chapterRepository.getChaptersByComicId(comicId).collectLatest { chapters ->
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
        }
    }

    private fun restartWithChapter(comicId: Int, chapterId: Int, chapterTitle: String) {
        lifecycleScope.launch {
            val comic = comicRepository.getComicById(comicId) ?: return@launch
            val intent = Intent(this@ReadingActivity, ReadingActivity::class.java).apply {
                putExtra(EXTRA_COMIC_TITLE, comic.title)
                putExtra(EXTRA_CHAPTER_TITLE, chapterTitle)
                putExtra(EXTRA_CHAPTER_ID, chapterId)
                putExtra(EXTRA_COMIC_ID, comicId)
            }
            finish()
            startActivity(intent)
        }
    }

    private fun updateReadingHistory(comicId: Int, chapterId: Int) {
        lifecycleScope.launch {
            val chapter = chapterRepository.getChapterById(chapterId) ?: return@launch
            val allChapters = chapterRepository.getChaptersByComicId(comicId).first()
            val totalChapters = allChapters.size

            val existingEntry = comicRepository.getLibraryEntry(comicId)
            if (existingEntry != null) {
                val updatedEntry = existingEntry.copy(
                    currentChapter = chapter.number,
                    totalChapter = totalChapters,
                    savedAt = System.currentTimeMillis()
                )
                comicRepository.addToLibrary(updatedEntry)
            } else {
                // If not in library, we still want it to show up in "Last Read" which is effectively the library view
                val newEntry = Library(
                    comicId = comicId,
                    currentChapter = chapter.number,
                    totalChapter = totalChapters,
                    savedAt = System.currentTimeMillis()
                )
                comicRepository.addToLibrary(newEntry)
            }
        }
    }
}
