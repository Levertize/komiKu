package com.kelompok1.komiku

import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.kelompok1.komiku.R
import com.kelompok1.komiku.adapter.ChapterAdapter
import com.kelompok1.komiku.data.DummyData
import com.kelompok1.komiku.databinding.ActivityDetailBinding
import com.kelompok1.komiku.model.Chapter
import com.kelompok1.komiku.model.Comic

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private var isDescExpanded = false

    companion object {
        const val EXTRA_COMIC_ID = "comic_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val comicId = intent.getIntExtra(EXTRA_COMIC_ID, 1)
        val comic = DummyData.getComicById(comicId) ?: return
        val chapters = DummyData.getChaptersByComicId(comicId)

        setupCover(comic)
        setupInfo(comic)
        setupGenreChips(comic)
        setupDescription(comic)
        setupButtons(comic, chapters)
        setupChapterList(chapters)
    }

    private fun setupCover(comic: Comic) {
        val gradient = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(comic.coverColorStart, comic.coverColorEnd)
        )
        binding.viewCoverBg.background = gradient
    }

    private fun setupInfo(comic: Comic) {
        binding.tvDetailTitle.text = comic.title
        binding.tvDetailAuthor.text = "oleh ${comic.author}"
        binding.tvDetailRating.text = comic.rating.toString()
        binding.tvDetailFormat.text = comic.format
        binding.tvStatViews.text = comic.views
        binding.tvStatChapters.text = DummyData.getChaptersByComicId(comic.id).size.toString()
        binding.tvStatStatus.text = comic.status
        binding.tvChapterCount.text = "${DummyData.getChaptersByComicId(comic.id).size} chapter"
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

    private fun setupButtons(comic: Comic, chapters: List<Chapter>) {
        // Tombol Mulai Baca → buka chapter pertama (terbaru)
        binding.btnStartRead.setOnClickListener {
            if (chapters.isNotEmpty()) {
                openReading(comic, chapters.first())
            }
        }

        // Tombol bookmark
        binding.btnBookmarkDetail.setOnClickListener {
            // TODO: simpan ke library
            binding.btnBookmarkDetail.setIconResource(R.drawable.ic_bookmark)
        }

        // Tombol back
        binding.btnDetailBack.setOnClickListener {
            finish()
        }
    }

    private fun setupChapterList(chapters: List<Chapter>) {
        val comic = DummyData.getComicById(
            intent.getIntExtra(EXTRA_COMIC_ID, 1)
        ) ?: return

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
