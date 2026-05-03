package com.kelompok1.komiku.database

import android.content.Context
import com.kelompok1.komiku.data.DummyData
import com.kelompok1.komiku.model.Library
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DatabaseSeeder {
    suspend fun seedDatabase(context: Context) {
        val database = KomiKuDatabase.getDatabase(context)
        val comicDao = database.comicDao()
        val chapterDao = database.chapterDao()

        withContext(Dispatchers.IO) {
            // Check if database is empty
            val currentComics = comicDao.getComicById(1)
            if (currentComics == null) {
                // Insert comics
                comicDao.insertComics(DummyData.comicList)
                
                // Insert chapters
                chapterDao.insertChapters(DummyData.chapterList)
                
                // Insert library dummy data
                DummyData.libraryComics.forEach { libItem ->
                    comicDao.addToLibrary(Library(
                        comicId = libItem.comic.id,
                        currentChapter = libItem.currentChapter,
                        totalChapter = libItem.totalChapter
                    ))
                }
            }
        }
    }
}
