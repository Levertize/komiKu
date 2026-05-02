package com.kelompok1.komiku

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.kelompok1.komiku.adapter.LibraryAdapter
import com.kelompok1.komiku.data.DummyData
import com.kelompok1.komiku.databinding.FragmentLibraryBinding

class LibraryFragment : Fragment() {

    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!

    private val allItems = DummyData.libraryComics.toMutableList()
    private var filteredItems = allItems.toMutableList()
    private lateinit var libraryAdapter: LibraryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLibraryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearch()
        setupFilter()
        updateCount()
    }

    private fun setupFilter() {
        var filterVisible = false
        binding.btnLibFilter.setOnClickListener {
            filterVisible = !filterVisible
            binding.panelLibFilter.visibility = if (filterVisible) View.VISIBLE else View.GONE
            binding.rvLibrary.visibility = if (filterVisible) View.GONE else View.VISIBLE
        }

        binding.btnApplyLibFilter.setOnClickListener {
            val checkedId = binding.chipGroupLibSort.checkedChipId
            val sortType = when (checkedId) {
                binding.chipGroupLibSort.getChildAt(1).id -> "Progress"
                binding.chipGroupLibSort.getChildAt(2).id -> "A-Z"
                binding.chipGroupLibSort.getChildAt(3).id -> "Rating"
                else -> "Newest"
            }
            applyLibSort(sortType)
            binding.panelLibFilter.visibility = View.GONE
            binding.rvLibrary.visibility = View.VISIBLE
            filterVisible = false
        }

        binding.btnResetLibFilter.setOnClickListener {
            binding.chipGroupLibSort.check(binding.chipGroupLibSort.getChildAt(0).id)
            applyLibSort("Newest")
        }
    }

    private fun applyLibSort(type: String) {
        val sortedList = when (type) {
            "Progress" -> allItems.sortedByDescending { it.progress }
            "A-Z" -> allItems.sortedBy { it.comic.title }
            "Rating" -> allItems.sortedByDescending { it.comic.rating }
            else -> allItems // Dummy newest
        }
        filteredItems.clear()
        filteredItems.addAll(sortedList)
        libraryAdapter.notifyDataSetChanged()
    }

    private fun setupRecyclerView() {
        libraryAdapter = LibraryAdapter(filteredItems) { item ->
            val intent = Intent(requireContext(), ReadingActivity::class.java).apply {
                putExtra(ReadingActivity.EXTRA_COMIC_TITLE, item.comic.title)
                putExtra(ReadingActivity.EXTRA_CHAPTER_TITLE, "Chapter ${item.currentChapter}")
                putExtra(ReadingActivity.EXTRA_CHAPTER_ID, item.currentChapter)
                putExtra(ReadingActivity.EXTRA_COMIC_ID, item.comic.id)
            }
            startActivity(intent)
        }
        binding.rvLibrary.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = libraryAdapter
        }
        updateEmptyState()
    }

    private fun setupSearch() {
        binding.etLibrarySearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.lowercase() ?: ""
                filteredItems = if (query.isEmpty()) {
                    allItems.toMutableList()
                } else {
                    allItems.filter {
                        it.comic.title.lowercase().contains(query)
                    }.toMutableList()
                }
                libraryAdapter = LibraryAdapter(filteredItems) { item ->
                    val intent = Intent(requireContext(), ReadingActivity::class.java).apply {
                        putExtra(ReadingActivity.EXTRA_COMIC_TITLE, item.comic.title)
                        putExtra(ReadingActivity.EXTRA_CHAPTER_TITLE, "Chapter ${item.currentChapter}")
                        putExtra(ReadingActivity.EXTRA_CHAPTER_ID, item.currentChapter)
                        putExtra(ReadingActivity.EXTRA_COMIC_ID, item.comic.id)
                    }
                    startActivity(intent)
                }
                binding.rvLibrary.adapter = libraryAdapter
                updateEmptyState()
                updateCount()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun updateCount() {
        binding.tvLibraryCount.text = "${allItems.size} komik tersimpan"
    }

    private fun updateEmptyState() {
        if (filteredItems.isEmpty()) {
            binding.layoutEmpty.visibility = View.VISIBLE
            binding.rvLibrary.visibility = View.GONE
        } else {
            binding.layoutEmpty.visibility = View.GONE
            binding.rvLibrary.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
