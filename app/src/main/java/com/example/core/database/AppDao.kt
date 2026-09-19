package com.example.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // Bookmarks
    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmarkById(id: String)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE id = :id)")
    fun isBookmarked(id: String): Flow<Boolean>

    // Notifications
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationAsRead(id: String)

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>

    // Offline Generic JSON Cache
    @Query("SELECT * FROM offline_cache WHERE cacheKey = :key")
    suspend fun getCacheByKey(key: String): OfflineCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCache(cache: OfflineCacheEntity)

    @Query("DELETE FROM offline_cache WHERE cacheKey = :key")
    suspend fun clearCacheByKey(key: String)

    @Query("DELETE FROM offline_cache")
    suspend fun clearAllCache()
}
