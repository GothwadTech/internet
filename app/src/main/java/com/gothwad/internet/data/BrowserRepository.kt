package com.gothwad.internet.data

import kotlinx.coroutines.flow.Flow

class BrowserRepository(private val browserDao: BrowserDao) {

    // Bookmarks
    val allBookmarks: Flow<List<BookmarkEntity>> = browserDao.getAllBookmarks()

    suspend fun addBookmark(title: String, url: String) {
        browserDao.insertBookmark(BookmarkEntity(title = title, url = url))
    }

    suspend fun removeBookmark(id: Int) {
        browserDao.deleteBookmark(id)
    }

    suspend fun removeBookmarkByUrl(url: String) {
        browserDao.deleteBookmarkByUrl(url)
    }

    suspend fun isBookmarked(url: String): Boolean {
        return browserDao.isBookmarked(url)
    }

    // History
    val allHistory: Flow<List<HistoryEntity>> = browserDao.getAllHistory()

    suspend fun addHistory(title: String, url: String) {
        browserDao.insertHistory(HistoryEntity(title = title, url = url))
    }

    suspend fun removeHistory(id: Int) {
        browserDao.deleteHistory(id)
    }

    suspend fun clearHistory() {
        browserDao.clearHistory()
    }

    // Quick Access items
    val quickAccessFlow: Flow<List<QuickAccessEntity>> = browserDao.getAllQuickAccessFlow()

    suspend fun getQuickAccessList(): List<QuickAccessEntity> {
        return browserDao.getAllQuickAccessList()
    }

    suspend fun getQuickAccessByParent(parentId: String): List<QuickAccessEntity> {
        return browserDao.getQuickAccessByParent(parentId)
    }

    suspend fun addQuickAccess(item: QuickAccessEntity) {
        browserDao.insertQuickAccess(item)
    }

    suspend fun addQuickAccessList(items: List<QuickAccessEntity>) {
        browserDao.insertQuickAccessList(items)
    }

    suspend fun removeQuickAccess(id: String) {
        browserDao.deleteQuickAccess(id)
    }

    // Preferences
    suspend fun getPreference(key: String, defaultValue: String = ""): String {
        return browserDao.getPreference(key) ?: defaultValue
    }

    suspend fun putPreference(key: String, value: String) {
        browserDao.insertPreference(PreferenceEntity(key = key, value = value))
    }

    suspend fun deletePreference(key: String) {
        browserDao.deletePreference(key)
    }
}
