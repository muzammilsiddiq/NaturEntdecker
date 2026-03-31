package com.example.naturentdecker.features.tour.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.naturentdecker.data.model.Tour
import com.example.naturentdecker.data.usecases.GetAllToursUseCase
import com.example.naturentdecker.data.usecases.GetTop5ToursUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.filter
import kotlin.collections.map

@HiltViewModel
class ToursViewModel @Inject constructor(
    private val getAllToursUseCase: GetAllToursUseCase,
    private val getTop5ToursUseCase: GetTop5ToursUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ToursUiState(isLoading = true))
    val uiState: StateFlow<ToursUiState> = _uiState.asStateFlow()

    private var allTours: List<Tour> = emptyList()

    init {
        observeTours()
        refresh()
    }

    private fun observeTours() {
        combine(
            getAllToursUseCase(),
            getTop5ToursUseCase()
        ) { all, top5 ->
            val top5Ids = top5.map { it.id }.toSet()
            all.map { it.copy(isTop5 = it.id in top5Ids) }
        }
            .onEach { tours ->
                allTours = tours
                _uiState.update { state ->
                    state.copy(
                        tours = filteredTours(state.selectedTab),
                        isLoading = false,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }
            getAllToursUseCase.refresh()
            getTop5ToursUseCase.refresh()
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun onTabSelected(tab: TourTab) {
        _uiState.update { state ->
            state.copy(selectedTab = tab, tours = filteredTours(tab))
        }
    }

    private fun filteredTours(tab: TourTab): List<Tour> = when (tab) {
        TourTab.All -> allTours
        TourTab.Top5 -> allTours.filter { it.isTop5 }
    }
}