package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar

class LearningRepository(private val db: AppDatabase) {

    val profileFlow: Flow<ChildProfile?> = db.profileDao().getProfileFlow()
    val unlockedBadgesFlow: Flow<List<UnlockedBadge>> = db.badgeDao().getUnlockedBadgesFlow()
    val activityLogsFlow: Flow<List<ActivityLog>> = db.activityDao().getAllActivityFlow()
    val chatHistoryFlow: Flow<List<ChatMessage>> = db.chatDao().getChatHistoryFlow()

    suspend fun getOrCreateProfile(): ChildProfile {
        val existing = db.profileDao().getProfile()
        if (existing != null) {
            // Update streak if needed
            val updated = checkAndUpdateStreak(existing)
            db.profileDao().insertOrUpdate(updated)
            return updated
        }

        val newProfile = ChildProfile(
            id = "default_child",
            name = "Space Explorer",
            avatar = "dino",
            avatarColor = "#4CAF50", // Lime friendly green
            xp = 0,
            stars = 0,
            level = 1,
            streak = 1,
            lastActiveTimestamp = System.currentTimeMillis()
        )
        db.profileDao().insertOrUpdate(newProfile)
        return newProfile
    }

    suspend fun updateProfileNameAndAvatar(name: String, avatar: String, color: String) {
        val profile = getOrCreateProfile()
        val updated = profile.copy(name = name, avatar = avatar, avatarColor = color)
        db.profileDao().insertOrUpdate(updated)
    }

    suspend fun addXpandStars(xpEarned: Int, starsEarned: Int, activityType: String, score: Int, maxScore: Int) {
        val profile = getOrCreateProfile()
        val newXp = profile.xp + xpEarned
        val newStars = profile.stars + starsEarned
        
        // Let's say level up is every 100 XP
        val nextLevelThreshold = profile.level * 100
        val newLevel = if (newXp >= nextLevelThreshold) {
            profile.level + 1
        } else {
            profile.level
        }

        val updatedProfile = profile.copy(
            xp = newXp,
            stars = newStars,
            level = newLevel,
            lastActiveTimestamp = System.currentTimeMillis()
        )
        db.profileDao().insertOrUpdate(updatedProfile)

        // Log the activity
        val log = ActivityLog(
            type = activityType,
            score = score,
            maxScore = maxScore,
            starsEarned = starsEarned,
            xpEarned = xpEarned
        )
        db.activityDao().insertLog(log)

        // Check and unlock badges
        checkAndUnlockBadges(activityType, score, maxScore, newLevel)
    }

    suspend fun addChatMessage(sender: String, messageText: String) {
        val msg = ChatMessage(sender = sender, text = messageText)
        db.chatDao().insertMessage(msg)
        
        // If this is the child's first question, unlock Curiousity badge!
        if (sender == "child") {
            val isUnlocked = db.badgeDao().isBadgeUnlocked("badge_curious")
            if (!isUnlocked) {
                db.badgeDao().unlockBadge(
                    UnlockedBadge(
                        id = "badge_curious",
                        title = "Curious Explorer",
                        description = "Asked your very first questions to Sparky the Dino!",
                        iconName = "emoji_nature"
                    )
                )
            }
        }
    }

    suspend fun clearChatHistory() {
        db.chatDao().clearChat()
    }

    private suspend fun checkAndUnlockBadges(activityType: String, score: Int, maxScore: Int, currentLevel: Int) {
        // Unlock Level badge
        if (currentLevel >= 2) {
            unlockBadgeIfNeeded(
                "badge_lvl2",
                "Rising Comet",
                "Reached level 2! Keep climbing the universe!",
                "stars"
            )
        }
        if (currentLevel >= 5) {
            unlockBadgeIfNeeded(
                "badge_lvl5",
                "Galaxy Brain",
                "Reached level 5! You are absolutely stellar!",
                "school"
            )
        }

        // Challenge badges
        when (activityType) {
            "math" -> {
                unlockBadgeIfNeeded(
                    "badge_math_first",
                    "Math Rookie",
                    "Completed your first Math Cosmos adventure!",
                    "calculate"
                )
                if (score == maxScore && maxScore > 0) {
                    unlockBadgeIfNeeded(
                        "badge_math_perfect",
                        "Math Wizard",
                        "Got a perfect score in Math Cosmos!",
                        "insights"
                    )
                }
            }
            "word" -> {
                unlockBadgeIfNeeded(
                    "badge_word_first",
                    "Word Scout",
                    "Spelled your first words in Alphabet Camp!",
                    "spellcheck"
                )
                if (score == maxScore && maxScore > 0) {
                    unlockBadgeIfNeeded(
                        "badge_word_perfect",
                        "Vocabulary Master",
                        "Spelled all words flawlessly in Word Safari!",
                        "emoji_events"
                    )
                }
            }
            "science" -> {
                unlockBadgeIfNeeded(
                    "badge_science_first",
                    "Star Voyager",
                    "Finished your first Space Science trivia quiz!",
                    "rocket_launch"
                )
            }
        }
    }

    private suspend fun unlockBadgeIfNeeded(id: String, title: String, desc: String, icon: String) {
        if (!db.badgeDao().isBadgeUnlocked(id)) {
            db.badgeDao().unlockBadge(
                UnlockedBadge(id = id, title = title, description = desc, iconName = icon)
            )
        }
    }

    private fun checkAndUpdateStreak(profile: ChildProfile): ChildProfile {
        val lastActiveCalendar = Calendar.getInstance().apply {
            timeInMillis = profile.lastActiveTimestamp
        }
        val currentCalendar = Calendar.getInstance()

        val isSameDay = lastActiveCalendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR) &&
                lastActiveCalendar.get(Calendar.DAY_OF_YEAR) == currentCalendar.get(Calendar.DAY_OF_YEAR)

        if (isSameDay) return profile

        // Check if yesterday
        val yesterday = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }
        val isYesterday = lastActiveCalendar.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                lastActiveCalendar.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR)

        val newStreak = if (isYesterday) profile.streak + 1 else 1

        return profile.copy(
            streak = newStreak,
            lastActiveTimestamp = System.currentTimeMillis()
        )
    }
}
