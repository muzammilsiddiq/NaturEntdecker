package com.example.naturentdecker.data

import com.example.naturentdecker.data.model.Tour
import com.example.naturentdecker.data.usecases.GetAllToursUseCase
import com.example.naturentdecker.data.usecases.GetTop5ToursUseCase
import com.example.naturentdecker.data.usecases.GetTourDetailUseCase
import com.example.naturentdecker.utils.AppException
import com.example.naturentdecker.utils.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class UseCasesTest {

    private lateinit var repository: TourRepository

    private val fakeTours = listOf(
        Tour(
            id = 1,
            title = "Rhino Tour",
            price = "10.3",
            startDate = "2026-03-16T01:31:04+00:00",
            endDate = "2026-04-23T01:31:04+00:00",
            thumb = "https://dummyimage.com/400x200/ff7f",
            shortDescription = "Test short description",
            description = "Test full description"

        ),
        Tour(
            id = 2,
            title = "Gorilla Tour",
            price = "20.6",
            startDate = "2026-03-16T01:31:04+00:00",
            endDate = "2026-04-23T01:31:04+00:00",
            thumb = "https://dummyimage.com/400x200/ff7f",
            shortDescription = "Test short description",
            description = "Test full description"
        )
    )

    @Before
    fun setup() {
        repository = mockk()
    }

    @Test
    fun `GetAllToursUseCase invoke delegates to repository getAllToursFlow`() = runTest {
        every { repository.getAllToursFlow() } returns flowOf(fakeTours)
        val useCase = GetAllToursUseCase(repository)

        val result = useCase().first()

        assertEquals(fakeTours, result)
        verify(exactly = 1) { repository.getAllToursFlow() }
    }

    @Test
    fun `GetAllToursUseCase invoke emits empty list from repository`() = runTest {
        every { repository.getAllToursFlow() } returns flowOf(emptyList())
        val useCase = GetAllToursUseCase(repository)

        val result = useCase().first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `GetAllToursUseCase refresh delegates to repository refreshAllTours`() = runTest {
        coEvery { repository.refreshAllTours() } returns Result.Success(Unit)
        val useCase = GetAllToursUseCase(repository)

        val result = useCase.refresh()

        assertTrue(result is Result.Success)
        coVerify(exactly = 1) { repository.refreshAllTours() }
    }

    @Test
    fun `GetAllToursUseCase refresh returns Error from repository`() = runTest {
        coEvery { repository.refreshAllTours() } returns Result.Error(AppException.NetworkException())
        val useCase = GetAllToursUseCase(repository)

        val result = useCase.refresh()

        assertTrue(result is Result.Error)
        assertTrue((result as Result.Error).exception is AppException.NetworkException)
    }

    @Test
    fun `GetTop5ToursUseCase refresh delegates to repository refreshTop5Tours`() = runTest {
        coEvery { repository.refreshTop5Tours() } returns Result.Success(Unit)
        val useCase = GetTop5ToursUseCase(repository)

        val result = useCase.refresh()

        assertTrue(result is Result.Success)
        coVerify(exactly = 1) { repository.refreshTop5Tours() }
    }

    @Test
    fun `GetTop5ToursUseCase refresh propagates server error`() = runTest {
        coEvery { repository.refreshTop5Tours() } returns Result.Error(
            AppException.ServerException(
                503,
                "Unavailable"
            )
        )
        val useCase = GetTop5ToursUseCase(repository)

        val result = useCase.refresh()

        assertTrue(result is Result.Error)
        val ex = (result as Result.Error).exception
        assertTrue(ex is AppException.ServerException)
        assertEquals(503, (ex as AppException.ServerException).code)
    }

    @Test
    fun `GetTourDetailUseCase invoke emits null when tour not cached`() = runTest {
        every { repository.getTourDetailFlow(99) } returns flowOf(null)
        val useCase = GetTourDetailUseCase(repository)

        val result = useCase(99).first()

        assertNull(result)
    }

    @Test
    fun `GetTourDetailUseCase refresh delegates to repository refreshTourDetail`() = runTest {
        coEvery { repository.refreshTourDetail(1) } returns Result.Success(Unit)
        val useCase = GetTourDetailUseCase(repository)

        val result = useCase.refresh(1)

        assertTrue(result is Result.Success)
        coVerify(exactly = 1) { repository.refreshTourDetail(1) }
    }

    @Test
    fun `GetTourDetailUseCase refresh passes correct id to repository`() = runTest {
        coEvery { repository.refreshTourDetail(42) } returns Result.Success(Unit)
        val useCase = GetTourDetailUseCase(repository)

        useCase.refresh(42)

        coVerify(exactly = 1) { repository.refreshTourDetail(42) }
    }

    @Test
    fun `GetTourDetailUseCase refresh returns Error on failure`() = runTest {
        coEvery { repository.refreshTourDetail(any()) } returns Result.Error(AppException.NetworkException())
        val useCase = GetTourDetailUseCase(repository)

        val result = useCase.refresh(1)

        assertTrue(result is Result.Error)
    }

    @Test
    fun `GetAllToursUseCase does not call getTop5ToursFlow`() = runTest {
        every { repository.getAllToursFlow() } returns flowOf(fakeTours)
        val useCase = GetAllToursUseCase(repository)

        useCase().first()

        verify(exactly = 0) { repository.getTop5ToursFlow() }
    }
}
