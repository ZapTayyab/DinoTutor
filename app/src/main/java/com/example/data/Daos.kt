package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM child_profiles WHERE id = :id LIMIT 1")
    fun getProfileFlow(id: String = "default_child"): Flow<ChildProfile?>

    @Query("SELECT * FROM child_profiles WHERE id = :id LIMIT 1")
    suspend fun getProfile(id: String = "default_child"): ChildProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(profile: ChildProfile)
}

@Dao
interface BadgeDao {
    @Query("SELECT * FROM unlocked_badges ORDER BY unlockedAt DESC")
    fun getUnlockedBadgesFlow(): Flow<List<UnlockedBadge>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun unlockBadge(badge: UnlockedBadge)

    @Query("SELECT EXISTS(SELECT 1 FROM unlocked_badges WHERE id = :badgeId)")
    suspend fun isBadgeUnlocked(badgeId: String): Boolean
}

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC")
    fun getAllActivityFlow(): Flow<List<ActivityLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ActivityLog)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getChatHistoryFlow(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)

    @Query("DELETE FROM chat_messages")
    suspend fun clearChat()
}
