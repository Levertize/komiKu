package com.kelompok1.komiku.model

data class Comic(
    val id: Int,
    val title: String,
    val author: String,
    val chapter: String,
    val format: String,       // Manga / Manhwa / Manhua / Webtoon
    val genre: List<String>,
    val badge: String,        // "HOT", "NEW", "UPDATE", atau ""
    val rating: Float,
    val views: String,        // "12.4M"
    val lastUpdate: String,   // "2 jam lalu"
    val coverColorStart: Int, // warna gradient cover (hex ARGB)
    val coverColorEnd: Int
)
