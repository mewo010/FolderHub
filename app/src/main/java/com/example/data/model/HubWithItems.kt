package com.example.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class HubWithItems(
    @Embedded
    val hub: HubEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "hubId"
    )
    val items: List<HubItemEntity> = emptyList()
)
