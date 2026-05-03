package com.kelompok1.komiku.repository

import com.kelompok1.komiku.database.ComicDao
import com.kelompok1.komiku.model.Comic
import com.kelompok1.komiku.model.Library
import com.kelompok1.komiku.model.LibraryComicJoin
import kotlinx.coroutines.flow.Flow

class ComicRepository(private val comicDao: ComicDao) {
    fun getAllComics(): Flow<List<Comic>> = comicDao.getAllComics()
    
    suspend fun getComicById(id: Int): Comic? = comicDao.getComicById(id)
    
    fun searchComics(query: String): Flow<List<Comic>> = comicDao.searchComics(query)
    
    fun getLibraryComics(): Flow<List<LibraryComicJoin>> = comicDao.getLibraryComics()
    
    suspend fun getLibraryEntry(comicId: Int): Library? = comicDao.getLibraryEntry(comicId)
    
    suspend fun addToLibrary(library: Library) = comicDao.addToLibrary(library)
    
    suspend fun removeFromLibrary(library: Library) = comicDao.removeFromLibrary(library)
}
