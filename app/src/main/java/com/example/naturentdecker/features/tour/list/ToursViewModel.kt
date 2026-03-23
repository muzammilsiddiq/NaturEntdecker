package com.example.naturentdecker.features.tour.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.naturentdecker.data.usecases.GetAllToursUseCase
import com.example.naturentdecker.data.usecases.GetTop5ToursUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ToursViewModel @Inject constructor(
    private val getAllToursUseCase: GetAllToursUseCase,
    private val getTop5ToursUseCase: GetTop5ToursUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ToursUiState(isLoading = true))
    val uiState: StateFlow<ToursUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null

    init {
        observeAllTours()
        refresh()
    }

    private fun observeAllTours() {
        observeJob?.cancel()
        observeJob = getAllToursUseCase()
            .onEach { tours ->
                _uiState.update { it.copy(tours = tours, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    private fun observeTop5Tours() {
        observeJob?.cancel()
        observeJob = getTop5ToursUseCase()
            .onEach { tours ->
                _uiState.update { it.copy(tours = tours, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }

            if (_uiState.value.showTop5) {
                getTop5ToursUseCase.refresh()
            } else {
                getAllToursUseCase.refresh()
            }

            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun toggleTop5() {
        val newShowTop5 = !_uiState.value.showTop5
        _uiState.update { it.copy(showTop5 = newShowTop5, isLoading = true, error = null) }
        if (newShowTop5) observeTop5Tours() else observeAllTours()
        refresh()
    }
}


