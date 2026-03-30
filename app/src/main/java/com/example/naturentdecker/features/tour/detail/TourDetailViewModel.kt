package com.example.naturentdecker.features.tour.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.naturentdecker.data.model.Tour
import com.example.naturentdecker.data.usecases.GetContactUseCase
import com.example.naturentdecker.data.usecases.GetTourDetailUseCase
import com.example.naturentdecker.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class TourDetailUiState(
    val isLoading: Boolean = false,
    val tour: Tour? = null,
    val contactPhone: String? = null,
    val error: String? = null,
)

@HiltViewModel
class TourDetailViewModel @Inject constructor(
    private val getTourDetailUseCase: GetTourDetailUseCase,
    private val getContactUseCase: GetContactUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TourDetailUiState())
    val uiState: StateFlow<TourDetailUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null
    private var currentId: Int? = null

    init {
        loadContact()
    }

    fun loadTour(id: Int) {
        if (currentId == id) return
        currentId = id

        _uiState.update { it.copy(isLoading = true, error = null, tour = null) }

        observeJob?.cancel()
        observeJob = getTourDetailUseCase(id)
            .filterNotNull()
            .onEach { tour ->
                Timber.d("Tour detail updated from cache: ${tour.title}")
                _uiState.update { it.copy(tour = tour, isLoading = false) }
            }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            val result = getTourDetailUseCase.refresh(id)
            if (result is Result.Error && _uiState.value.tour == null) {
                Timber.e(result.exception, "Failed to load tour $id")
                _uiState.update { it.copy(isLoading = false, error = result.exception.message) }
            }
        }
    }

    private fun loadContact() {
        viewModelScope.launch {
            when (val result = getContactUseCase()) {
                is Result.Success -> _uiState.update { it.copy(contactPhone = result.data.phone) }
                is Result.Error -> Timber.w(result.exception, "Could not load contact info")
            }
        }
    }

    fun clear() {
        currentId = null
        observeJob?.cancel()
        _uiState.update { TourDetailUiState() }
    }
}
