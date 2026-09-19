package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hubs")
data class HubEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val iconKey: String = "folder",
    val colorHex: String = "#4F46E5",
    val displayOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
