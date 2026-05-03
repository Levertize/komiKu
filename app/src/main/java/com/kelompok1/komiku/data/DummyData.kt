package com.kelompok1.komiku.data

import android.graphics.Color
import com.kelompok1.komiku.model.Chapter
import com.kelompok1.komiku.model.Comic

object DummyData {

    private fun getTimestamp(offsetDays: Int): String {
        return (System.currentTimeMillis() - (offsetDays * 24 * 60 * 60 * 1000L)).toString()
    }

    // ─────────────────────────────────────────────────────────
    //  COMIC LIST
    // ─────────────────────────────────────────────────────────
    val comicList: List<Comic> = listOf(
        Comic(
            id = 1, title = "One Piece", author = "Eiichiro Oda",
            chapter = "Ch. 1111", format = "Manga",
            genre = listOf("Action", "Adventure", "Fantasy"),
            badge = "HOT", rating = 9.2f, views = "12.4M", lastUpdate = getTimestamp(0),
            coverColorStart = Color.parseColor("#1A0E35"),
            coverColorEnd = Color.parseColor("#0D1840"),
            status = "Ongoing",
            description = "Monkey D. Luffy adalah seorang pemuda yang bermimpi menjadi Raja Bajak Laut..."
        ),
        Comic(
            id = 2, title = "Naruto Shippuden", author = "Masashi Kishimoto",
            chapter = "Ch. 97", format = "Manga",
            genre = listOf("Action", "Adventure", "Ninja"),
            badge = "UPDATE", rating = 8.9f, views = "9.8M", lastUpdate = getTimestamp(1),
            coverColorStart = Color.parseColor("#0A1A06"),
            coverColorEnd = Color.parseColor("#182A0A"),
            status = "Completed",
            description = "Naruto Uzumaki, ninja remaja dari Desa Konoha..."
        ),
        Comic(
            id = 3, title = "Jujutsu Kaisen", author = "Gege Akutami",
            chapter = "Ch. 264", format = "Manga",
            genre = listOf("Action", "Horror", "Supernatural"),
            badge = "HOT", rating = 9.0f, views = "8.1M", lastUpdate = getTimestamp(3),
            coverColorStart = Color.parseColor("#1A0808"),
            coverColorEnd = Color.parseColor("#2A0A14"),
            status = "Ongoing",
            description = "Yuji Itadori menelan jari Ryomen Sukuna..."
        ),
        Comic(
            id = 4, title = "Demon Slayer", author = "Koyoharu Gotouge",
            chapter = "Ch. 205", format = "Manga",
            genre = listOf("Action", "Fantasy", "Historical"),
            badge = "NEW", rating = 9.1f, views = "7.5M", lastUpdate = getTimestamp(5),
            coverColorStart = Color.parseColor("#060610"),
            coverColorEnd = Color.parseColor("#10102A"),
            status = "Completed",
            description = "Tanjiro Kamado hidup damai bersama keluarganya..."
        ),
        Comic(
            id = 5, title = "Attack on Titan", author = "Hajime Isayama",
            chapter = "Ch. 139", format = "Manga",
            genre = listOf("Action", "Drama", "Post-apocalyptic"),
            badge = "", rating = 9.5f, views = "11.2M", lastUpdate = getTimestamp(7),
            coverColorStart = Color.parseColor("#081408"),
            coverColorEnd = Color.parseColor("#0A1A0A"),
            status = "Completed",
            description = "Manusia bertahan hidup di balik tembok raksasa..."
        ),
        Comic(
            id = 6, title = "Bleach", author = "Tite Kubo",
            chapter = "Ch. 686", format = "Manga",
            genre = listOf("Action", "Supernatural", "Adventure"),
            badge = "NEW", rating = 8.7f, views = "6.5M", lastUpdate = getTimestamp(7),
            coverColorStart = Color.parseColor("#180A00"),
            coverColorEnd = Color.parseColor("#241200"),
            status = "Completed",
            description = "Ichigo Kurosaki, remaja dengan kemampuan melihat roh..."
        ),
        Comic(
            id = 7, title = "Solo Leveling", author = "Chugong",
            chapter = "Ch. 179", format = "Manhwa",
            genre = listOf("Action", "Fantasy", "Adventure"),
            badge = "HOT", rating = 9.4f, views = "7.3M", lastUpdate = getTimestamp(5),
            coverColorStart = Color.parseColor("#182A20"),
            coverColorEnd = Color.parseColor("#0A2018"),
            status = "Completed",
            description = "Sung Jinwoo adalah hunter paling lemah di dunia..."
        ),
        Comic(
            id = 8, title = "Tower of God", author = "SIU",
            chapter = "Ch. 550", format = "Manhwa",
            genre = listOf("Action", "Fantasy", "Adventure"),
            badge = "UPDATE", rating = 8.8f, views = "5.9M", lastUpdate = getTimestamp(3),
            coverColorStart = Color.parseColor("#0A0820"),
            coverColorEnd = Color.parseColor("#14103A"),
            status = "Ongoing",
            description = "Bam adalah anak muda yang telah hidup seumur hidupnya..."
        ),
        Comic(
            id = 9, title = "Vinland Saga", author = "Makoto Yukimura",
            chapter = "Ch. 208", format = "Manga",
            genre = listOf("Action", "Historical", "Drama"),
            badge = "", rating = 9.3f, views = "4.2M", lastUpdate = getTimestamp(14),
            coverColorStart = Color.parseColor("#201008"),
            coverColorEnd = Color.parseColor("#301808"),
            status = "Ongoing",
            description = "Thorfinn, putra seorang Viking legendaris..."
        ),
        Comic(
            id = 10, title = "Dragon Ball Super", author = "Akira Toriyama",
            chapter = "Ch. 101", format = "Manga",
            genre = listOf("Action", "Adventure", "Sci-fi"),
            badge = "UPDATE", rating = 8.5f, views = "8.9M", lastUpdate = getTimestamp(1),
            coverColorStart = Color.parseColor("#1A1000"),
            coverColorEnd = Color.parseColor("#2A1C00"),
            status = "Ongoing",
            description = "Setelah mengalahkan Majin Buu, Goku dan kawan-kawan..."
        ),
        Comic(
            id = 11, title = "My Hero Academia", author = "Kohei Horikoshi",
            chapter = "Ch. 430", format = "Manga",
            genre = listOf("Action", "Superhero", "School"),
            badge = "HOT", rating = 8.6f, views = "6.8M", lastUpdate = getTimestamp(0),
            coverColorStart = Color.parseColor("#001A0A"),
            coverColorEnd = Color.parseColor("#002814"),
            status = "Ongoing",
            description = "Di dunia di mana 80% populasi memiliki kekuatan super..."
        ),
        Comic(
            id = 12, title = "Chainsaw Man", author = "Tatsuki Fujimoto",
            chapter = "Ch. 168", format = "Manga",
            genre = listOf("Action", "Horror", "Dark Fantasy"),
            badge = "NEW", rating = 9.1f, views = "5.4M", lastUpdate = getTimestamp(4),
            coverColorStart = Color.parseColor("#200000"),
            coverColorEnd = Color.parseColor("#300808"),
            status = "Ongoing",
            description = "Denji hidup dalam kemiskinan ekstrem..."
        )
    )

    // ─────────────────────────────────────────────────────────
    //  CHAPTER LIST
    // ─────────────────────────────────────────────────────────
    val chapterList: List<Chapter> = listOf(
        // One Piece (comicId = 1)
        Chapter(1, 1, 1111, "Chapter 1111 - Elbaf", uploadDate = "2 jam lalu"),
        Chapter(2, 1, 1110, "Chapter 1110 - The Giant Alliance", uploadDate = "1 mgg lalu"),
        Chapter(3, 1, 1109, "Chapter 1109 - Countdown", uploadDate = "2 mgg lalu"),
        Chapter(4, 1, 1108, "Chapter 1108 - Awakening", uploadDate = "3 mgg lalu"),
        Chapter(5, 1, 1107, "Chapter 1107 - Gear 5 Unleashed", uploadDate = "1 bln lalu"),

        // Naruto Shippuden (comicId = 2)
        Chapter(6,  2, 97,  "Chapter 97 - The Chronicle Karma", uploadDate = "1 hari lalu"),
        Chapter(7,  2, 96,  "Chapter 96 - Sasuke's Choice", uploadDate = "1 mgg lalu"),
        Chapter(8,  2, 95,  "Chapter 95 - Battle at the Valley", uploadDate = "2 mgg lalu"),
        Chapter(9,  2, 94,  "Chapter 94 - Reunion", uploadDate = "3 mgg lalu"),
        Chapter(10, 2, 93,  "Chapter 93 - The Hidden Truth", uploadDate = "1 bln lalu"),

        // Jujutsu Kaisen (comicId = 3)
        Chapter(11, 3, 264, "Chapter 264 - Star and Oil", uploadDate = "3 hari lalu"),
        Chapter(12, 3, 263, "Chapter 263 - Inhuman Makings", uploadDate = "1 mgg lalu"),
        Chapter(13, 3, 262, "Chapter 262 - The Decisive Battle", uploadDate = "2 mgg lalu"),
        Chapter(14, 3, 261, "Chapter 261 - Iron Hammer", uploadDate = "3 mgg lalu"),
        Chapter(15, 3, 260, "Chapter 260 - New Vessel", uploadDate = "1 bln lalu"),

        // Demon Slayer (comicId = 4)
        Chapter(16, 4, 205, "Chapter 205 - Epilogue", uploadDate = "5 hari lalu"),
        Chapter(17, 4, 204, "Chapter 204 - The End of Infinity", uploadDate = "2 mgg lalu"),
        Chapter(18, 4, 203, "Chapter 203 - Life Shining Across the Years", uploadDate = "3 mgg lalu"),

        // Attack on Titan (comicId = 5)
        Chapter(19, 5, 139, "Chapter 139 - Toward the Tree on the Hill", uploadDate = "1 mgg lalu"),
        Chapter(20, 5, 138, "Chapter 138 - A Long Dream", uploadDate = "2 mgg lalu"),
        Chapter(21, 5, 137, "Chapter 137 - Titans", uploadDate = "3 mgg lalu"),

        // Solo Leveling (comicId = 7)
        Chapter(22, 7, 179, "Chapter 179 - The End and the Beginning", uploadDate = "5 hari lalu"),
        Chapter(23, 7, 178, "Chapter 178 - Ashborn's Legacy", uploadDate = "2 mgg lalu"),
        Chapter(24, 7, 177, "Chapter 177 - The Shadow Monarch", uploadDate = "3 mgg lalu"),
        Chapter(25, 7, 176, "Chapter 176 - Arise", uploadDate = "1 bln lalu"),

        // Dragon Ball Super (comicId = 10)
        Chapter(26, 10, 101, "Chapter 101 - The New Threat", uploadDate = "1 hari lalu"),
        Chapter(27, 10, 100, "Chapter 100 - Anniversary", uploadDate = "1 mgg lalu"),
        Chapter(28, 10, 99,  "Chapter 99 - Ultra Ego", uploadDate = "2 mgg lalu"),

        // My Hero Academia (comicId = 11)
        Chapter(29, 11, 430, "Chapter 430 - Finale", uploadDate = "2 jam lalu"),
        Chapter(30, 11, 429, "Chapter 429 - Last Battle", uploadDate = "1 mgg lalu"),
        Chapter(31, 11, 428, "Chapter 428 - Symbol of Peace", uploadDate = "2 mgg lalu"),

        // Chainsaw Man (comicId = 12)
        Chapter(32, 12, 168, "Chapter 168 - My Battle", uploadDate = "4 hari lalu"),
        Chapter(33, 12, 167, "Chapter 167 - Guns and Chainsaws", uploadDate = "1 mgg lalu"),
        Chapter(34, 12, 166, "Chapter 166 - Darkness Devil", uploadDate = "2 mgg lalu")
    )

    data class LibraryComic(
        val comic: Comic,
        val currentChapter: Int,
        val totalChapter: Int
    )

    val libraryComics = listOf(
        LibraryComic(comicList[0], 778, 1111),
        LibraryComic(comicList[1], 87,  97),
        LibraryComic(comicList[2], 79,  264),
        LibraryComic(comicList[3], 92,  205),
        LibraryComic(comicList[6], 150, 179),
        LibraryComic(comicList[4], 100, 139)
    )
}
