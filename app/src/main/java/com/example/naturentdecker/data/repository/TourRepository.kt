package com.example.naturentdecker.data

import com.example.naturentdecker.data.local.NaturEntdeckerDatabase
import com.example.naturentdecker.data.local.toDomain
import com.example.naturentdecker.data.local.toEntity
import com.example.naturentdecker.data.model.Contact
import com.example.naturentdecker.data.model.Tour
import com.example.naturentdecker.data.remote.api.ToursApiService
import com.example.naturentdecker.utils.safeApiCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import com.example.naturentdecker.utils.Result

private const val CACHE_EXPIRY_MS = 5 * 60 * 1000L // 5 minutes

@Singleton
class TourRepository @Inject constructor(
    private val api: ToursApiService,
    db: NaturEntdeckerDatabase,
) {
    private val dao = db.tourDao()

    fun getAllToursFlow(): Flow<List<Tour>> =
        dao.getAllTours().map { entities -> entities.map { it.toDomain() } }

    fun getTop5ToursFlow(): Flow<List<Tour>> =
        dao.getTop5Tours().map { entities -> entities.map { it.toDomain() } }

    fun getTourDetailFlow(id: Int): Flow<Tour?> =
        dao.getTourById(id).map { it?.toDomain() }

    suspend fun refreshAllTours(): Result<Unit> {
        Timber.d("Refreshing all tours from network")
        return safeApiCall {
            val tours = api.getAllTours()
            dao.clearAllTours()
            dao.upsertTours(tours.map { it.toEntity(isTop5 = false) })
            Timber.d("Cached ${tours.size} tours")
        }
    }

    suspend fun refreshTop5Tours(): Result<Unit> {
        Timber.d("Refreshing top5 tours from network")
        return safeApiCall {
            val tours = api.getTop5Tours()
            dao.clearTop5Tours()
            dao.upsertTours(tours.map { it.toEntity(isTop5 = true) })
        }
    }

    suspend fun refreshTourDetail(id: Int): Result<Unit> {
        return safeApiCall {
            val tour = api.getTourDetail(id)
            dao.upsertTour(tour.toEntity(isTop5 = false))
            Timber.d("Cached tour detail: ${tour.title}")
        }
    }

    suspend fun getContact(): Result<Contact> = safeApiCall { api.getContact() }

    suspend fun isCacheStale(): Boolean {
        val lastCache = dao.getLastCacheTime() ?: return true
        return System.currentTimeMillis() - lastCache > CACHE_EXPIRY_MS
    }
}
