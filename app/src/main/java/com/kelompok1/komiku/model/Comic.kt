package com.kelompok1.komiku.model

data class Comic(
    val id: Int,
    val title: String,
    val author: String,
    val chapter: String,           // chapter terbaru, e.g. "Ch. 1111"
    val format: String,            // Manga / Manhwa / Manhua / Webtoon
    val genre: List<String>,
    val badge: String,             // "HOT", "NEW", "UPDATE", ""
    val rating: Float,
    val views: String,             // "12.4M"
    val lastUpdate: String,        // "2 jam lalu"
    val coverColorStart: Int,
    val coverColorEnd: Int,
    val description: String = "",  // sinopsis
    val status: String = "Ongoing" // Ongoing / Completed
)

data class Chapter(
    val id: Int,
    val comicId: Int,
    val number: Int,               // nomor chapter, e.g. 97
    val title: String,             // "Chapter 97 - The Final Battle"
    val uploadDate: String,        // "2 hari lalu"
    val pages: List<String> = emptyList() // list URL gambar (dummy pakai warna)
)
