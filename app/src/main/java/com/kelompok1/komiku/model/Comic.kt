package com.kelompok1.komiku.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "comics")
data class Comic(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val author: String,
    val format: String,            // Manga / Manhwa / Manhua / Webtoon
    val genre: List<String>,       // Using TypeConverter
    val description: String = "",  // sinopsis
    val status: String = "Ongoing", // Ongoing / Completed
    val rating: Float = 0f,
    val views: String = "0",
    val badge: String = "",        // "HOT", "NEW", "UPDATE", ""
    @ColumnInfo(name = "cover_color_start") val coverColorStart: Int,
    @ColumnInfo(name = "cover_color_end") val coverColorEnd: Int,
    @ColumnInfo(name = "last_update") val lastUpdate: String = "",
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    val chapter: String = "",       // chapter terbaru (redundant but kept for UI compatibility for now)
    @ColumnInfo(name = "cover_path") val coverPath: String? = null // Path ke file gambar cover di internal storage
)

@Entity(tableName = "chapters")
data class Chapter(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "comic_id") val comicId: Int,
    val number: Int,               // nomor chapter, e.g. 97
    val title: String,             // "Chapter 97 - The Final Battle"
    @ColumnInfo(name = "image_paths") val imagePaths: List<String> = emptyList(), // List path gambar
    @ColumnInfo(name = "upload_date") val uploadDate: String
)

@Entity(tableName = "library")
data class Library(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "comic_id") val comicId: Int,
    @ColumnInfo(name = "current_chapter") val currentChapter: Int = 0,
    @ColumnInfo(name = "total_chapter") val totalChapter: Int = 0,
    @ColumnInfo(name = "saved_at") val savedAt: Long = System.currentTimeMillis()
)

data class LibraryComicJoin(
    @Embedded val comic: Comic,
    @Embedded(prefix = "lib_") val library: Library
)
