package com.kelompok1.komiku

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import android.graphics.Color
import com.bumptech.glide.Glide
import com.kelompok1.komiku.databinding.FragmentPengaturanBinding
import java.io.File
import java.io.FileOutputStream

class PengaturanFragment : Fragment() {

    private var _binding: FragmentPengaturanBinding? = null
    private val binding get() = _binding!!

    private val selectAvatarLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            saveAvatarToInternalStorage(it)
        }
    }

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

        // Muat & pasang avatar profil
        val savedAvatarPath = prefs.getString("avatar_path", null)
        loadAvatarImage(savedAvatarPath)

        // Jadikan settings avatar berbentuk lingkaran sempurna
        binding.ivSettingsAvatar.post {
            binding.ivSettingsAvatar.clipToOutline = true
            binding.ivSettingsAvatar.outlineProvider = android.view.ViewOutlineProvider.BACKGROUND
        }

        // Listener click avatar untuk membuka pratinjau foto profil
        binding.rowUpdateProfile.setOnClickListener {
            showAvatarPreviewDialog()
        }

        // Muat & pasang status glow avatar
        updateGlowStatusText()

        // Listener click efek glow untuk membuka dialog kustomisasi glow
        binding.rowGlowEffect.setOnClickListener {
            showGlowCustomizeDialog()
        }

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

    override fun onResume() {
        super.onResume()
        val prefs = requireContext().getSharedPreferences("komiku_prefs", Context.MODE_PRIVATE)
        val savedAvatarPath = prefs.getString("avatar_path", null)
        loadAvatarImage(savedAvatarPath)
        binding.layoutSettingsAvatarGlow.loadGlowSettings()
        updateGlowStatusText()
    }

    private fun saveAvatarToInternalStorage(uri: Uri) {
        try {
            val file = File(requireContext().filesDir, "avatar_user.jpg")
            val inputStream = requireContext().contentResolver.openInputStream(uri) ?: return
            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            
            // Simpan path ke SharedPreferences
            val prefs = requireContext().getSharedPreferences("komiku_prefs", Context.MODE_PRIVATE)
            prefs.edit().putString("avatar_path", file.absolutePath).apply()
            
            // Muat gambar profil baru
            loadAvatarImage(file.absolutePath)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun loadAvatarImage(path: String?) {
        if (!path.isNullOrEmpty()) {
            val file = File(path)
            if (file.exists()) {
                Glide.with(this)
                    .load(file)
                    .circleCrop()
                    .into(binding.ivSettingsAvatar)
                return
            }
        }
        // Fallback default
        binding.ivSettingsAvatar.setImageResource(R.drawable.ic_avatar_placeholder)
    }

    private fun showAvatarPreviewDialog() {
        try {
            val dialogView = layoutInflater.inflate(R.layout.dialog_avatar_preview, null)
            val alertDialog = android.app.AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create()

            // Transparan agar layout card membulat keliatan rapi
            alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

            val ivPreview = dialogView.findViewById<android.widget.ImageView>(R.id.iv_dialog_avatar_preview)
            val btnChange = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_avatar_change)
            val btnDelete = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_avatar_delete)
            val btnClose = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_avatar_close)

            // Muat foto profil saat ini ke pratinjau dialog
            val prefs = requireContext().getSharedPreferences("komiku_prefs", Context.MODE_PRIVATE)
            val savedAvatarPath = prefs.getString("avatar_path", null)

            var hasCustomAvatar = false
            if (!savedAvatarPath.isNullOrEmpty()) {
                val file = File(savedAvatarPath)
                if (file.exists()) {
                    hasCustomAvatar = true
                    Glide.with(this)
                        .load(file)
                        .circleCrop()
                        .into(ivPreview)
                }
            }
            if (!hasCustomAvatar) {
                ivPreview.setImageResource(R.drawable.ic_avatar_placeholder)
                btnDelete.visibility = View.GONE // Sembunyikan tombol hapus jika masih menggunakan default placeholder
            }

            // Aksi Ubah Foto
            btnChange.setOnClickListener {
                alertDialog.dismiss()
                selectAvatarLauncher.launch("image/*")
            }

            // Aksi Hapus Foto kustom
            btnDelete.setOnClickListener {
                android.app.AlertDialog.Builder(requireContext())
                    .setTitle("Hapus Foto Profil")
                    .setMessage("Apakah Anda yakin ingin menghapus foto profil kustom dan kembali menggunakan default?")
                    .setPositiveButton("Hapus") { _, _ ->
                        try {
                            if (!savedAvatarPath.isNullOrEmpty()) {
                                val file = File(savedAvatarPath)
                                if (file.exists()) {
                                    file.delete()
                                }
                            }
                            prefs.edit().remove("avatar_path").apply()
                            loadAvatarImage(null) // Reset di UI utama
                            alertDialog.dismiss()
                            android.widget.Toast.makeText(requireContext(), "Foto profil berhasil dihapus", android.widget.Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    .setNegativeButton("Batal", null)
                    .show()
            }

            // Aksi Batal
            btnClose.setOnClickListener {
                alertDialog.dismiss()
            }

            alertDialog.show()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun updateGlowStatusText() {
        val prefs = requireContext().getSharedPreferences("komiku_prefs", Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean("avatar_glow_enabled", false)
        if (!isEnabled) {
            binding.tvGlowStatus.text = "Nonaktif"
            return
        }
        val style = prefs.getInt("avatar_glow_style", 0)
        val styleName = when (style) {
            0 -> "Solid"
            1 -> "Breathing"
            2 -> "RGB Wave"
            3 -> "Neon Blink"
            else -> "Solid"
        }
        binding.tvGlowStatus.text = "Aktif · $styleName"
    }

    private fun showGlowCustomizeDialog() {
        try {
            val dialogView = layoutInflater.inflate(R.layout.dialog_customize_glow, null)
            val alertDialog = android.app.AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create()

            alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

            val previewGlowContainer = dialogView.findViewById<com.kelompok1.komiku.view.GlowAvatarView>(R.id.dialog_preview_glow_container)
            val ivGlowPreview = dialogView.findViewById<android.widget.ImageView>(R.id.iv_dialog_glow_preview)
            val switchGlowEnable = dialogView.findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.switch_glow_enable)
            val layoutGlowOptions = dialogView.findViewById<android.widget.LinearLayout>(R.id.layout_glow_options)

            val btnCyan = dialogView.findViewById<android.view.View>(R.id.btn_color_cyan)
            val btnRed = dialogView.findViewById<android.view.View>(R.id.btn_color_red)
            val btnGold = dialogView.findViewById<android.view.View>(R.id.btn_color_gold)
            val btnGreen = dialogView.findViewById<android.view.View>(R.id.btn_color_green)
            val btnPurple = dialogView.findViewById<android.view.View>(R.id.btn_color_purple)

            val borderCyan = dialogView.findViewById<android.view.View>(R.id.border_color_cyan)
            val borderRed = dialogView.findViewById<android.view.View>(R.id.border_color_red)
            val borderGold = dialogView.findViewById<android.view.View>(R.id.border_color_gold)
            val borderGreen = dialogView.findViewById<android.view.View>(R.id.border_color_green)
            val borderPurple = dialogView.findViewById<android.view.View>(R.id.border_color_purple)

            val rgGlowStyle = dialogView.findViewById<android.widget.RadioGroup>(R.id.rg_glow_style)
            val rbSolid = dialogView.findViewById<android.widget.RadioButton>(R.id.rb_style_solid)
            val rbBreathing = dialogView.findViewById<android.widget.RadioButton>(R.id.rb_style_breathing)
            val rbRgb = dialogView.findViewById<android.widget.RadioButton>(R.id.rb_style_rgb)
            val rbBlink = dialogView.findViewById<android.widget.RadioButton>(R.id.rb_style_blink)

            val btnCancel = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_glow_cancel)
            val btnSave = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_glow_save)

            // Muat foto profil saat ini ke pratinjau dialog kustomisasi
            val prefs = requireContext().getSharedPreferences("komiku_prefs", Context.MODE_PRIVATE)
            val savedAvatarPath = prefs.getString("avatar_path", null)
            if (!savedAvatarPath.isNullOrEmpty()) {
                val file = File(savedAvatarPath)
                if (file.exists()) {
                    Glide.with(this)
                        .load(file)
                        .circleCrop()
                        .into(ivGlowPreview)
                }
            } else {
                ivGlowPreview.setImageResource(R.drawable.ic_avatar_placeholder)
            }

            // Muat state kustomisasi saat ini
            var isGlowEnabled = prefs.getBoolean("avatar_glow_enabled", false)
            var selectedColor = prefs.getInt("avatar_glow_color", Color.parseColor("#00F3FF"))
            var selectedStyle = prefs.getInt("avatar_glow_style", 0)

            switchGlowEnable.isChecked = isGlowEnabled
            layoutGlowOptions.visibility = if (isGlowEnabled) View.VISIBLE else View.GONE

            // Terapkan ke pratinjau live dialog
            previewGlowContainer.isGlowEnabled = isGlowEnabled
            previewGlowContainer.glowColor = selectedColor
            previewGlowContainer.glowStyle = selectedStyle

            // Atur pilihan warna aktif
            fun updateColorBorders() {
                borderCyan.visibility = if (selectedColor == Color.parseColor("#00F3FF")) View.VISIBLE else View.GONE
                borderRed.visibility = if (selectedColor == Color.parseColor("#FF0055")) View.VISIBLE else View.GONE
                borderGold.visibility = if (selectedColor == Color.parseColor("#FFD700")) View.VISIBLE else View.GONE
                borderGreen.visibility = if (selectedColor == Color.parseColor("#39FF14")) View.VISIBLE else View.GONE
                borderPurple.visibility = if (selectedColor == Color.parseColor("#9D00FF")) View.VISIBLE else View.GONE
            }
            updateColorBorders()

            // Atur pilihan gaya aktif
            when (selectedStyle) {
                0 -> rbSolid.isChecked = true
                1 -> rbBreathing.isChecked = true
                2 -> rbRgb.isChecked = true
                3 -> rbBlink.isChecked = true
            }

            // Click listener untuk Switch enable/disable glow
            switchGlowEnable.setOnCheckedChangeListener { _, isChecked ->
                isGlowEnabled = isChecked
                layoutGlowOptions.visibility = if (isChecked) View.VISIBLE else View.GONE
                previewGlowContainer.isGlowEnabled = isChecked
            }

            // Click listener untuk warna
            btnCyan.setOnClickListener {
                selectedColor = Color.parseColor("#00F3FF")
                updateColorBorders()
                previewGlowContainer.glowColor = selectedColor
            }
            btnRed.setOnClickListener {
                selectedColor = Color.parseColor("#FF0055")
                updateColorBorders()
                previewGlowContainer.glowColor = selectedColor
            }
            btnGold.setOnClickListener {
                selectedColor = Color.parseColor("#FFD700")
                updateColorBorders()
                previewGlowContainer.glowColor = selectedColor
            }
            btnGreen.setOnClickListener {
                selectedColor = Color.parseColor("#39FF14")
                updateColorBorders()
                previewGlowContainer.glowColor = selectedColor
            }
            btnPurple.setOnClickListener {
                selectedColor = Color.parseColor("#9D00FF")
                updateColorBorders()
                previewGlowContainer.glowColor = selectedColor
            }

            // Listener untuk gaya
            rgGlowStyle.setOnCheckedChangeListener { _, checkedId ->
                selectedStyle = when (checkedId) {
                    R.id.rb_style_solid -> 0
                    R.id.rb_style_breathing -> 1
                    R.id.rb_style_rgb -> 2
                    R.id.rb_style_blink -> 3
                    else -> 0
                }
                previewGlowContainer.glowStyle = selectedStyle
            }

            // Aksi Batal
            btnCancel.setOnClickListener {
                alertDialog.dismiss()
            }

            // Aksi Simpan
            btnSave.setOnClickListener {
                prefs.edit().apply {
                    putBoolean("avatar_glow_enabled", isGlowEnabled)
                    putInt("avatar_glow_color", selectedColor)
                    putInt("avatar_glow_style", selectedStyle)
                    apply()
                }

                // Update UI utama settings
                binding.layoutSettingsAvatarGlow.loadGlowSettings()
                updateGlowStatusText()

                alertDialog.dismiss()
                android.widget.Toast.makeText(requireContext(), "Efek glow profil berhasil disimpan", android.widget.Toast.LENGTH_SHORT).show()
            }

            alertDialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}