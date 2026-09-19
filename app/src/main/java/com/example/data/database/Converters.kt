package com.example.data.database

import androidx.room.TypeConverter
import com.example.data.model.HubItemType

class Converters {
    @TypeConverter
    fun fromHubItemType(type: HubItemType): String = type.name

    @TypeConverter
    fun toHubItemType(value: String): HubItemType {
        return try {
            HubItemType.valueOf(value)
        } catch (e: Exception) {
            HubItemType.APP
        }
    }
}
