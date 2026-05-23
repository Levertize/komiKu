package com.kelompok1.komiku

import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.kelompok1.komiku.adapter.BannerAdapter
import com.kelompok1.komiku.adapter.ComicGridAdapter
import com.kelompok1.komiku.database.KomiKuDatabase
import com.kelompok1.komiku.databinding.FragmentHomeBinding
import com.kelompok1.komiku.model.Comic
import com.kelompok1.komiku.repository.ComicRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import android.content.Context
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import java.io.File
import java.io.FileOutputStream

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val autoScrollHandler = Handler(Looper.getMainLooper())
    private var autoScrollRunnable: Runnable? = null
    
    private lateinit var comicRepository: ComicRepository

    private val selectAvatarLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            saveAvatarToInternalStorage(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val database = KomiKuDatabase.getDatabase(requireContext())
        comicRepository = ComicRepository(database.comicDao())
        
        // Muat & pasang avatar profil
        val prefs = requireContext().getSharedPreferences("komiku_prefs", Context.MODE_PRIVATE)
        val savedAvatarPath = prefs.getString("avatar_path", null)
        loadAvatarImage(savedAvatarPath)

        // Listener click avatar untuk membuka pratinjau foto profil
        binding.ivAvatar.setOnClickListener {
            showAvatarPreviewDialog()
        }

        // Listener click hamburger untuk menampilkan info pengembang
        binding.btnHamburger.setOnClickListener {
            showAboutDeveloperDialog()
        }

        fixAvatarCircle()
        observeComics()
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
                    .into(binding.ivAvatar)
                return
            }
        }
        // Fallback default
        binding.ivAvatar.setImageResource(R.drawable.ic_avatar_placeholder)
    }

    private fun showAboutDeveloperDialog() {
        try {
            val dialogView = layoutInflater.inflate(R.layout.dialog_about_developer, null)
            val alertDialog = android.app.AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create()

            // Transparan agar layout card membulat keliatan rapi
            alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

            val btnGithub = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_dialog_github)
            val btnClose = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_dialog_close)

            btnGithub.setOnClickListener {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Levertize"))
                    startActivity(intent)
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }

            btnClose.setOnClickListener {
                alertDialog.dismiss()
            }

            alertDialog.show()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
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

    private fun observeComics() {
        viewLifecycleOwner.lifecycleScope.launch {
            comicRepository.getAllComics().collectLatest { comics ->
                if (comics.isNotEmpty()) {
                    setupBanner(comics.take(3))
                    setupPopularGrid(comics.take(6))
                    setupOthersGrid(comics.drop(6))
                }
            }
        }
    }

    private fun fixAvatarCircle() {
        binding.ivAvatar.post {
            val size = binding.ivAvatar.width
            val oval = GradientDrawable()
            oval.shape = GradientDrawable.OVAL
            oval.setColor(0xFF7C5CFC.toInt())
            binding.ivAvatar.background = oval
            binding.ivAvatar.clipToOutline = true
            binding.ivAvatar.outlineProvider = android.view.ViewOutlineProvider.BACKGROUND
        }
    }

    private fun setupBanner(banners: List<Comic>) {
        binding.vpBanner.adapter = BannerAdapter(banners) { comic ->
            val intent = Intent(requireContext(), DetailActivity::class.java).apply {
                putExtra(DetailActivity.EXTRA_COMIC_ID, comic.id)
            }
            startActivity(intent)
        }
        binding.vpBanner.offscreenPageLimit = 1

        // Animasi Page Transformer
        val pageMargin = (12 * resources.displayMetrics.density).toInt()
        binding.vpBanner.setPageTransformer { page, position ->
            val myOffset = position * -(2 * pageMargin)
            if (position < -1) {
                page.translationX = -myOffset
            } else if (position <= 1) {
                val scaleFactor = Math.max(0.9f, 1 - Math.abs(position) * 0.1f)
                page.translationX = myOffset
                page.scaleY = scaleFactor
                page.alpha = scaleFactor
            } else {
                page.alpha = 0f
                page.translationX = myOffset
            }
        }

        setupBannerDots(banners.size)

        binding.vpBanner.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateDots(position, banners.size)
            }
        })

        startAutoScroll(banners.size)
    }

    private fun setupBannerDots(count: Int) {
        binding.llBannerDots.removeAllViews()
        val dp = resources.displayMetrics.density

        repeat(count) { i ->
            val dot = View(requireContext())
            val w = if (i == 0) (13 * dp).toInt() else (5 * dp).toInt()
            val params = LinearLayout.LayoutParams(w, (5 * dp).toInt())
            params.marginStart = (4 * dp).toInt()

            val bg = GradientDrawable()
            bg.cornerRadius = if (i == 0) 3f * dp else 50f * dp
            bg.setColor(if (i == 0) 0xFF7C5CFC.toInt() else 0x30F0EEF8.toInt())

            dot.background = bg
            dot.layoutParams = params
            binding.llBannerDots.addView(dot)
        }
    }

    private fun updateDots(selected: Int, count: Int) {
        val dp = resources.displayMetrics.density
        repeat(binding.llBannerDots.childCount) { i ->
            val dot = binding.llBannerDots.getChildAt(i)
            val params = dot.layoutParams as LinearLayout.LayoutParams
            val bg = GradientDrawable()
            if (i == selected) {
                bg.setColor(0xFF7C5CFC.toInt())
                bg.cornerRadius = 3f * dp
                params.width = (13 * dp).toInt()
            } else {
                bg.setColor(0x30F0EEF8.toInt())
                bg.cornerRadius = 50f * dp
                params.width = (5 * dp).toInt()
            }
            dot.background = bg
            dot.layoutParams = params
        }
    }

    private fun startAutoScroll(count: Int) {
        autoScrollRunnable?.let { autoScrollHandler.removeCallbacks(it) }
        autoScrollRunnable = object : Runnable {
            override fun run() {
                val next = (binding.vpBanner.currentItem + 1) % count
                binding.vpBanner.setCurrentItem(next, true)
                autoScrollHandler.postDelayed(this, 3000)
            }
        }
        autoScrollHandler.postDelayed(autoScrollRunnable!!, 3000)
    }

    private fun setupPopularGrid(comics: List<Comic>) {
        binding.rvPopular.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.rvPopular.adapter = ComicGridAdapter(comics) { comic ->
            val intent = Intent(requireContext(), DetailActivity::class.java).apply {
                putExtra(DetailActivity.EXTRA_COMIC_ID, comic.id)
            }
            startActivity(intent)
        }
        binding.rvPopular.isNestedScrollingEnabled = false
    }

    private fun setupOthersGrid(comics: List<Comic>) {
        binding.rvOthers.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.rvOthers.adapter = ComicGridAdapter(comics) { comic ->
            val intent = Intent(requireContext(), DetailActivity::class.java).apply {
                putExtra(DetailActivity.EXTRA_COMIC_ID, comic.id)
            }
            startActivity(intent)
        }
        binding.rvOthers.isNestedScrollingEnabled = false
    }

    override fun onPause() {
        super.onPause()
        autoScrollRunnable?.let { autoScrollHandler.removeCallbacks(it) }
    }

    override fun onResume() {
        super.onResume()
        // vpBanner might not be initialized yet if database is empty
        if (binding.vpBanner.adapter != null && (binding.vpBanner.adapter?.itemCount ?: 0) > 0) {
            startAutoScroll(binding.vpBanner.adapter!!.itemCount)
        }
        
        // Refresh avatar & glow settings
        val prefs = requireContext().getSharedPreferences("komiku_prefs", Context.MODE_PRIVATE)
        val savedAvatarPath = prefs.getString("avatar_path", null)
        loadAvatarImage(savedAvatarPath)
        binding.layoutAvatarGlow.loadGlowSettings()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        autoScrollRunnable?.let { autoScrollHandler.removeCallbacks(it) }
        _binding = null
    }
}
