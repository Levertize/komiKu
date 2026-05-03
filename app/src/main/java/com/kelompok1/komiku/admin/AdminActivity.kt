package com.kelompok1.komiku.admin

import android.content.Context
import android.content.Intent
import android.os.Bundle
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
    private lateinit var adapter: ComicListAdapter

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
        
        // FAB click opens Chapter management for now (simplified for this task)
        binding.fabAddComic.setOnClickListener {
            // Simplified: we'll just allow adding chapters to existing comics for now
        }
    }

    private fun setupRecyclerView() {
        adapter = ComicListAdapter(mutableListOf()) { comic ->
            val intent = Intent(this, AdminChapterActivity::class.java).apply {
                putExtra("comic_id", comic.id)
                putExtra("comic_title", comic.title)
            }
            startActivity(intent)
        }
        binding.rvAdminComics.layoutManager = LinearLayoutManager(this)
        binding.rvAdminComics.adapter = adapter
    }

    private fun observeComics() {
        lifecycleScope.launch {
            comicRepository.getAllComics().collectLatest { comics ->
                // Note: ComicListAdapter needs an update to accept dynamic list updates
                // For now, recreating adapter or adding a method to it
                (binding.rvAdminComics.adapter as? ComicListAdapter)?.let {
                    // This is a bit of a hack since ComicListAdapter takes List, not MutableList
                    setupRecyclerViewWithData(comics)
                }
            }
        }
    }

    private fun setupRecyclerViewWithData(comics: List<Comic>) {
        adapter = ComicListAdapter(comics) { comic ->
            val intent = Intent(this, AdminChapterActivity::class.java).apply {
                putExtra("comic_id", comic.id)
                putExtra("comic_title", comic.title)
            }
            startActivity(intent)
        }
        binding.rvAdminComics.adapter = adapter
    }
}
