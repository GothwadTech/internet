package com.gothwad.internet.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        BookmarkEntity::class,
        HistoryEntity::class,
        QuickAccessEntity::class,
        PreferenceEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BrowserDatabase : RoomDatabase() {
    abstract fun browserDao(): BrowserDao

    companion object {
        @Volatile
        private var INSTANCE: BrowserDatabase? = null

        fun getDatabase(context: Context): BrowserDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BrowserDatabase::class.java,
                    "browser_database"
                )
                    .addCallback(DatabaseCallback(context.applicationContext))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val context: Context
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // Prepopulate some classic quick access items when the database is first created
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    val dao = database.browserDao()
                    val defaults = listOf(
                        QuickAccessEntity(
                            id = "qa_google",
                            order = 0,
                            type = 0,
                            parent = "root",
                            title = "Google",
                            iconUri = "https://www.google.com/favicon.ico",
                            extraJson = """{"default_mark":"G", "background":"#4285F4", "url":"https://www.google.com"}"""
                        ),
                        QuickAccessEntity(
                            id = "qa_gmail",
                            order = 1,
                            type = 0,
                            parent = "root",
                            title = "Gmail",
                            iconUri = "https://ssl.gstatic.com/ui/v1/icons/mail/images/favicon_v2.ico",
                            extraJson = """{"default_mark":"M", "background":"#EA4335", "url":"https://mail.google.com"}"""
                        ),
                        QuickAccessEntity(
                            id = "qa_github",
                            order = 2,
                            type = 0,
                            parent = "root",
                            title = "GitHub",
                            iconUri = "https://github.com/favicon.ico",
                            extraJson = """{"default_mark":"Git", "background":"#181717", "url":"https://github.com"}"""
                        ),
                        QuickAccessEntity(
                            id = "qa_youtube",
                            order = 3,
                            type = 0,
                            parent = "root",
                            title = "YouTube",
                            iconUri = "https://www.youtube.com/favicon.ico",
                            extraJson = """{"default_mark":"Y", "background":"#FF0000", "url":"https://www.youtube.com"}"""
                        ),
                        QuickAccessEntity(
                            id = "qa_drive",
                            order = 4,
                            type = 0,
                            parent = "root",
                            title = "Drive",
                            iconUri = "https://ssl.gstatic.com/images/branding/product/1x/drive_2020q4_32dp.png",
                            extraJson = """{"default_mark":"D", "background":"#34A853", "url":"https://drive.google.com"}"""
                        ),
                        QuickAccessEntity(
                            id = "qa_notes",
                            order = 5,
                            type = 0,
                            parent = "root",
                            title = "Notes",
                            iconUri = "https://ssl.gstatic.com/keep/keep_2020q4_v2_32dp.png",
                            extraJson = """{"default_mark":"N", "background":"#FBBC05", "url":"https://keep.google.com"}"""
                        ),
                        QuickAccessEntity(
                            id = "qa_wikipedia",
                            order = 6,
                            type = 0,
                            parent = "root",
                            title = "Wikipedia",
                            iconUri = "https://en.wikipedia.org/favicon.ico",
                            extraJson = """{"default_mark":"W", "background":"#000000", "url":"https://wikipedia.org"}"""
                        )
                    )
                    dao.insertQuickAccessList(defaults)
                }
            }
        }
    }
}
