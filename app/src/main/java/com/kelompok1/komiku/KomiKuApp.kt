package com.kelompok1.komiku

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.kelompok1.komiku.database.DatabaseSeeder
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class KomiKuApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Seed database
        MainScope().launch {
            DatabaseSeeder.seedDatabase(this@KomiKuApp)
        }

        // Terapkan mode yang tersimpan setiap kali app dibuka
        val prefs = getSharedPreferences("komiku_prefs", Context.MODE_PRIVATE)
        val isDark = prefs.getBoolean("dark_mode", false) // ← default false = light
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}