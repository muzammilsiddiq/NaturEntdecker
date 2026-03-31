package com.example.naturentdecker.data

import com.example.naturentdecker.data.local.NaturEntdeckerDatabase
import com.example.naturentdecker.data.local.dao.TourDao
import com.example.naturentdecker.data.local.entity.TourEntity
import com.example.naturentdecker.data.model.Tour
import com.example.naturentdecker.data.remote.api.ToursApiService
import com.example.naturentdecker.data.repository.TourRepository
import com.example.naturentdecker.utils.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ToursRepositoryTest {

    private lateinit var api: ToursApiService
    private lateinit var db: NaturEntdeckerDatabase
    private lateinit var dao: TourDao
    private lateinit var repository: TourRepository

    private val apiTours = listOf(
        Tour(
            id = 1, title = "Rhino Tour", price = "10.3",
            startDate = "2026-03-16T01:31:04+00:00", endDate = "2026-04-23T01:31:04+00:00",
            thumb = "https://dummyimage.com/400x200/ff7f7f/333333?text=Rhino",
            shortDescription = "Test short description",
            description = "Test full description"
        ),
        Tour(
            id = 2, title = "Gorilla Tour", price = "20.6",
            startDate = "2026-03-16T01:31:04+00:00", endDate = "2026-04-23T01:31:04+00:00",
            thumb = "https://dummyimage.com/400x200/ff7f7f/333333?text=Gorilla",
            shortDescription = "Test short description",
            description = "Test full description"
        ),
    )

    private val cachedEntities = listOf(
        TourEntity(
            id = 1, title = "Rhino Tour", price = "10.3",
            startDate = "2026-03-16T01:31:04+00:00", endDate = "2026-04-23T01:31:04+00:00",
            shortDescription = null, description = null, thumb = null, isTop5 = false
        ),
        TourEntity(
            id = 2, title = "Gorilla Tour", price = "20.6",
            startDate = "2026-03-16T01:31:04+00:00", endDate = "2026-04-23T01:31:04+00:00",
            shortDescription = null, description = null, thumb = null, isTop5 = false
        ),
    )

    @Before
    fun setup() {
        api = mockk()
        db = mockk()
        dao = mockk(relaxed = true)
        every { db.tourDao() } returns dao
        repository = TourRepository(api, db)
    }

    @Test
    fun `getAllToursFlow emits mapped domain models from cache`() = runTest {
        every { dao.getAllTours() } returns flowOf(cachedEntities)

        val tours = repository.getAllToursFlow().first()

        assertEquals(2, tours.size)
        assertEquals("Rhino Tour", tours[0].title)
        assertEquals("Gorilla Tour", tours[1].title)
    }

    @Test
    fun `getAllToursFlow emits empty list when cache is empty`() = runTest {
        every { dao.getAllTours() } returns flowOf(emptyList())

        val tours = repository.getAllToursFlow().first()

        assertTrue(tours.isEmpty())
    }

    @Test
    fun `getAllToursFlow maps price as String`() = runTest {
        every { dao.getAllTours() } returns flowOf(cachedEntities)

        val tours = repository.getAllToursFlow().first()

        assertEquals("10.3", tours[0].price)
        assertEquals("20.6", tours[1].price)
    }

    @Test
    fun `getAllToursFlow maps dates correctly`() = runTest {
        every { dao.getAllTours() } returns flowOf(cachedEntities)

        val tours = repository.getAllToursFlow().first()

        assertEquals("2026-03-16T01:31:04+00:00", tours[0].startDate)
        assertEquals("2026-04-23T01:31:04+00:00", tours[0].endDate)
    }

    @Test
    fun `refreshAllTours returns Success and writes to cache`() = runTest {
        coEvery { api.getAllTours() } returns apiTours
        coEvery { dao.getLastCacheTime() } returns null

        val result = repository.refreshAllTours()

        assertTrue(result is Result.Success)
        coVerify { dao.replaceAllTopTours(any()) }
    }

    @Test
    fun `refreshAllTours returns Error on network failure`() = runTest {
        coEvery { api.getAllTours() } throws java.io.IOException("No connection")

        val result = repository.refreshAllTours()

        assertTrue(result is Result.Error)
    }

    @Test
    fun `refreshAllTours does not clear cache on failure`() = runTest {
        coEvery { api.getAllTours() } throws java.io.IOException("No connection")

        repository.refreshAllTours()

        coVerify(exactly = 0) { dao.clearAllTours() }
    }

    @Test
    fun `refreshTop5Tours returns Success and writes top5 entities`() = runTest {
        coEvery { api.getTop5Tours() } returns apiTours.take(1)
        coEvery { dao.getLastCacheTime() } returns null

        val result = repository.refreshTop5Tours()

        assertTrue(result is Result.Success)
        coVerify { dao.replaceAllTop5Tours(any()) }
    }

    @Test
    fun `refreshTop5Tours returns Error on server error`() = runTest {
        coEvery { api.getTop5Tours() } throws retrofit2.HttpException(
            mockk { every { code() } returns 500; every { message() } returns "Server Error" }
        )

        val result = repository.refreshTop5Tours()

        assertTrue(result is Result.Error)
    }


    @Test
    fun `refreshTourDetail upserts single tour`() = runTest {
        val detailTour = apiTours[0].copy(
            description = "Full description of the Rhino Tour"
        )
        coEvery { api.getTourDetail(1) } returns detailTour

        val result = repository.refreshTourDetail(1)

        assertTrue(result is Result.Success)
        coVerify { dao.upsertTour(any()) }
    }

    @Test
    fun `refreshTourDetail returns Error when API fails`() = runTest {
        coEvery { api.getTourDetail(any()) } throws java.io.IOException()

        val result = repository.refreshTourDetail(1)

        assertTrue(result is Result.Error)
    }

    @Test
    fun `isCacheStale returns true when cache is empty`() = runTest {
        coEvery { dao.getLastCacheTime() } returns null

        assertTrue(repository.isCacheStale())
    }

    @Test
    fun `isCacheStale returns true when cache is older than 5 minutes`() = runTest {
        val sixMinutesAgo = System.currentTimeMillis() - (6 * 60 * 1000L)
        coEvery { dao.getLastCacheTime() } returns sixMinutesAgo

        assertTrue(repository.isCacheStale())
    }

    @Test
    fun `isCacheStale returns false when cache is fresh`() = runTest {
        val oneMinuteAgo = System.currentTimeMillis() - (1 * 60 * 1000L)
        coEvery { dao.getLastCacheTime() } returns oneMinuteAgo

        assertFalse(repository.isCacheStale())
    }
}
