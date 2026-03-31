package com.example.naturentdecker.data.local.dao

import androidx.room.*
import com.example.naturentdecker.data.local.entity.TourEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TourDao {

    @Query("SELECT * FROM tour ORDER BY startDate ASC")
    fun getAllTours(): Flow<List<TourEntity>>

    @Query("SELECT * FROM tour WHERE isTop5 = 1 ORDER BY startDate ASC")
    fun getTop5Tours(): Flow<List<TourEntity>>

    @Query("SELECT * FROM tour WHERE id = :id LIMIT 1")
    fun getTourById(id: Int): Flow<TourEntity?>

    @Upsert
    suspend fun upsertTours(tours: List<TourEntity>)

    @Upsert
    suspend fun upsertTour(tour: TourEntity)

    @Query("DELETE FROM tour WHERE isTop5 = 0")
    suspend fun clearAllTours()

    @Query("DELETE FROM tour WHERE isTop5 = 1")
    suspend fun clearTop5Tours()

    @Query("SELECT cachedAt FROM tour WHERE isTop5 = 0 ORDER BY cachedAt DESC LIMIT 1")
    suspend fun getLastCacheTimeAllTours(): Long?

    @Query("SELECT cachedAt FROM tour WHERE isTop5 = 1 ORDER BY cachedAt DESC LIMIT 1")
    suspend fun getLastCacheTimeTop5Tours(): Long?

    @Transaction
    suspend fun replaceAllTop5Tours(newTours: List<TourEntity>) {
        clearTop5Tours()
        upsertTours(newTours)
    }

    @Transaction
    suspend fun replaceAllTopTours(newTours: List<TourEntity>) {
        clearAllTours()
        upsertTours(newTours)
    }


}