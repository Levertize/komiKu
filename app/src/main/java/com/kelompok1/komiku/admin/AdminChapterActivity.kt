package com.kelompok1.komiku.admin

import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.kelompok1.komiku.adapter.ChapterAdapter
import com.kelompok1.komiku.adapter.ChapterPreviewAdapter
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
    private var selectedImages = mutableListOf<Uri>()

    // Launcher for selecting multiple images
    private val selectImagesLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (uriListNotEmpty(uris)) {
            selectedImages.clear()
            selectedImages.addAll(uris)
            binding.tvSelectedPdfPath.text = "${uris.size} gambar terpilih"
            
            // Show preview
            binding.rvImagePreview.visibility = View.VISIBLE
            binding.rvImagePreview.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            binding.rvImagePreview.adapter = ChapterPreviewAdapter(selectedImages)
        }
    }

    private fun uriListNotEmpty(uris: List<Uri>?): Boolean {
        return uris != null && uris.isNotEmpty()
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

        binding.btnSelectPdf.apply {
            text = "Pilih Gambar Chapter"
            setOnClickListener {
                selectImagesLauncher.launch("image/*")
            }
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
                binding.rvAdminChapters.adapter = ChapterAdapter(chapters) { chapter ->
                    // Admin can preview the chapter content
                    val intent = Intent(this@AdminChapterActivity, com.kelompok1.komiku.ReadingActivity::class.java).apply {
                        putExtra(com.kelompok1.komiku.ReadingActivity.EXTRA_COMIC_TITLE, binding.tvAdminComicTitle.text.toString())
                        putExtra(com.kelompok1.komiku.ReadingActivity.EXTRA_CHAPTER_TITLE, "Chapter ${chapter.number}")
                        putExtra(com.kelompok1.komiku.ReadingActivity.EXTRA_CHAPTER_ID, chapter.id)
                        putExtra(com.kelompok1.komiku.ReadingActivity.EXTRA_COMIC_ID, chapter.comicId)
                    }
                    startActivity(intent)
                }
            }
        }
    }

    private fun saveChapter() {
        val number = binding.etChapterNumber.text.toString().toIntOrNull()
        val title = binding.etChapterTitle.text.toString()

        if (number == null || title.isEmpty() || selectedImages.isEmpty()) {
            Toast.makeText(this, "Mohon lengkapi semua data", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val imagePaths = copyImagesToInternalStorage(selectedImages, comicId, number)
            if (imagePaths.isNotEmpty()) {
                val newChapter = Chapter(
                    comicId = comicId,
                    number = number,
                    title = title,
                    imagePaths = imagePaths,
                    uploadDate = "baru saja"
                )
                
                val database = KomiKuDatabase.getDatabase(this@AdminChapterActivity)
                database.chapterDao().insertChapters(listOf(newChapter))
                
                // Update comic's last_update timestamp
                val comic = database.comicDao().getComicById(comicId)
                if (comic != null) {
                    val updatedComic = comic.copy(lastUpdate = System.currentTimeMillis().toString())
                    database.comicDao().updateComic(updatedComic)
                }
                
                Toast.makeText(this@AdminChapterActivity, "Chapter berhasil disimpan", Toast.LENGTH_SHORT).show()
                clearForm()
            } else {
                Toast.makeText(this@AdminChapterActivity, "Gagal menyalin gambar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun copyImagesToInternalStorage(uris: List<Uri>, comicId: Int, chapterNumber: Int): List<String> {
        val savedPaths = mutableListOf<String>()
        try {
            val folder = File(filesDir, "chapters/$comicId/$chapterNumber")
            if (!folder.exists()) folder.mkdirs()
            
            uris.forEachIndexed { index, uri ->
                val inputStream = contentResolver.openInputStream(uri) ?: return@forEachIndexed
                val file = File(folder, "page_${index + 1}.jpg")
                val outputStream = FileOutputStream(file)
                
                inputStream.copyTo(outputStream)
                inputStream.close()
                outputStream.close()
                
                savedPaths.add(file.absolutePath)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return savedPaths
    }

    private fun clearForm() {
        binding.etChapterNumber.setText("")
        binding.etChapterTitle.setText("")
        binding.tvSelectedPdfPath.text = "Belum ada gambar terpilih"
        binding.rvImagePreview.visibility = View.GONE
        selectedImages.clear()
    }
}
