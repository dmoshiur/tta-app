package com.example.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val id: String,
    val type: String, // "COURSE", "ARTICLE", "BOOK", "KNOWLEDGE", "WORLD", "HUMANITY", "SOCIETY"
    val title: String,
    val subtitle: String, // author / category
    val imageUrl: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val message: String,
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "offline_cache")
data class OfflineCacheEntity(
    @PrimaryKey val cacheKey: String,
    val jsonContent: String,
    val timestamp: Long = System.currentTimeMillis()
)
