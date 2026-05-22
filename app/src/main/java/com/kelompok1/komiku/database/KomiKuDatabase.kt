package com.kelompok1.komiku.database

import android.content.Context
import androidx.room.*
import com.kelompok1.komiku.model.Chapter
import com.kelompok1.komiku.model.Comic
import com.kelompok1.komiku.model.Library

@Database(entities = [Comic::class, Chapter::class, Library::class], version = 4, exportSchema = false)
@TypeConverters(Converters::class)
abstract class KomiKuDatabase : RoomDatabase() {
    abstract fun comicDao(): ComicDao
    abstract fun chapterDao(): ChapterDao

    companion object {
        @Volatile
        private var INSTANCE: KomiKuDatabase? = null

        fun getDatabase(context: Context): KomiKuDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KomiKuDatabase::class.java,
                    "komiku_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
