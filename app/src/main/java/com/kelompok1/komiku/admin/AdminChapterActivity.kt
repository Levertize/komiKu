package com.kelompok1.komiku.admin

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.kelompok1.komiku.R
import com.kelompok1.komiku.adapter.ChapterAdapter
import com.kelompok1.komiku.database.KomiKuDatabase
import com.kelompok1.komiku.databinding.ActivityAdminChapterBinding
import com.kelompok1.komiku.model.Chapter
import com.kelompok1.komiku.repository.ChapterRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class AdminChapterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminChapterBinding
    private lateinit var chapterRepository: ChapterRepository
    private var comicId: Int = 0
    private var selectedPdfUri: Uri? = null
    private var editingChapter: Chapter? = null

    // Launcher for selecting a single PDF file
    private val selectPdfLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedPdfUri = uri
            binding.tvSelectedPdfPath.text = "File terpilih: ${getFileName(uri)}"
        }
    }

    private fun getFileName(uri: Uri): String {
        return uri.path?.substringAfterLast('/') ?: "file_pdf"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminChapterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        comicId = intent.getIntExtra("comic_id", 0)
        val comicTitle = intent.getStringExtra("comic_title") ?: "Unknown"

        binding.tvAdminComicTitle.text = comicTitle

        val database = KomiKuDatabase.getDatabase(this)
        chapterRepository = ChapterRepository(database.chapterDao())

        setupRecyclerView()
        observeChapters()

        binding.btnAdminChapterBack.setOnClickListener { finish() }

        binding.btnSelectPdf.setOnClickListener {
            selectPdfLauncher.launch("application/pdf")
        }

        binding.btnSaveChapter.setOnClickListener {
            saveChapter()
        }
    }

    private fun setupRecyclerView() {
        binding.rvAdminChapters.layoutManager = LinearLayoutManager(this)
    }

    private fun observeChapters() {
        lifecycleScope.launch {
            chapterRepository.getChaptersByComicId(comicId).collectLatest { chapters ->
                binding.rvAdminChapters.adapter = ChapterAdapter(
                    chapters = chapters,
                    isAdminMode = true,
                    onDeleteClick = { chapter ->
                        deleteChapter(chapter)
                    },
                    onEditClick = { chapter ->
                        startEditing(chapter)
                    },
                    onReadClick = { chapter ->
                        val intent = Intent(this@AdminChapterActivity, com.kelompok1.komiku.ReadingActivity::class.java).apply {
                            putExtra(com.kelompok1.komiku.ReadingActivity.EXTRA_COMIC_TITLE, binding.tvAdminComicTitle.text.toString())
                            putExtra(com.kelompok1.komiku.ReadingActivity.EXTRA_CHAPTER_TITLE, "Chapter ${chapter.number}")
                            putExtra(com.kelompok1.komiku.ReadingActivity.EXTRA_CHAPTER_ID, chapter.id)
                            putExtra(com.kelompok1.komiku.ReadingActivity.EXTRA_COMIC_ID, chapter.comicId)
                        }
                        startActivity(intent)
                    }
                )
            }
        }
    }

    private fun startEditing(chapter: Chapter) {
        editingChapter = chapter
        binding.etChapterNumber.setText(chapter.number.toString())
        binding.etChapterTitle.setText(chapter.title)
        binding.tvSelectedPdfPath.text = if (chapter.pdfPath != null) {
            "PDF Saat Ini: ${chapter.pdfPath.substringAfterLast('/')}"
        } else {
            "Belum ada PDF (Silakan upload)"
        }
        binding.btnSaveChapter.text = "Update Chapter"
        // Scroll to form
        binding.formChapter.parent.requestChildFocus(binding.formChapter, binding.formChapter)
    }

    private fun deleteChapter(chapter: Chapter) {
        lifecycleScope.launch {
            if (chapter.pdfPath != null) {
                val file = File(chapter.pdfPath)
                if (file.exists()) file.delete()
            }
            
            chapterRepository.deleteChapter(chapter)
            Toast.makeText(this@AdminChapterActivity, "Chapter dihapus", Toast.LENGTH_SHORT).show()
            if (editingChapter?.id == chapter.id) {
                clearForm()
            }
        }
    }

    private fun saveChapter() {
        val numberString = binding.etChapterNumber.text.toString()
        val title = binding.etChapterTitle.text.toString()

        if (numberString.isEmpty() || title.isEmpty()) {
            Toast.makeText(this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show()
            return
        }

        val number = numberString.toIntOrNull() ?: return

        lifecycleScope.launch {
            var pdfPath = editingChapter?.pdfPath
            
            // If new PDF selected, copy it
            if (selectedPdfUri != null) {
                val newPdfPath = copyPdfToInternalStorage(selectedPdfUri!!, comicId, number)
                if (newPdfPath != null) {
                    // Delete old file if updating
                    if (pdfPath != null && pdfPath != newPdfPath) {
                        val oldFile = File(pdfPath)
                        if (oldFile.exists()) oldFile.delete()
                    }
                    pdfPath = newPdfPath
                } else {
                    Toast.makeText(this@AdminChapterActivity, getString(R.string.copy_failed), Toast.LENGTH_SHORT).show()
                    return@launch
                }
            }

            val chapterToSave = Chapter(
                id = editingChapter?.id ?: 0,
                comicId = comicId,
                number = number,
                title = title,
                pdfPath = pdfPath,
                uploadDate = if (editingChapter != null) editingChapter!!.uploadDate else "baru saja"
            )
            
            val database = KomiKuDatabase.getDatabase(this@AdminChapterActivity)
            database.chapterDao().insertChapters(listOf(chapterToSave))
            
            // Update comic's last_update timestamp
            val comic = database.comicDao().getComicById(comicId)
            if (comic != null) {
                val updatedComic = comic.copy(lastUpdate = System.currentTimeMillis().toString())
                database.comicDao().updateComic(updatedComic)
            }
            
            val msg = if (editingChapter != null) "Chapter diperbarui" else getString(R.string.chapter_saved)
            Toast.makeText(this@AdminChapterActivity, msg, Toast.LENGTH_SHORT).show()
            clearForm()
        }
    }

    private fun copyPdfToInternalStorage(uri: Uri, comicId: Int, chapterNumber: Int): String? {
        return try {
            val folder = File(filesDir, "chapters/$comicId")
            if (!folder.exists()) folder.mkdirs()
            
            val file = File(folder, "chapter_$chapterNumber.pdf")
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val outputStream = FileOutputStream(file)
            
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun clearForm() {
        binding.etChapterNumber.setText("")
        binding.etChapterTitle.setText("")
        binding.tvSelectedPdfPath.text = getString(R.string.no_pdf_selected)
        binding.btnSaveChapter.text = "Simpan Chapter"
        selectedPdfUri = null
        editingChapter = null
    }
}
