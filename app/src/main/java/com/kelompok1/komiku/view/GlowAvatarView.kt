package com.kelompok1.komiku.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import com.kelompok1.komiku.R

class GlowAvatarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    // Glow configurations
    var isGlowEnabled: Boolean = false
        set(value) {
            field = value
            updateAnimationState()
            invalidate()
        }
        
    var glowColor: Int = Color.CYAN
        set(value) {
            field = value
            invalidate()
        }

    // 0: Solid, 1: Breathing, 2: RGB Wave, 3: Neon Blink
    var glowStyle: Int = 0
        set(value) {
            field = value
            updateAnimationState()
            invalidate()
        }

    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dpToPx(2.5f)
    }

    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dpToPx(6f)
    }

    private var rotationAngle = 0f
    private var pulseProgress = 1.0f
    private var blinkAlpha = 255

    private var rotationAnimator: ValueAnimator? = null
    private var pulseAnimator: ValueAnimator? = null
    private var blinkAnimator: ValueAnimator? = null

    // Rainbow colors for SweepGradient
    private val rainbowColors = intArrayOf(
        Color.RED,
        Color.YELLOW,
        Color.GREEN,
        Color.CYAN,
        Color.BLUE,
        Color.MAGENTA,
        Color.RED
    )
    private val rainbowPositions = floatArrayOf(0f, 0.16f, 0.33f, 0.5f, 0.66f, 0.83f, 1f)

    init {
        // Required for BlurMaskFilter or setShadowLayer to work properly on some API levels
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        setWillNotDraw(false)
        loadGlowSettings()
    }

    fun loadGlowSettings() {
        val prefs = context.getSharedPreferences("komiku_prefs", Context.MODE_PRIVATE)
        isGlowEnabled = prefs.getBoolean("avatar_glow_enabled", false)
        glowColor = prefs.getInt("avatar_glow_color", 0xFF00F3FF.toInt()) // Default Cyan
        glowStyle = prefs.getInt("avatar_glow_style", 0) // Default Solid
        updateAnimationState()
        invalidate()
    }

    private fun updateAnimationState() {
        // Stop all animators first
        stopAnimators()

        if (!isGlowEnabled) return

        when (glowStyle) {
            1 -> { // Breathing
                pulseAnimator = ValueAnimator.ofFloat(0.4f, 1.2f).apply {
                    duration = 1800
                    repeatCount = ValueAnimator.INFINITE
                    repeatMode = ValueAnimator.REVERSE
                    interpolator = AccelerateDecelerateInterpolator()
                    addUpdateListener {
                        pulseProgress = it.animatedValue as Float
                        invalidate()
                    }
                    start()
                }
            }
            2 -> { // RGB Wave (Rotation)
                rotationAnimator = ValueAnimator.ofFloat(0f, 360f).apply {
                    duration = 3000
                    repeatCount = ValueAnimator.INFINITE
                    interpolator = LinearInterpolator()
                    addUpdateListener {
                        rotationAngle = it.animatedValue as Float
                        invalidate()
                    }
                    start()
                }
            }
            3 -> { // Neon Blink
                blinkAnimator = ValueAnimator.ofInt(255, 30, 255, 255, 0, 255).apply {
                    duration = 2000
                    repeatCount = ValueAnimator.INFINITE
                    addUpdateListener {
                        blinkAlpha = it.animatedValue as Int
                        invalidate()
                    }
                    start()
                }
            }
        }
    }

    private fun stopAnimators() {
        rotationAnimator?.cancel()
        rotationAnimator = null
        pulseAnimator?.cancel()
        pulseAnimator = null
        blinkAnimator?.cancel()
        blinkAnimator = null
        pulseProgress = 1.0f
        rotationAngle = 0f
        blinkAlpha = 255
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnimators()
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)

        if (!isGlowEnabled) return

        val child = getChildAt(0) ?: return
        val cx = width / 2f
        val cy = height / 2f
        
        // Use half of child's width as base radius plus a small offset
        val baseRadius = (child.width / 2f) + dpToPx(1f)

        // Set up paints based on style
        var currentColor = glowColor
        
        // Adjust alpha for blinking effect
        if (glowStyle == 3) {
            currentColor = Color.argb(
                blinkAlpha,
                Color.red(glowColor),
                Color.green(glowColor),
                Color.blue(glowColor)
            )
        }

        // Apply shader for RGB Wave
        if (glowStyle == 2) {
            val matrix = Matrix()
            matrix.setRotate(rotationAngle, cx, cy)
            
            val sweepGradient = SweepGradient(cx, cy, rainbowColors, rainbowPositions)
            sweepGradient.setLocalMatrix(matrix)
            
            borderPaint.shader = sweepGradient
            
            // Outer glow for RGB wave
            val glowSweep = SweepGradient(cx, cy, rainbowColors, rainbowPositions)
            glowSweep.setLocalMatrix(matrix)
            glowPaint.shader = glowSweep
        } else {
            borderPaint.shader = null
            borderPaint.color = currentColor
            
            glowPaint.shader = null
            glowPaint.color = currentColor
        }

        // Setup outer glow blur radius
        val blurRadius = if (glowStyle == 1) {
            dpToPx(6f) * pulseProgress
        } else {
            dpToPx(6f)
        }

        if (blurRadius > 0.1f) {
            glowPaint.maskFilter = BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL)
            
            // Adjust opacity of glow
            val originalAlpha = if (glowStyle == 3) blinkAlpha else 180
            glowPaint.alpha = (originalAlpha * if (glowStyle == 1) (0.5f + 0.5f * pulseProgress) else 1f).toInt().coerceIn(0, 255)
            
            // Draw outer glow circle
            canvas.drawCircle(cx, cy, baseRadius, glowPaint)
        }

        // Draw solid sharp border on top
        borderPaint.alpha = if (glowStyle == 3) blinkAlpha else 255
        canvas.drawCircle(cx, cy, baseRadius, borderPaint)
    }

    private fun dpToPx(dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }
}
