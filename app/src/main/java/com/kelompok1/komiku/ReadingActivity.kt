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
        binding.btnReadMenu.setOnClickListener {
            showReadingMenuBottomSheet(comicId, chapterId)
        }
        setupChapterNav(comicId, chapterId)

        // Hide custom scroll thumb as PDFView has its own or we might not need it
        binding.scrollThumb.visibility = View.GONE
    }

    private fun isHorizontalMode(): Boolean {
        val prefs = getSharedPreferences("komiku_prefs", MODE_PRIVATE)
        return prefs.getBoolean("read_horizontal_mode", false)
    }

    private fun setHorizontalMode(horizontal: Boolean) {
        val prefs = getSharedPreferences("komiku_prefs", MODE_PRIVATE)
        prefs.edit().putBoolean("read_horizontal_mode", horizontal).apply()
    }

    private fun loadChapterData(chapterId: Int) {
        lifecycleScope.launch {
            val chapter = chapterRepository.getChapterById(chapterId)
            if (chapter != null) {
                if (chapter.pdfPath != null) {
                    val file = File(chapter.pdfPath)
                    if (file.exists()) {
                        binding.pdfView.visibility = View.VISIBLE
                        binding.layoutEmptyPdf.visibility = View.GONE
                        val isHorizontal = isHorizontalMode()
                        binding.pdfView.fromFile(file)
                            .enableSwipe(true)
                            .swipeHorizontal(isHorizontal)
                            .enableDoubletap(true)
                            .defaultPage(0)
                            .load()
                    } else {
                        binding.pdfView.visibility = View.GONE
                        binding.layoutEmptyPdf.visibility = View.VISIBLE
                    }
                } else {
                    binding.pdfView.visibility = View.GONE
                    binding.layoutEmptyPdf.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun showReadingMenuBottomSheet(comicId: Int, currentChapterId: Int) {
        val dialog = com.google.android.material.bottomsheet.BottomSheetDialog(this)
        val sheetBinding = com.kelompok1.komiku.databinding.LayoutBottomSheetReadingMenuBinding.inflate(layoutInflater)
        dialog.setContentView(sheetBinding.root)

        // 1. Configure Mode selection
        val isHorizontal = isHorizontalMode()
        updateModeSelectionUi(sheetBinding, isHorizontal)

        sheetBinding.cardModeVertical.setOnClickListener {
            if (isHorizontalMode()) {
                setHorizontalMode(false)
                updateModeSelectionUi(sheetBinding, false)
                loadChapterData(currentChapterId) // Reload PDF with vertical scroll
            }
        }

        sheetBinding.cardModeHorizontal.setOnClickListener {
            if (!isHorizontalMode()) {
                setHorizontalMode(true)
                updateModeSelectionUi(sheetBinding, true)
                loadChapterData(currentChapterId) // Reload PDF with horizontal scroll
            }
        }

        // 2. Load and bind chapter list
        sheetBinding.rvSheetChapters.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        lifecycleScope.launch {
            chapterRepository.getChaptersByComicId(comicId).collectLatest { chapters ->
                sheetBinding.rvSheetChapters.adapter = com.kelompok1.komiku.adapter.ChapterAdapter(
                    chapters = chapters,
                    isAdminMode = false,
                    onDeleteClick = null,
                    onEditClick = null,
                    onReadClick = { chapter ->
                        dialog.dismiss()
                        if (chapter.id != currentChapterId) {
                            restartWithChapter(comicId, chapter.id, chapter.title)
                        }
                    }
                )
            }
        }

        dialog.show()
    }

    private fun updateModeSelectionUi(sheetBinding: com.kelompok1.komiku.databinding.LayoutBottomSheetReadingMenuBinding, isHorizontal: Boolean) {
        val activeColor = resources.getColor(R.color.accent, theme)
        val inactiveColor = resources.getColor(R.color.dk_muted, theme)
        val activeStrokeColor = resources.getColor(R.color.accent, theme)
        val inactiveStrokeColor = resources.getColor(R.color.dk_border, theme)
        
        if (isHorizontal) {
            // Horizontal active
            sheetBinding.cardModeHorizontal.strokeColor = activeStrokeColor
            sheetBinding.cardModeHorizontal.strokeWidth = (1.5 * resources.displayMetrics.density).toInt()
            sheetBinding.ivModeHorizontal.setColorFilter(activeColor)
            sheetBinding.tvModeHorizontal.setTextColor(activeColor)
            sheetBinding.tvModeHorizontal.setTypeface(null, android.graphics.Typeface.BOLD)

            // Vertical inactive
            sheetBinding.cardModeVertical.strokeColor = inactiveStrokeColor
            sheetBinding.cardModeVertical.strokeWidth = (1.0 * resources.displayMetrics.density).toInt()
            sheetBinding.ivModeVertical.setColorFilter(inactiveColor)
            sheetBinding.tvModeVertical.setTextColor(inactiveColor)
            sheetBinding.tvModeVertical.setTypeface(null, android.graphics.Typeface.NORMAL)
        } else {
            // Vertical active
            sheetBinding.cardModeVertical.strokeColor = activeStrokeColor
            sheetBinding.cardModeVertical.strokeWidth = (1.5 * resources.displayMetrics.density).toInt()
            sheetBinding.ivModeVertical.setColorFilter(activeColor)
            sheetBinding.tvModeVertical.setTextColor(activeColor)
            sheetBinding.tvModeVertical.setTypeface(null, android.graphics.Typeface.BOLD)

            // Horizontal inactive
            sheetBinding.cardModeHorizontal.strokeColor = inactiveStrokeColor
            sheetBinding.cardModeHorizontal.strokeWidth = (1.0 * resources.displayMetrics.density).toInt()
            sheetBinding.ivModeHorizontal.setColorFilter(inactiveColor)
            sheetBinding.tvModeHorizontal.setTextColor(inactiveColor)
            sheetBinding.tvModeHorizontal.setTypeface(null, android.graphics.Typeface.NORMAL)
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
