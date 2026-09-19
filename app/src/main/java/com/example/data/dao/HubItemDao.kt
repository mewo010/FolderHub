package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.HubItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HubItemDao {
    @Query("SELECT * FROM hub_items WHERE hubId = :hubId ORDER BY displayOrder ASC, id ASC")
    fun getItemsForHub(hubId: Long): Flow<List<HubItemEntity>>

    @Query("SELECT * FROM hub_items ORDER BY createdAt DESC")
    fun getAllItems(): Flow<List<HubItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: HubItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<HubItemEntity>): List<Long>

    @Update
    suspend fun updateItem(item: HubItemEntity)

    @Delete
    suspend fun deleteItem(item: HubItemEntity)

    @Query("DELETE FROM hub_items WHERE id = :itemId")
    suspend fun deleteItemById(itemId: Long)

    @Query("DELETE FROM hub_items WHERE hubId = :hubId")
    suspend fun deleteItemsByHubId(hubId: Long)
}
