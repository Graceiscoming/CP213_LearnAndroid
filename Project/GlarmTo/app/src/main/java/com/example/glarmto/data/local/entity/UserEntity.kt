package com.example.glarmto.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_log")
data class UserEntity(
    @PrimaryKey val username: String,
    val createdAt: Long = System.currentTimeMillis(),
    val age: Int = 0,
    val isMale: Boolean = true,
    val weight: Double = 0.0,
    val height: Double = 0.0,
    val dailyGoal: Int = 2500,
    val profileSetup: Boolean = false
)
