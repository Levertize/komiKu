package com.kelompok1.komiku

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.kelompok1.komiku.databinding.FragmentPengaturanBinding

class PengaturanFragment : Fragment() {

    private var _binding: FragmentPengaturanBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPengaturanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext()
            .getSharedPreferences("komiku_prefs", Context.MODE_PRIVATE)

        // Muat state tersimpan
        val isDark = prefs.getBoolean("dark_mode", false) // ← samain false
        binding.switchDarkMode.isChecked = isDark
        updateDarkModeStatus(isDark)

        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("dark_mode", isChecked).apply()
            updateDarkModeStatus(isChecked)

            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )

            // Animasi aman pakai overridePendingTransition saja
            requireActivity().overridePendingTransition(
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
        }
    }

    private fun updateDarkModeStatus(isDark: Boolean) {
        binding.tvDarkModeStatus.text = if (isDark)
            "Aktif · Nyaman untuk mata"
        else
            "Nonaktif · Mode terang"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}