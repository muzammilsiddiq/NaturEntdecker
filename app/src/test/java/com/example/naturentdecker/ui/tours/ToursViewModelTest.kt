package com.example.naturentdecker.ui.tours

import app.cash.turbine.test
import com.example.naturentdecker.data.model.Tour
import com.example.naturentdecker.data.usecases.GetAllToursUseCase
import com.example.naturentdecker.data.usecases.GetTop5ToursUseCase
import com.example.naturentdecker.features.tour.list.ToursViewModel
import com.example.naturentdecker.utils.AppException
import com.example.naturentdecker.utils.Result
import io.mockk.coEvery
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
class ToursViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getAllToursUseCase: GetAllToursUseCase
    private lateinit var getTop5ToursUseCase: GetTop5ToursUseCase
    private lateinit var viewModel: ToursViewModel

    private val allTours = listOf(
        Tour(
            id = 1,
            title = "Rhino Tour",
            price = "10.3",
            startDate = "2026-03-16T01:31:04+00:00",
            endDate = "2026-04-23T01:31:04+00:00",
            thumb = "https://dummyimage.com/400x200/ff7f7f/333333?text=Rhino",
            shortDescription = "Test short description",
            description = "Test full description"
        ),
        Tour(
            id = 2,
            title = "Gorilla Tour",
            price = "20.6",
            startDate = "2026-03-16T01:31:04+00:00",
            endDate = "2026-04-23T01:31:04+00:00",
            thumb = "https://dummyimage.com/40,0x200/ff7f7f/333333?text=Gorilla",
            shortDescription = "Test short description",
            description = "Test full description"
        ),
        Tour(
            id = 3,
            title = "Elephant Tour",
            price = "15",
            startDate = "2026-03-16T01:31:04+00:00",
            endDate = "2026-04-23T01:31:04+00:00",
            thumb = "https://dummyimage.com/400x200/ff",
            shortDescription = "Test short description",
            description = "Test full description"
        ),
        )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getAllToursUseCase = mockk()
        getTop5ToursUseCase = mockk()

        // Default happy-path stubs
        every { getAllToursUseCase() } returns flowOf(allTours)
        coEvery { getAllToursUseCase.refresh() } returns Result.Success(Unit)
        coEvery { getTop5ToursUseCase.refresh() } returns Result.Success(Unit)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = ToursViewModel(getAllToursUseCase, getTop5ToursUseCase)

    // ─── Initial state ────────────────────────────────────────────────────────

    @Test
    fun `initial state has isLoading true`() = runTest {
        viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state shows all tours after load`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(allTours, state.tours)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state has no error on success`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertNull(state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `initial state has isLoading false after load`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refresh clears error state`() = runTest {
        every { getAllToursUseCase() } returns flowOf(emptyList())
        coEvery { getAllToursUseCase.refresh() } returnsMany listOf(
            Result.Error(AppException.NetworkException()),
            Result.Success(Unit),
        )
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.refresh()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertNull(state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refresh sets isRefreshing false when done`() = runTest {
        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.refresh()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isRefreshing)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `network error with cached data does not show error`() = runTest {
        every { getAllToursUseCase() } returns flowOf(allTours)
        coEvery { getAllToursUseCase.refresh() } returns
                Result.Error(AppException.NetworkException())

        viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertNull(state.error)
            assertEquals(allTours, state.tours)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
