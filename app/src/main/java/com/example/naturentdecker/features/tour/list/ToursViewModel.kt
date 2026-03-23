package com.example.naturentdecker.features.tour.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.naturentdecker.data.model.Tour
import com.example.naturentdecker.data.repository.TourRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ToursViewModel @Inject constructor(
    private val repository: TourRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ToursUiState())
    val uiState: StateFlow<ToursUiState> = _uiState.asStateFlow()

    init {
        loadTours()
    }

    fun loadTours(showTop5: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, showTop5 = showTop5)
            val result = if (showTop5) repository.getTop5Tours() else repository.getAllTours()
            result.fold(
                onSuccess = { tours ->
                    Timber.d("Loaded ${tours.size} tours (top5=$showTop5)")
                    _uiState.value = _uiState.value.copy(isLoading = false, tours = tours)
                },
                onFailure = { error ->
                    Timber.e(error, "Failed to load tours")
                    _uiState.value = _uiState.value.copy(isLoading = false, error = error.message)
                }
            )
        }
    }

    fun toggleTop5() {
        loadTours(!_uiState.value.showTop5)
    }
}

data class ToursUiState(
    val isLoading: Boolean = false,
    val tours: List<Tour> = emptyList(),
    val error: String? = null,
    val showTop5: Boolean = false,
)