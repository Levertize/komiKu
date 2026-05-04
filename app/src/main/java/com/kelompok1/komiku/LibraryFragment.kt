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
import androidx.recyclerview.widget.GridLayoutManager
import com.kelompok1.komiku.adapter.LibraryAdapter
import com.kelompok1.komiku.database.KomiKuDatabase
import com.kelompok1.komiku.databinding.FragmentLibraryBinding
import com.kelompok1.komiku.model.LibraryComicJoin
import com.kelompok1.komiku.repository.ChapterRepository
import com.kelompok1.komiku.repository.ComicRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LibraryFragment : Fragment() {

    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!

    private lateinit var comicRepository: ComicRepository
    private lateinit var chapterRepository: ChapterRepository
    private var allItems = mutableListOf<LibraryComicJoin>()
    private var filteredItems = mutableListOf<LibraryComicJoin>()
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
        
        val database = KomiKuDatabase.getDatabase(requireContext())
        comicRepository = ComicRepository(database.comicDao())
        chapterRepository = ChapterRepository(database.chapterDao())
        
        setupRecyclerView()
        setupSearch()
        setupFilter()
        observeLibrary()
    }

    private fun observeLibrary() {
        viewLifecycleOwner.lifecycleScope.launch {
            comicRepository.getLibraryComics().collectLatest { items ->
                allItems.clear()
                allItems.addAll(items)
                applySearch(binding.etLibrarySearch.text?.toString() ?: "")
                updateCount()
            }
        }
    }

    private fun setupRecyclerView() {
        libraryAdapter = LibraryAdapter(filteredItems) { item ->
            viewLifecycleOwner.lifecycleScope.launch {
                val chapters = chapterRepository.getChaptersByComicId(item.comic.id).first()
                if (chapters.isNotEmpty()) {
                    // Try to find the chapter matching current progress, otherwise take the first available
                    val targetChapter = chapters.find { it.number == item.library.currentChapter } ?: chapters.last()
                    
                    val intent = Intent(requireContext(), ReadingActivity::class.java).apply {
                        putExtra(ReadingActivity.EXTRA_COMIC_TITLE, item.comic.title)
                        putExtra(ReadingActivity.EXTRA_CHAPTER_TITLE, targetChapter.title)
                        putExtra(ReadingActivity.EXTRA_CHAPTER_ID, targetChapter.id)
                        putExtra(ReadingActivity.EXTRA_COMIC_ID, item.comic.id)
                    }
                    startActivity(intent)
                } else {
                    // Fallback to detail if no chapters found
                    val intent = Intent(requireContext(), DetailActivity::class.java).apply {
                        putExtra(DetailActivity.EXTRA_COMIC_ID, item.comic.id)
                    }
                    startActivity(intent)
                }
            }
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
                applySearch(s?.toString() ?: "")
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun applySearch(query: String) {
        val q = query.lowercase()
        val filtered = if (q.isEmpty()) {
            allItems
        } else {
            allItems.filter {
                it.comic.title.lowercase().contains(q)
            }
        }
        filteredItems.clear()
        filteredItems.addAll(filtered)
        libraryAdapter.notifyDataSetChanged()
        updateEmptyState()
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
            "Progress" -> {
                allItems.sortedByDescending { 
                    if (it.library.totalChapter > 0) it.library.currentChapter.toFloat() / it.library.totalChapter else 0f
                }
            }
            "A-Z" -> allItems.sortedBy { it.comic.title }
            "Rating" -> allItems.sortedByDescending { it.comic.rating }
            else -> allItems // Default: saved_at DESC (handled by DAO)
        }
        filteredItems.clear()
        filteredItems.addAll(sortedList)
        libraryAdapter.notifyDataSetChanged()
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
