package com.kelompok1.komiku.database

import androidx.room.*
import com.kelompok1.komiku.model.Chapter
import kotlinx.coroutines.flow.Flow

@Dao
interface ChapterDao {
    @Query("SELECT * FROM chapters WHERE comic_id = :comicId ORDER BY number DESC")
    fun getChaptersByComicId(comicId: Int): Flow<List<Chapter>>

    @Query("SELECT * FROM chapters WHERE id = :id")
    suspend fun getChapterById(id: Int): Chapter?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<Chapter>)

    @Delete
    suspend fun deleteChapter(chapter: Chapter)
    
    @Query("DELETE FROM chapters WHERE comic_id = :comicId")
    suspend fun deleteChaptersByComicId(comicId: Int)
}
