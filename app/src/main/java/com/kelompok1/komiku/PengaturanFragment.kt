package com.kelompok1.komiku

import android.content.Context
import android.content.Intent
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

        // Muat state Mode Membaca
        val isHorizontal = prefs.getBoolean("read_horizontal_mode", false)
        updateReadModeStatus(isHorizontal)

        binding.rowReadMode.setOnClickListener {
            val options = arrayOf("Scroll Vertikal (Gaya Webtoon)", "Swipe Horisontal (Gaya Buku)")
            val currentSelection = if (prefs.getBoolean("read_horizontal_mode", false)) 1 else 0

            android.app.AlertDialog.Builder(requireContext())
                .setTitle("Pilih Mode Membaca")
                .setSingleChoiceItems(options, currentSelection) { dialog, which ->
                    val isHorizontalSelected = which == 1
                    prefs.edit().putBoolean("read_horizontal_mode", isHorizontalSelected).apply()
                    updateReadModeStatus(isHorizontalSelected)
                    dialog.dismiss()
                }
                .setNegativeButton("Batal", null)
                .show()
        }

        // Muat state Notifikasi Update
        val enableNotif = prefs.getBoolean("enable_notifications", true)
        binding.switchNotif.isChecked = enableNotif

        binding.switchNotif.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("enable_notifications", isChecked).apply()
        }

        // Tombol Logout
        binding.rowLogout.setOnClickListener {
            prefs.edit()
                .putBoolean("is_logged_in", false)
                .putBoolean("is_admin", false)
                .apply()
            
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun updateDarkModeStatus(isDark: Boolean) {
        binding.tvDarkModeStatus.text = if (isDark)
            "Aktif · Nyaman untuk mata"
        else
            "Nonaktif · Mode terang"
    }

    private fun updateReadModeStatus(isHorizontal: Boolean) {
        binding.tvReadModeVal.text = if (isHorizontal)
            "Swipe Horisontal (Gaya Buku)"
        else
            "Scroll Vertikal (Gaya Webtoon)"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}