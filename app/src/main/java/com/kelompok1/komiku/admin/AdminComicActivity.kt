package com.kelompok1.komiku.admin

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.kelompok1.komiku.database.KomiKuDatabase
import com.kelompok1.komiku.databinding.ActivityAdminComicBinding
import com.kelompok1.komiku.model.Comic
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class AdminComicActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminComicBinding
    private var comicId: Int = -1
    private var selectedCoverUri: Uri? = null
    private var currentCoverPath: String? = null

    private val selectCoverLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedCoverUri = it
            Glide.with(this).load(it).into(binding.ivCoverPreview)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminComicBinding.inflate(layoutInflater)
        setContentView(binding.root)

        comicId = intent.getIntExtra("comic_id", -1)

        if (comicId != -1) {
            binding.tvTitle.text = "Edit Komik"
            binding.btnDelete.visibility = View.VISIBLE
            loadComicData()
        } else {
            binding.tvTitle.text = "Tambah Komik"
            binding.btnDelete.visibility = View.GONE
        }

        binding.btnBack.setOnClickListener { finish() }
        binding.btnSelectCover.setOnClickListener { selectCoverLauncher.launch("image/*") }
        binding.btnSave.setOnClickListener { saveComic() }
        binding.btnDelete.setOnClickListener { deleteComic() }
    }

    private fun loadComicData() {
        lifecycleScope.launch {
            val database = KomiKuDatabase.getDatabase(this@AdminComicActivity)
            val comic = database.comicDao().getComicById(comicId)
            comic?.let {
                binding.etTitle.setText(it.title)
                binding.etAuthor.setText(it.author)
                binding.etGenre.setText(it.genre.joinToString(", "))
                binding.etDescription.setText(it.description)
                binding.etRating.setText(it.rating.toString())
                binding.etBadge.setText(it.badge)
                binding.etColorStart.setText(String.format("#%06X", (0xFFFFFF and it.coverColorStart)))
                binding.etColorEnd.setText(String.format("#%06X", (0xFFFFFF and it.coverColorEnd)))
                
                currentCoverPath = it.coverPath
                if (!it.coverPath.isNullOrEmpty()) {
                    val file = File(it.coverPath)
                    if (file.exists()) {
                        Glide.with(this@AdminComicActivity).load(file).into(binding.ivCoverPreview)
                    }
                }

                when (it.format) {
                    "Manga" -> binding.cgFormat.check(binding.chipManga.id)
                    "Manhwa" -> binding.cgFormat.check(binding.chipManhwa.id)
                    "Manhua" -> binding.cgFormat.check(binding.chipManhua.id)
                    "Webtoon" -> binding.cgFormat.check(binding.chipWebtoon.id)
                }
            }
        }
    }

    private fun saveComic() {
        val title = binding.etTitle.text.toString().trim()
        val author = binding.etAuthor.text.toString().trim()
        val genreStr = binding.etGenre.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val rating = binding.etRating.text.toString().toFloatOrNull() ?: 0f
        val badge = binding.etBadge.text.toString().trim()
        val colorStartStr = binding.etColorStart.text.toString().trim()
        val colorEndStr = binding.etColorEnd.text.toString().trim()

        val selectedFormatId = binding.cgFormat.checkedChipId
        val format = when (selectedFormatId) {
            binding.chipManga.id -> "Manga"
            binding.chipManhwa.id -> "Manhwa"
            binding.chipManhua.id -> "Manhua"
            binding.chipWebtoon.id -> "Webtoon"
            else -> ""
        }

        if (title.isEmpty() || author.isEmpty() || format.isEmpty()) {
            Toast.makeText(this, "Judul, Author, dan Format wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        val genres = genreStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val colorStart = try { Color.parseColor(colorStartStr) } catch (e: Exception) { Color.parseColor("#1A0E35") }
        val colorEnd = try { Color.parseColor(colorEndStr) } catch (e: Exception) { Color.parseColor("#0D1840") }

        lifecycleScope.launch {
            val database = KomiKuDatabase.getDatabase(this@AdminComicActivity)
            val comicDao = database.comicDao()

            var coverPath = currentCoverPath
            selectedCoverUri?.let { uri ->
                coverPath = copyCoverToInternalStorage(uri, title.replace(" ", "_"))
            }

            val comic = if (comicId != -1) {
                val existing = comicDao.getComicById(comicId)
                existing?.copy(
                    title = title,
                    author = author,
                    format = format,
                    genre = genres,
                    description = description,
                    rating = rating,
                    badge = badge,
                    coverColorStart = colorStart,
                    coverColorEnd = colorEnd,
                    coverPath = coverPath
                ) ?: return@launch
            } else {
                Comic(
                    title = title,
                    author = author,
                    format = format,
                    genre = genres,
                    description = description,
                    rating = rating,
                    badge = badge,
                    coverColorStart = colorStart,
                    coverColorEnd = colorEnd,
                    lastUpdate = System.currentTimeMillis().toString(),
                    coverPath = coverPath
                )
            }

            if (comicId != -1) {
                comicDao.updateComic(comic)
                Toast.makeText(this@AdminComicActivity, "Komik diperbarui", Toast.LENGTH_SHORT).show()
            } else {
                comicDao.insertComics(listOf(comic))
                Toast.makeText(this@AdminComicActivity, "Komik ditambahkan", Toast.LENGTH_SHORT).show()
            }
            finish()
        }
    }

    private fun copyCoverToInternalStorage(uri: Uri, fileName: String): String? {
        return try {
            val folder = File(filesDir, "covers")
            if (!folder.exists()) folder.mkdirs()
            
            val file = File(folder, "cover_${fileName}_${System.currentTimeMillis()}.jpg")
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

    private fun deleteComic() {
        if (comicId == -1) return
        
        lifecycleScope.launch {
            val database = KomiKuDatabase.getDatabase(this@AdminComicActivity)
            val comic = database.comicDao().getComicById(comicId)
            if (comic != null) {
                // Delete associated chapters first
                database.chapterDao().deleteChaptersByComicId(comicId)
                // Delete cover file if exists
                if (!comic.coverPath.isNullOrEmpty()) {
                    File(comic.coverPath).delete()
                }
                // Delete comic
                database.comicDao().deleteComic(comic)
                Toast.makeText(this@AdminComicActivity, "Komik dan semua chapter dihapus", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
