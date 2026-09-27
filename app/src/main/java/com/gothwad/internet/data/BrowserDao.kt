package com.gothwad.internet.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BrowserDao {

    // Bookmarks
    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmark(id: Int)

    @Query("DELETE FROM bookmarks WHERE url = :url")
    suspend fun deleteBookmarkByUrl(url: String)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE url = :url LIMIT 1)")
    suspend fun isBookmarked(url: String): Boolean

    // History
    @Query("SELECT * FROM history ORDER BY timestamp DESC LIMIT 200")
    fun getAllHistory(): Flow<List<HistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: HistoryEntity)

    @Query("DELETE FROM history WHERE id = :id")
    suspend fun deleteHistory(id: Int)

    @Query("DELETE FROM history")
    suspend fun clearHistory()

    // Quick Access items
    @Query("SELECT * FROM quick_access ORDER BY `order` ASC")
    fun getAllQuickAccessFlow(): Flow<List<QuickAccessEntity>>

    @Query("SELECT * FROM quick_access ORDER BY `order` ASC")
    suspend fun getAllQuickAccessList(): List<QuickAccessEntity>

    @Query("SELECT * FROM quick_access WHERE parent = :parentId ORDER BY `order` ASC")
    suspend fun getQuickAccessByParent(parentId: String): List<QuickAccessEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuickAccess(item: QuickAccessEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuickAccessList(items: List<QuickAccessEntity>)

    @Query("DELETE FROM quick_access WHERE id = :id")
    suspend fun deleteQuickAccess(id: String)

    // Preferences
    @Query("SELECT value FROM preferences WHERE `key` = :key LIMIT 1")
    suspend fun getPreference(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreference(pref: PreferenceEntity)

    @Query("DELETE FROM preferences WHERE `key` = :key")
    suspend fun deletePreference(key: String)
}
