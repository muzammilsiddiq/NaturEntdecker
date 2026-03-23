package com.example.naturentdecker.ui.tourdetail

import app.cash.turbine.test
import com.example.naturentdecker.data.model.Tour
import com.example.naturentdecker.data.usecases.GetContactUseCase
import com.example.naturentdecker.data.usecases.GetTourDetailUseCase
import com.example.naturentdecker.features.tour.detail.TourDetailViewModel
import com.example.naturentdecker.utils.AppException
import com.example.naturentdecker.utils.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TourDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getTourDetailUseCase: GetTourDetailUseCase
    private lateinit var getContactUseCase: GetContactUseCase
    private lateinit var viewModel: TourDetailViewModel

    private val fakeTour = Tour(
        id = 1,
        title = "Rhino Tour",
        price = "10.3",
        startDate = "2026-03-16T01:31:04+00:00",
        endDate = "2026-04-23T01:31:04+00:00",
        thumb = "https://dummyimage.com/400x200/ff7f",
        shortDescription = "Test short description",
        description = "Test full description"

    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getTourDetailUseCase = mockk()
        getContactUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = TourDetailViewModel(getTourDetailUseCase, getContactUseCase)

    @Test
    fun `initial state is empty`() = runTest {
        viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertNull(state.tour)
            assertFalse(state.isLoading)
            assertNull(state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadTour shows tour from cache`() = runTest {
        every { getTourDetailUseCase(1) } returns flowOf(fakeTour)
        coEvery { getTourDetailUseCase.refresh(1) } returns Result.Success(Unit)

        viewModel = createViewModel()
        viewModel.loadTour(1)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(fakeTour, state.tour)
            assertFalse(state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadTour sets isLoading false after load`() = runTest {
        every { getTourDetailUseCase(1) } returns flowOf(fakeTour)
        coEvery { getTourDetailUseCase.refresh(1) } returns Result.Success(Unit)

        viewModel = createViewModel()
        viewModel.loadTour(1)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            assertFalse(awaitItem().isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadTour shows error when no cache and network fails`() = runTest {
        every { getTourDetailUseCase(1) } returns flowOf(null)
        coEvery { getTourDetailUseCase.refresh(1) } returns
                Result.Error(AppException.NetworkException())

        viewModel = createViewModel()
        viewModel.loadTour(1)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertNull(state.tour)
            assertNotNull(state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadTour shows tour even when network refresh fails`() = runTest {
        every { getTourDetailUseCase(1) } returns flowOf(fakeTour)
        coEvery { getTourDetailUseCase.refresh(1) } returns
                Result.Error(AppException.NetworkException())

        viewModel = createViewModel()
        viewModel.loadTour(1)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(fakeTour, state.tour)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadTour called twice with same id is no-op second time`() = runTest {
        every { getTourDetailUseCase(1) } returns flowOf(fakeTour)
        coEvery { getTourDetailUseCase.refresh(1) } returns Result.Success(Unit)

        viewModel = createViewModel()
        viewModel.loadTour(1)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.loadTour(1)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(fakeTour, state.tour)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadTour calls refresh with correct id`() = runTest {
        every { getTourDetailUseCase(42) } returns flowOf(fakeTour)
        coEvery { getTourDetailUseCase.refresh(42) } returns Result.Success(Unit)

        viewModel = createViewModel()
        viewModel.loadTour(42)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { getTourDetailUseCase.refresh(42) }
    }

    @Test
    fun `loadTour server error with no cache shows error`() = runTest {
        every { getTourDetailUseCase(1) } returns flowOf(null)
        coEvery { getTourDetailUseCase.refresh(1) } returns
                Result.Error(AppException.ServerException(404, "Not Found"))

        viewModel = createViewModel()
        viewModel.loadTour(1)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertNotNull(state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clear resets tour to null`() = runTest {
        every { getTourDetailUseCase(1) } returns flowOf(fakeTour)
        coEvery { getTourDetailUseCase.refresh(1) } returns Result.Success(Unit)

        viewModel = createViewModel()
        viewModel.loadTour(1)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.clear()

        viewModel.uiState.test {
            assertNull(awaitItem().tour)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clear resets error to null`() = runTest {
        every { getTourDetailUseCase(1) } returns flowOf(null)
        coEvery { getTourDetailUseCase.refresh(1) } returns
                Result.Error(AppException.NetworkException())

        viewModel = createViewModel()
        viewModel.loadTour(1)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.clear()

        viewModel.uiState.test {
            assertNull(awaitItem().error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clear resets isLoading to false`() = runTest {
        viewModel = createViewModel()
        viewModel.clear()

        viewModel.uiState.test {
            assertFalse(awaitItem().isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `after clear loadTour can load again`() = runTest {
        every { getTourDetailUseCase(1) } returns flowOf(fakeTour)
        coEvery { getTourDetailUseCase.refresh(1) } returns Result.Success(Unit)

        viewModel = createViewModel()
        viewModel.loadTour(1)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.clear()
        viewModel.loadTour(1)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(fakeTour, state.tour)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
