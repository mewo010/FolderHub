package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "hub_items",
    foreignKeys = [
        ForeignKey(
            entity = HubEntity::class,
            parentColumns = ["id"],
            childColumns = ["hubId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["hubId"])]
)
data class HubItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hubId: Long,
    val title: String,
    val type: HubItemType,
    val dataUri: String, // Package name for APP, URL for WEB_LINK, File Uri string for LOCAL_FILE
    val secondaryInfo: String = "", // e.g. Domain, file size/mime, or package details
    val iconIdentifier: String = "",
    val displayOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
