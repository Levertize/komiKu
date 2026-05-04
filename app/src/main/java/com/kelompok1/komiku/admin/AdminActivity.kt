package com.kelompok1.komiku.admin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.kelompok1.komiku.adapter.ComicListAdapter
import com.kelompok1.komiku.database.KomiKuDatabase
import com.kelompok1.komiku.databinding.ActivityAdminBinding
import com.kelompok1.komiku.model.Comic
import com.kelompok1.komiku.repository.ComicRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding
    private lateinit var comicRepository: ComicRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val database = KomiKuDatabase.getDatabase(this)
        comicRepository = ComicRepository(database.comicDao())

        setupRecyclerView()
        observeComics()

        binding.btnAdminBack.setOnClickListener { finish() }

        binding.btnAdminLogout.setOnClickListener {
            val prefs = getSharedPreferences("komiku_prefs", Context.MODE_PRIVATE)
            prefs.edit()
                .putBoolean("is_logged_in", false)
                .putBoolean("is_admin", false)
                .apply()

            val intent = Intent(this, com.kelompok1.komiku.LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
        
        binding.fabAddComic.setOnClickListener {
            val intent = Intent(this, AdminComicActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        binding.rvAdminComics.layoutManager = LinearLayoutManager(this)
    }

    private fun observeComics() {
        lifecycleScope.launch {
            comicRepository.getAllComics().collectLatest { comics ->
                val adapter = ComicListAdapter(comics, onBookmarkClick = null) { comic ->
                    // Admin behavior: show options (Manage Chapters or Edit Comic)
                    showAdminOptions(comic)
                }
                binding.rvAdminComics.adapter = adapter
            }
        }
    }

    private fun showAdminOptions(comic: Comic) {
        val options = arrayOf("Kelola Chapter", "Edit Detail Komik", "Hapus Komik")
        android.app.AlertDialog.Builder(this)
            .setTitle(comic.title)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> { // Manage Chapters
                        val intent = Intent(this, AdminChapterActivity::class.java).apply {
                            putExtra("comic_id", comic.id)
                            putExtra("comic_title", comic.title)
                        }
                        startActivity(intent)
                    }
                    1 -> { // Edit Comic
                        val intent = Intent(this, AdminComicActivity::class.java).apply {
                            putExtra("comic_id", comic.id)
                        }
                        startActivity(intent)
                    }
                    2 -> { // Delete Comic
                        confirmDelete(comic)
                    }
                }
            }
            .show()
    }

    private fun confirmDelete(comic: Comic) {
        android.app.AlertDialog.Builder(this)
            .setTitle("Hapus Komik")
            .setMessage("Apakah Anda yakin ingin menghapus '${comic.title}' beserta semua chapternya?")
            .setPositiveButton("Hapus") { _, _ ->
                lifecycleScope.launch {
                    val database = KomiKuDatabase.getDatabase(this@AdminActivity)
                    database.chapterDao().deleteChaptersByComicId(comic.id)
                    database.comicDao().deleteComic(comic)
                    Toast.makeText(this@AdminActivity, "Komik berhasil dihapus", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}
