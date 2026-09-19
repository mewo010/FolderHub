package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.HubEntity
import com.example.data.model.HubWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface HubDao {
    @Query("SELECT * FROM hubs ORDER BY displayOrder ASC, id ASC")
    fun getAllHubs(): Flow<List<HubEntity>>

    @Transaction
    @Query("SELECT * FROM hubs ORDER BY displayOrder ASC, id ASC")
    fun getAllHubsWithItems(): Flow<List<HubWithItems>>

    @Transaction
    @Query("SELECT * FROM hubs WHERE id = :hubId")
    fun getHubWithItems(hubId: Long): Flow<HubWithItems?>

    @Query("SELECT COUNT(*) FROM hubs")
    suspend fun getHubCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHub(hub: HubEntity): Long

    @Update
    suspend fun updateHub(hub: HubEntity)

    @Delete
    suspend fun deleteHub(hub: HubEntity)

    @Query("DELETE FROM hubs WHERE id = :hubId")
    suspend fun deleteHubById(hubId: Long)
}
