package com.kelompok1.komiku

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
        updateCount()
    }

    private fun setupRecyclerView() {
        libraryAdapter = LibraryAdapter(filteredItems) { item ->
            // Pastikan bagian ini sudah ada Intent nya!
            val intent = android.content.Intent(requireContext(), ReadingActivity::class.java).apply {
                putExtra("comic_title", item.comic.title)
                putExtra("comic_chapter", item.progressText)
                putExtra("color_start", item.comic.coverColorStart)
                putExtra("color_end", item.comic.coverColorEnd)
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
                libraryAdapter = LibraryAdapter(filteredItems) {}
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
