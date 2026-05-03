package com.kelompok1.komiku.database

import androidx.room.*
import com.kelompok1.komiku.model.Comic
import com.kelompok1.komiku.model.Library
import com.kelompok1.komiku.model.LibraryComicJoin
import kotlinx.coroutines.flow.Flow

@Dao
interface ComicDao {
    @Query("SELECT * FROM comics ORDER BY last_update DESC, created_at DESC")
    fun getAllComics(): Flow<List<Comic>>

    @Query("SELECT * FROM comics WHERE id = :id")
    suspend fun getComicById(id: Int): Comic?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComics(comics: List<Comic>)

    @Update
    suspend fun updateComic(comic: Comic)

    @Delete
    suspend fun deleteComic(comic: Comic)

    @Query("SELECT * FROM comics WHERE title LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%'")
    fun searchComics(query: String): Flow<List<Comic>>

    // Library related
    @Transaction
    @Query("""
        SELECT comics.*, 
               library.id AS lib_id, 
               library.comic_id AS lib_comic_id, 
               library.current_chapter AS lib_current_chapter, 
               library.total_chapter AS lib_total_chapter, 
               library.saved_at AS lib_saved_at 
        FROM comics 
        INNER JOIN library ON comics.id = library.comic_id 
        ORDER BY library.saved_at DESC
    """)
    fun getLibraryComics(): Flow<List<LibraryComicJoin>>
    
    @Query("SELECT * FROM library WHERE comic_id = :comicId")
    suspend fun getLibraryEntry(comicId: Int): Library?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToLibrary(library: Library)

    @Delete
    suspend fun removeFromLibrary(library: Library)
}
