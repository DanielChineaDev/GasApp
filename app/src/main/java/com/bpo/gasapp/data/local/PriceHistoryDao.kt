package com.bpo.gasapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bpo.gasapp.data.local.entity.PriceHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PriceHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<PriceHistoryEntity>)

    @Query("SELECT * FROM price_history WHERE stationId = :stationId ORDER BY timestamp ASC")
    fun observeForStation(stationId: String): Flow<List<PriceHistoryEntity>>

    @Query("DELETE FROM price_history WHERE timestamp < :before")
    suspend fun pruneOlderThan(before: Long)
}
