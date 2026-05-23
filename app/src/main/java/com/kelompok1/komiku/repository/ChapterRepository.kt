package com.kelompok1.komiku.repository

import com.kelompok1.komiku.database.ChapterDao
import com.kelompok1.komiku.model.Chapter
import kotlinx.coroutines.flow.Flow

class ChapterRepository(private val chapterDao: ChapterDao) {
    fun getChaptersByComicId(comicId: Int): Flow<List<Chapter>> = 
        chapterDao.getChaptersByComicId(comicId)
        
    suspend fun getChapterById(id: Int): Chapter? = 
        chapterDao.getChapterById(id)

    suspend fun deleteChapter(chapter: Chapter) =
        chapterDao.deleteChapter(chapter)
}
