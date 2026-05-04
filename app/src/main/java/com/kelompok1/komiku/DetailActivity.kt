package com.kelompok1.komiku

import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.kelompok1.komiku.adapter.ChapterAdapter
import com.kelompok1.komiku.database.KomiKuDatabase
import com.kelompok1.komiku.databinding.ActivityDetailBinding
import com.kelompok1.komiku.model.Chapter
import com.kelompok1.komiku.model.Comic
import com.kelompok1.komiku.model.Library
import com.kelompok1.komiku.repository.ChapterRepository
import com.kelompok1.komiku.repository.ComicRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private var isDescExpanded = false
    
    private lateinit var comicRepository: ComicRepository
    private lateinit var chapterRepository: ChapterRepository
    private var currentComic: Comic? = null
    private var isBookmarked = false

    companion object {
        const val EXTRA_COMIC_ID = "comic_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val database = KomiKuDatabase.getDatabase(this)
        comicRepository = ComicRepository(database.comicDao())
        chapterRepository = ChapterRepository(database.chapterDao())

        val comicId = intent.getIntExtra(EXTRA_COMIC_ID, 1)
        
        loadComicData(comicId)
        
        binding.btnDetailBack.setOnClickListener { finish() }
    }

    private fun loadComicData(comicId: Int) {
        lifecycleScope.launch {
            val comic = comicRepository.getComicById(comicId)
            if (comic != null) {
                currentComic = comic
                setupCover(comic)
                setupInfo(comic)
                setupGenreChips(comic)
                setupDescription(comic)
                checkBookmarkStatus(comicId)
                
                chapterRepository.getChaptersByComicId(comicId).collectLatest { chapters ->
                    setupChapterList(comic, chapters)
                    setupReadButton(comic, chapters)
                }
            }
        }
    }

    private fun checkBookmarkStatus(comicId: Int) {
        lifecycleScope.launch {
            val entry = comicRepository.getLibraryEntry(comicId)
            isBookmarked = entry != null
            updateBookmarkIcon()
            
            binding.btnBookmarkDetail.setOnClickListener {
                toggleBookmark(comicId)
            }
        }
    }

    private fun toggleBookmark(comicId: Int) {
        lifecycleScope.launch {
            if (isBookmarked) {
                val entry = comicRepository.getLibraryEntry(comicId)
                if (entry != null) {
                    comicRepository.removeFromLibrary(entry)
                    isBookmarked = false
                }
            } else {
                val chapters = chapterRepository.getChaptersByComicId(comicId).first()
                comicRepository.addToLibrary(Library(
                    comicId = comicId,
                    totalChapter = chapters.size
                ))
                isBookmarked = true
            }
            updateBookmarkIcon()
        }
    }

    private fun updateBookmarkIcon() {
        if (isBookmarked) {
            binding.btnBookmarkDetail.setIconResource(R.drawable.ic_bookmark)
            binding.btnBookmarkDetail.setIconTintResource(R.color.accent)
        } else {
            binding.btnBookmarkDetail.setIconResource(R.drawable.ic_bookmark)
            binding.btnBookmarkDetail.setIconTintResource(R.color.dk_muted)
        }
    }

    private fun setupCover(comic: Comic) {
        if (!comic.coverPath.isNullOrEmpty()) {
            val file = File(comic.coverPath)
            if (file.exists()) {
                Glide.with(this).load(file).into(binding.ivDetailCover)
                // Also set gradient for the background overlay
                val gradient = GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    intArrayOf(comic.coverColorStart, comic.coverColorEnd)
                )
                binding.viewCoverBg.background = gradient
                return
            }
        }
        
        val gradient = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(comic.coverColorStart, comic.coverColorEnd)
        )
        binding.viewCoverBg.background = gradient
        binding.ivDetailCover.setImageDrawable(null)
    }

    private fun setupInfo(comic: Comic) {
        binding.tvDetailTitle.text = comic.title
        binding.tvDetailAuthor.text = "oleh ${comic.author}"
        binding.tvDetailRating.text = comic.rating.toString()
        binding.tvDetailFormat.text = comic.format
        binding.tvStatViews.text = comic.views
        binding.tvStatStatus.text = comic.status
    }

    private fun setupGenreChips(comic: Comic) {
        binding.chipGroupGenres.removeAllViews()
        comic.genre.forEach { genre ->
            val chip = Chip(this).apply {
                text = genre
                textSize = 10f
                setTextColor(0xFF7C5CFC.toInt())
                isCheckable = false
                chipBackgroundColor = android.content.res.ColorStateList.valueOf(0x1A7C5CFC.toInt())
                setChipStrokeColorResource(R.color.accent_border)
                chipStrokeWidth = 1f
                chipCornerRadius = 20f
            }
            binding.chipGroupGenres.addView(chip)
        }
    }

    private fun setupDescription(comic: Comic) {
        binding.tvDetailDesc.text = comic.description
        binding.tvShowMore.setOnClickListener {
            if (isDescExpanded) {
                binding.tvDetailDesc.maxLines = 4
                binding.tvShowMore.text = "Selengkapnya ▼"
            } else {
                binding.tvDetailDesc.maxLines = Int.MAX_VALUE
                binding.tvShowMore.text = "Sembunyikan ▲"
            }
            isDescExpanded = !isDescExpanded
        }
    }

    private fun setupReadButton(comic: Comic, chapters: List<Chapter>) {
        binding.btnStartRead.setOnClickListener {
            if (chapters.isNotEmpty()) {
                openReading(comic, chapters.last()) // Start from first chapter (last in sorted list)
            }
        }
    }

    private fun setupChapterList(comic: Comic, chapters: List<Chapter>) {
        binding.tvStatChapters.text = chapters.size.toString()
        binding.tvChapterCount.text = "${chapters.size} chapter"

        binding.rvChapters.layoutManager = LinearLayoutManager(this)
        binding.rvChapters.adapter = ChapterAdapter(chapters) { chapter ->
            openReading(comic, chapter)
        }
        binding.rvChapters.isNestedScrollingEnabled = false
    }

    private fun openReading(comic: Comic, chapter: Chapter) {
        val intent = Intent(this, ReadingActivity::class.java).apply {
            putExtra(ReadingActivity.EXTRA_COMIC_TITLE, comic.title)
            putExtra(ReadingActivity.EXTRA_CHAPTER_TITLE, chapter.title)
            putExtra(ReadingActivity.EXTRA_CHAPTER_ID, chapter.id)
            putExtra(ReadingActivity.EXTRA_COMIC_ID, comic.id)
        }
        startActivity(intent)
    }
}
