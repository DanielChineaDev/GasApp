package com.bpo.gasapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.bpo.gasapp.data.local.entity.StationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StationDao {

    @Query("SELECT * FROM stations")
    fun observeAll(): Flow<List<StationEntity>>

    /**
     * Stations inside a geographic bounding box, capped to [limit] rows.
     * Used by the map to load only what's near the user / visible region
     * instead of the whole country (which exhausts memory and freezes the UI).
     */
    @Query(
        """
        SELECT * FROM stations
        WHERE latitude BETWEEN :minLat AND :maxLat
          AND longitude BETWEEN :minLng AND :maxLng
        LIMIT :limit
        """
    )
    fun observeInBounds(
        minLat: Double,
        maxLat: Double,
        minLng: Double,
        maxLng: Double,
        limit: Int
    ): Flow<List<StationEntity>>

    @Query("SELECT * FROM stations WHERE id = :id")
    suspend fun getById(id: String): StationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(stations: List<StationEntity>)

    @Query("DELETE FROM stations")
    suspend fun clear()

    @Query("SELECT COUNT(*) FROM stations")
    suspend fun count(): Int

    @Transaction
    suspend fun replaceAll(stations: List<StationEntity>) {
        clear()
        upsertAll(stations)
    }
}
