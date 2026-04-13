package com.kelompok1.komiku

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

class KomiKuApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Terapkan mode yang tersimpan setiap kali app dibuka
        val prefs = getSharedPreferences("komiku_prefs", Context.MODE_PRIVATE)
        val isDark = prefs.getBoolean("dark_mode", false) // ← default false = light
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}