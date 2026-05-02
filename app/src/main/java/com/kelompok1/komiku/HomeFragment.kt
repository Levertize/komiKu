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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.kelompok1.komiku.adapter.BannerAdapter
import com.kelompok1.komiku.adapter.ComicGridAdapter
import com.kelompok1.komiku.data.DummyData
import com.kelompok1.komiku.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val autoScrollHandler = Handler(Looper.getMainLooper())
    private var autoScrollRunnable: Runnable? = null

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
        fixAvatarCircle()
        setupBanner()
        setupPopularGrid()
        setupOthersGrid()
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

    private fun setupBanner() {
        val banners = DummyData.bannerComics
        binding.vpBanner.adapter = BannerAdapter(banners)
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

    private fun setupPopularGrid() {
        binding.rvPopular.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.rvPopular.adapter = ComicGridAdapter(DummyData.popularComics) { comic ->
            val intent = Intent(requireContext(), DetailActivity::class.java).apply {
                putExtra(DetailActivity.EXTRA_COMIC_ID, comic.id)
            }
            startActivity(intent)
        }
        binding.rvPopular.isNestedScrollingEnabled = false
    }

    private fun setupOthersGrid() {
        binding.rvOthers.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.rvOthers.adapter = ComicGridAdapter(DummyData.otherComics) { comic ->
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
        startAutoScroll(DummyData.bannerComics.size)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        autoScrollRunnable?.let { autoScrollHandler.removeCallbacks(it) }
        _binding = null
    }
}
