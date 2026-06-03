package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "child_profiles")
data class ChildProfile(
    @PrimaryKey val id: String = "default_child",
    val name: String = "Little Explorer",
    val avatar: String = "dino", // dino, rocket, kitten, wizard
    val avatarColor: String = "#FF5722", // HEX representing selected theme
    val xp: Int = 0,
    val stars: Int = 0,
    val level: Int = 1,
    val streak: Int = 1,
    val lastActiveTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "unlocked_badges")
data class UnlockedBadge(
    @PrimaryKey val id: String, // math_whiz, spelling_bee, science_guru, space_cadet, curiousity_spark
    val title: String,
    val description: String,
    val iconName: String,
    val unlockedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "activity_logs")
data class ActivityLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // math, word, science, ai_custom
    val score: Int,
    val maxScore: Int,
    val starsEarned: Int,
    val xpEarned: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String, // child, ai
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
