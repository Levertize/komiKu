package com.kelompok1.komiku

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.kelompok1.komiku.adapter.ComicListAdapter
import com.kelompok1.komiku.database.KomiKuDatabase
import com.kelompok1.komiku.databinding.FragmentJelajahiBinding
import com.kelompok1.komiku.model.Comic
import com.kelompok1.komiku.repository.ComicRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.widget.Toast

class JelajahiFragment : Fragment() {

    private var _binding: FragmentJelajahiBinding? = null
    private val binding get() = _binding!!

    private lateinit var comicRepository: ComicRepository
    private var allComics = mutableListOf<Comic>()
    private var filteredComics = mutableListOf<Comic>()
    private lateinit var listAdapter: ComicListAdapter

    // State filter
    private var selectedFormat = ""
    private var selectedGenre = ""
    private var selectedSort = "Terpopuler"
    private var isDesc = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJelajahiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val database = KomiKuDatabase.getDatabase(requireContext())
        comicRepository = ComicRepository(database.comicDao())

        setupRecyclerView()
        setupSearch()
        setupFilterButton()
        setupSortToggle()
        observeComics()
    }

    private fun observeComics() {
        viewLifecycleOwner.lifecycleScope.launch {
            comicRepository.getAllComics().collectLatest { comics ->
                allComics.clear()
                allComics.addAll(comics)
                applyFilter()
                updateActiveChips()
            }
        }
    }

    private fun setupRecyclerView() {
        listAdapter = ComicListAdapter(filteredComics, onBookmarkClick = { comic ->
            toggleBookmark(comic)
        }) { comic ->
            val intent = Intent(requireContext(), DetailActivity::class.java).apply {
                putExtra(DetailActivity.EXTRA_COMIC_ID, comic.id)
            }
            startActivity(intent)
        }
        binding.rvExploreResults.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = listAdapter
        }
    }

    private fun toggleBookmark(comic: Comic) {
        viewLifecycleOwner.lifecycleScope.launch {
            val entry = comicRepository.getLibraryEntry(comic.id)
            if (entry != null) {
                comicRepository.removeFromLibrary(entry)
                android.widget.Toast.makeText(requireContext(), "${comic.title} dihapus dari Library", android.widget.Toast.LENGTH_SHORT).show()
            } else {
                val chapters = KomiKuDatabase.getDatabase(requireContext()).chapterDao().getChaptersByComicId(comic.id).first()
                comicRepository.addToLibrary(com.kelompok1.komiku.model.Library(
                    comicId = comic.id,
                    totalChapter = chapters.size
                ))
                android.widget.Toast.makeText(requireContext(), "${comic.title} disimpan ke Library", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                applyFilter()
                binding.btnClearSearch.visibility =
                    if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.btnClearSearch.setOnClickListener {
            binding.etSearch.setText("")
        }
    }

    private fun setupFilterButton() {
        var filterVisible = false

        binding.btnFilter.setOnClickListener {
            filterVisible = !filterVisible
            if (filterVisible) {
                binding.panelFilter.visibility = View.VISIBLE
                binding.layoutResults.visibility = View.GONE
            } else {
                binding.panelFilter.visibility = View.GONE
                binding.layoutResults.visibility = View.VISIBLE
            }
        }

        binding.btnCloseFilter.setOnClickListener {
            binding.btnFilter.performClick()
        }

        binding.btnApplyFilter.setOnClickListener {
            val checkedFormat = binding.chipGroupFormat.checkedChipId
            selectedFormat = when (checkedFormat) {
                View.NO_ID -> ""
                else -> {
                    val chip = binding.chipGroupFormat.findViewById<com.google.android.material.chip.Chip>(checkedFormat)
                    chip?.text?.toString() ?: ""
                }
            }

            val checkedGenre = binding.chipGroupGenre.checkedChipIds
            selectedGenre = if (checkedGenre.isEmpty()) "" else {
                val chip = binding.chipGroupGenre.findViewById<com.google.android.material.chip.Chip>(checkedGenre.first())
                chip?.text?.toString() ?: ""
            }

            val checkedSort = binding.chipGroupSort.checkedChipId
            if (checkedSort != View.NO_ID) {
                val chip = binding.chipGroupSort.findViewById<com.google.android.material.chip.Chip>(checkedSort)
                selectedSort = chip?.text?.toString() ?: "Terpopuler"
            }

            applyFilter()
            updateActiveChips()
            binding.panelFilter.visibility = View.GONE
            binding.layoutResults.visibility = View.VISIBLE
            filterVisible = false
        }

        binding.btnResetFilter.setOnClickListener {
            selectedFormat = ""
            selectedGenre = ""
            selectedSort = getString(R.string.sort_popular)
            binding.chipGroupFormat.clearCheck()
            binding.chipGroupGenre.clearCheck()
            // Reset sort chip to default
            for (i in 0 until binding.chipGroupSort.childCount) {
                val chip = binding.chipGroupSort.getChildAt(i) as? com.google.android.material.chip.Chip
                if (chip?.text == getString(R.string.sort_popular)) {
                    chip.isChecked = true
                    break
                }
            }
            applyFilter()
            updateActiveChips()
        }
    }

    private fun updateActiveChips() {
        binding.chipGroupActive.removeAllViews()

        if (selectedFormat.isNotEmpty()) {
            addActiveChip(selectedFormat)
        }
        if (selectedGenre.isNotEmpty()) {
            addActiveChip(selectedGenre)
        }
        if (selectedSort != getString(R.string.sort_popular)) {
            addActiveChip(selectedSort)
        }
    }

    private fun addActiveChip(text: String) {
        val chip = com.google.android.material.chip.Chip(requireContext())
        chip.text = text
        chip.isCloseIconVisible = true
        chip.setChipBackgroundColorResource(R.color.dk_card2)
        chip.setTextColor(android.content.res.ColorStateList.valueOf(0xFFF0EEF8.toInt()))
        chip.setCloseIconTintResource(R.color.accent)
        chip.chipStrokeWidth = 0f
        chip.textSize = 10f
        
        chip.setOnCloseIconClickListener {
            if (text == selectedFormat) selectedFormat = ""
            else if (text == selectedGenre) selectedGenre = ""
            else if (text == selectedSort) selectedSort = getString(R.string.sort_popular)
            
            applyFilter()
            updateActiveChips()
            // Sync with filter panel
            syncFilterPanel()
        }
        binding.chipGroupActive.addView(chip)
    }

    private fun syncFilterPanel() {
        // Sync format
        if (selectedFormat.isEmpty()) binding.chipGroupFormat.clearCheck()
        // Sync genre
        if (selectedGenre.isEmpty()) binding.chipGroupGenre.clearCheck()
        // Sync sort
        if (selectedSort == getString(R.string.sort_popular)) {
            for (i in 0 until binding.chipGroupSort.childCount) {
                val chip = binding.chipGroupSort.getChildAt(i) as? com.google.android.material.chip.Chip
                if (chip?.text == getString(R.string.sort_popular)) {
                    chip.isChecked = true
                    break
                }
            }
        }
    }

    private fun setupSortToggle() {
        binding.btnSortDesc.setOnClickListener {
            isDesc = true
            binding.btnSortDesc.setBackgroundResource(R.drawable.bg_sort_active)
            binding.btnSortDesc.setTextColor(0xFF7C5CFC.toInt())
            binding.btnSortAsc.background = null
            binding.btnSortAsc.setTextColor(0x61F0EEF8.toInt())
            applyFilter()
        }

        binding.btnSortAsc.setOnClickListener {
            isDesc = false
            binding.btnSortAsc.setBackgroundResource(R.drawable.bg_sort_active)
            binding.btnSortAsc.setTextColor(0xFF7C5CFC.toInt())
            binding.btnSortDesc.background = null
            binding.btnSortDesc.setTextColor(0x61F0EEF8.toInt())
            applyFilter()
        }
    }

    private fun applyFilter() {
        val query = binding.etSearch.text?.toString()?.trim()?.lowercase() ?: ""

        filteredComics.clear()
        val filtered = allComics.filter { comic ->
            // Match Query (Search box)
            val matchQuery = query.isEmpty() ||
                    comic.title.lowercase().contains(query) ||
                    comic.author.lowercase().contains(query) ||
                    comic.genre.any { it.lowercase().contains(query) }

            // Match Format (Exact match)
            val matchFormat = selectedFormat.isEmpty() ||
                    comic.format.trim().equals(selectedFormat.trim(), ignoreCase = true)

            // Match Genre (Exact match in list)
            val matchGenre = selectedGenre.isEmpty() ||
                    comic.genre.any { it.trim().equals(selectedGenre.trim(), ignoreCase = true) }

            matchQuery && matchFormat && matchGenre
        }.let { list ->
            // Handle Sorting
            val sorted = when (selectedSort) {
                getString(R.string.sort_rating) -> list.sortedByDescending { it.rating }
                getString(R.string.sort_az) -> list.sortedBy { it.title.lowercase() }
                getString(R.string.sort_za) -> list.sortedByDescending { it.title.lowercase() }
                getString(R.string.sort_latest) -> list.sortedByDescending { it.lastUpdate.toLongOrNull() ?: 0L }
                else -> list.sortedByDescending { 
                    val raw = it.views.uppercase()
                    var multiplier = 1f
                    if (raw.endsWith("M")) multiplier = 1_000_000f
                    else if (raw.endsWith("K")) multiplier = 1_000f
                    
                    val value = raw.replace("M", "").replace("K", "").replace(",", ".").trim().toFloatOrNull() ?: 0f
                    value * multiplier
                }
            }
            if (isDesc) sorted else sorted.reversed()
        }
        
        filteredComics.addAll(filtered)
        listAdapter.notifyDataSetChanged()
        updateResultCount()
        
        // Toggle empty state visibility
        if (filteredComics.isEmpty()) {
            binding.layoutEmpty.root.visibility = View.VISIBLE
            binding.rvExploreResults.visibility = View.GONE
        } else {
            binding.layoutEmpty.root.visibility = View.GONE
            binding.rvExploreResults.visibility = View.VISIBLE
        }
    }

    private fun updateResultCount() {
        binding.tvResultCount.text = "${filteredComics.size} hasil ditemukan"
        binding.tvActiveSort.text = selectedSort
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
