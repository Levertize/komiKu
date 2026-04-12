package com.kelompok1.komiku.data

import android.graphics.Color
import com.kelompok1.komiku.model.Comic

object DummyData {

    val comicList: List<Comic> = listOf(

        Comic(
            id = 1,
            title = "One Piece",
            author = "Eiichiro Oda",
            chapter = "Ch. 1111",
            format = "Manga",
            genre = listOf("Action", "Adventure"),
            badge = "HOT",
            rating = 9.2f,
            views = "12.4M",
            lastUpdate = "2 jam lalu",
            coverColorStart = Color.parseColor("#1A0E35"),
            coverColorEnd = Color.parseColor("#0D1840")
        ),

        Comic(
            id = 2,
            title = "Naruto Shippuden",
            author = "Masashi Kishimoto",
            chapter = "Ch. 97",
            format = "Manga",
            genre = listOf("Action", "Ninja"),
            badge = "UPDATE",
            rating = 8.9f,
            views = "9.8M",
            lastUpdate = "1 hari lalu",
            coverColorStart = Color.parseColor("#0A1A06"),
            coverColorEnd = Color.parseColor("#182A0A")
        ),

        Comic(
            id = 3,
            title = "Jujutsu Kaisen",
            author = "Gege Akutami",
            chapter = "Ch. 264",
            format = "Manga",
            genre = listOf("Horror", "Action"),
            badge = "HOT",
            rating = 9.0f,
            views = "8.1M",
            lastUpdate = "3 hari lalu",
            coverColorStart = Color.parseColor("#1A0808"),
            coverColorEnd = Color.parseColor("#2A0A14")
        ),

        Comic(
            id = 4,
            title = "Demon Slayer",
            author = "Koyoharu Gotouge",
            chapter = "Ch. 205",
            format = "Manga",
            genre = listOf("Action", "Fantasy"),
            badge = "NEW",
            rating = 9.1f,
            views = "7.5M",
            lastUpdate = "5 hari lalu",
            coverColorStart = Color.parseColor("#060610"),
            coverColorEnd = Color.parseColor("#10102A")
        ),

        Comic(
            id = 5,
            title = "Attack on Titan",
            author = "Hajime Isayama",
            chapter = "Ch. 139",
            format = "Manga",
            genre = listOf("Action", "Drama"),
            badge = "",
            rating = 9.5f,
            views = "11.2M",
            lastUpdate = "1 mgg lalu",
            coverColorStart = Color.parseColor("#081408"),
            coverColorEnd = Color.parseColor("#0A1A0A")
        ),

        Comic(
            id = 6,
            title = "Bleach",
            author = "Tite Kubo",
            chapter = "Ch. 686",
            format = "Manga",
            genre = listOf("Action", "Supernatural"),
            badge = "NEW",
            rating = 8.7f,
            views = "6.5M",
            lastUpdate = "1 mgg lalu",
            coverColorStart = Color.parseColor("#180A00"),
            coverColorEnd = Color.parseColor("#241200")
        ),

        Comic(
            id = 7,
            title = "Solo Leveling",
            author = "Chugong",
            chapter = "Ch. 179",
            format = "Manhwa",
            genre = listOf("Action", "Fantasy"),
            badge = "HOT",
            rating = 9.4f,
            views = "7.3M",
            lastUpdate = "5 hari lalu",
            coverColorStart = Color.parseColor("#182A20"),
            coverColorEnd = Color.parseColor("#0A2018")
        ),

        Comic(
            id = 8,
            title = "Tower of God",
            author = "SIU",
            chapter = "Ch. 550",
            format = "Manhwa",
            genre = listOf("Action", "Adventure"),
            badge = "UPDATE",
            rating = 8.8f,
            views = "5.9M",
            lastUpdate = "3 hari lalu",
            coverColorStart = Color.parseColor("#0A0820"),
            coverColorEnd = Color.parseColor("#14103A")
        ),

        Comic(
            id = 9,
            title = "Vinland Saga",
            author = "Makoto Yukimura",
            chapter = "Ch. 208",
            format = "Manga",
            genre = listOf("Action", "Historical"),
            badge = "",
            rating = 9.3f,
            views = "4.2M",
            lastUpdate = "2 mgg lalu",
            coverColorStart = Color.parseColor("#201008"),
            coverColorEnd = Color.parseColor("#301808")
        ),

        Comic(
            id = 10,
            title = "Dragon Ball Super",
            author = "Akira Toriyama",
            chapter = "Ch. 101",
            format = "Manga",
            genre = listOf("Action", "Sci-fi"),
            badge = "UPDATE",
            rating = 8.5f,
            views = "8.9M",
            lastUpdate = "1 hari lalu",
            coverColorStart = Color.parseColor("#1A1000"),
            coverColorEnd = Color.parseColor("#2A1C00")
        ),

        Comic(
            id = 11,
            title = "My Hero Academia",
            author = "Kohei Horikoshi",
            chapter = "Ch. 430",
            format = "Manga",
            genre = listOf("Action", "Superhero"),
            badge = "HOT",
            rating = 8.6f,
            views = "6.8M",
            lastUpdate = "2 jam lalu",
            coverColorStart = Color.parseColor("#001A0A"),
            coverColorEnd = Color.parseColor("#002814")
        ),

        Comic(
            id = 12,
            title = "Chainsaw Man",
            author = "Tatsuki Fujimoto",
            chapter = "Ch. 168",
            format = "Manga",
            genre = listOf("Horror", "Action"),
            badge = "NEW",
            rating = 9.1f,
            views = "5.4M",
            lastUpdate = "4 hari lalu",
            coverColorStart = Color.parseColor("#200000"),
            coverColorEnd = Color.parseColor("#300808")
        )
    )

    // Data untuk Home — Terpopuler (6 item pertama)
    val popularComics get() = comicList.take(6)

    // Data untuk Home — Komik Lainnya (6 item berikutnya)
    val otherComics get() = comicList.drop(6)

    // Data untuk Jelajahi — semua komik
    val exploreComics get() = comicList

    // Data untuk Library — simulasi komik yang disimpan (4 item)
    data class LibraryComic(
        val comic: Comic,
        val currentChapter: Int,
        val totalChapter: Int
    ) {
        val progress: Int get() = ((currentChapter.toFloat() / totalChapter) * 100).toInt()
        val progressText: String get() = "Ch. $currentChapter / $totalChapter"
    }

    val libraryComics = listOf(
        LibraryComic(comicList[0], 778, 1111),
        LibraryComic(comicList[1], 87, 97),
        LibraryComic(comicList[2], 79, 264),
        LibraryComic(comicList[3], 92, 205),
        LibraryComic(comicList[6], 150, 179),
        LibraryComic(comicList[4], 100, 139)
    )

    // Data banner carousel Home (3 item terpopuler)
    val bannerComics get() = listOf(comicList[0], comicList[6], comicList[4])
}
