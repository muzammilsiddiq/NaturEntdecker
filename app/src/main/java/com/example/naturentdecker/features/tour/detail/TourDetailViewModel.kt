package com.example.naturentdecker.features.tour.detail

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

data class TourDetailUiState(
    val isLoading: Boolean = false,
    val tour: Tour? = null,
    val error: String? = null,
)

@HiltViewModel
class TourDetailViewModel @Inject constructor(
    private val repository: TourRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TourDetailUiState())
    val uiState: StateFlow<TourDetailUiState> = _uiState.asStateFlow()

    fun loadTour(id: Int) {
        if (_uiState.value.tour?.id?.toInt() == id) return

        viewModelScope.launch {
            _uiState.value = TourDetailUiState(isLoading = true)
            repository.getTourDetail(id).fold(
                onSuccess = { tour ->
                    Timber.d("Loaded tour detail: ${tour.title}")
                    _uiState.value = TourDetailUiState(tour = tour)
                },
                onFailure = { error ->
                    Timber.e(error, "Failed to load tour $id")
                    _uiState.value = TourDetailUiState(error = error.message)
                }
            )
        }
    }

    fun clear() {
        _uiState.value = TourDetailUiState()
    }
}
