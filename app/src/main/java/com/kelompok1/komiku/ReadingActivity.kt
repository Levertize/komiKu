package com.kelompok1.komiku

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.kelompok1.komiku.database.KomiKuDatabase
import com.kelompok1.komiku.databinding.ActivityReadingBinding
import com.kelompok1.komiku.model.Chapter
import com.kelompok1.komiku.model.Library
import com.kelompok1.komiku.repository.ChapterRepository
import com.kelompok1.komiku.repository.ComicRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File

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
        
        loadChapterData(chapterId)
        updateReadingHistory(comicId, chapterId)

        binding.btnReadBack.setOnClickListener { finish() }
        setupChapterNav(comicId, chapterId)

        // Hide custom scroll thumb as PDFView has its own or we might not need it
        binding.scrollThumb.visibility = View.GONE
    }

    private fun loadChapterData(chapterId: Int) {
        lifecycleScope.launch {
            val chapter = chapterRepository.getChapterById(chapterId)
            if (chapter != null) {
                if (chapter.pdfPath != null) {
                    val file = File(chapter.pdfPath)
                    if (file.exists()) {
                        binding.pdfView.fromFile(file)
                            .enableSwipe(true)
                            .swipeHorizontal(false)
                            .enableDoubletap(true)
                            .defaultPage(0)
                            .load()
                    } else {
                        Toast.makeText(this@ReadingActivity, "File PDF tidak ditemukan", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@ReadingActivity, "Chapter ini belum memiliki file PDF", Toast.LENGTH_SHORT).show()
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
