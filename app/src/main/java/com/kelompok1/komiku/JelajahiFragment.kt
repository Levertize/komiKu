package com.kelompok1.komiku


import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.kelompok1.komiku.adapter.ComicListAdapter

import com.kelompok1.komiku.data.DummyData
import com.kelompok1.komiku.databinding.FragmentJelajahiBinding
import com.kelompok1.komiku.model.Comic

class JelajahiFragment : Fragment() {

    private var _binding: FragmentJelajahiBinding? = null
    private val binding get() = _binding!!

    private var allComics = DummyData.exploreComics.toMutableList()
    private var filteredComics = allComics.toMutableList()
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

        setupRecyclerView()
        setupSearch()
        setupFilterButton()
        setupSortToggle()
        updateResultCount()
    }

    private fun setupRecyclerView() {
        listAdapter = ComicListAdapter(filteredComics) { comic ->
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

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                applyFilter()
                // Tampilkan tombol clear kalau ada teks
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

        binding.btnApplyFilter.setOnClickListener {
            // Ambil format yang dipilih
            val checkedFormat = binding.chipGroupFormat.checkedChipId
            selectedFormat = when (checkedFormat) {
                View.NO_ID -> ""
                else -> {
                    val chip = binding.chipGroupFormat.findViewById<com.google.android.material.chip.Chip>(checkedFormat)
                    chip?.text?.toString() ?: ""
                }
            }

            // Ambil genre yang dipilih
            val checkedGenre = binding.chipGroupGenre.checkedChipIds
            selectedGenre = if (checkedGenre.isEmpty()) "" else {
                val chip = binding.chipGroupGenre.findViewById<com.google.android.material.chip.Chip>(checkedGenre.first())
                chip?.text?.toString() ?: ""
            }

            // Ambil sort
            val checkedSort = binding.chipGroupSort.checkedChipId
            if (checkedSort != View.NO_ID) {
                val chip = binding.chipGroupSort.findViewById<com.google.android.material.chip.Chip>(checkedSort)
                selectedSort = chip?.text?.toString() ?: "Terpopuler"
            }

            applyFilter()
            binding.panelFilter.visibility = View.GONE
            binding.layoutResults.visibility = View.VISIBLE
            filterVisible = false
        }

        binding.btnResetFilter.setOnClickListener {
            selectedFormat = ""
            selectedGenre = ""
            selectedSort = "Terpopuler"
            binding.chipGroupFormat.clearCheck()
            binding.chipGroupGenre.clearCheck()
            applyFilter()
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
        val query = binding.etSearch.text?.toString()?.lowercase() ?: ""

        filteredComics = allComics.filter { comic ->
            val matchQuery = query.isEmpty() ||
                    comic.title.lowercase().contains(query) ||
                    comic.author.lowercase().contains(query) ||
                    comic.genre.any { it.lowercase().contains(query) }

            val matchFormat = selectedFormat.isEmpty() ||
                    comic.format.equals(selectedFormat, ignoreCase = true)

            val matchGenre = selectedGenre.isEmpty() ||
                    comic.genre.any { it.equals(selectedGenre, ignoreCase = true) }

            matchQuery && matchFormat && matchGenre
        }.let { list ->
            // Sorting
            val sorted = when (selectedSort) {
                "Rating tertinggi" -> list.sortedByDescending { it.rating }
                "A → Z" -> list.sortedBy { it.title }
                "Z → A" -> list.sortedByDescending { it.title }
                else -> list.sortedByDescending { it.views.replace("M", "").replace(".", "").toFloatOrNull() ?: 0f }
            }
            if (isDesc) sorted else sorted.reversed()
        }.toMutableList()

        listAdapter = ComicListAdapter(filteredComics) { comic ->
            val intent = Intent(requireContext(), DetailActivity::class.java).apply {
                putExtra(DetailActivity.EXTRA_COMIC_ID, comic.id)
            }
            startActivity(intent)
        }
        binding.rvExploreResults.adapter = listAdapter
        updateResultCount()
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
