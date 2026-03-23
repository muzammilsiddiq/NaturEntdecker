package com.example.naturentdecker.features.tour.list

import com.example.naturentdecker.data.model.Tour

data class ToursUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val tours: List<Tour> = emptyList(),
    val error: String? = null,
    val showTop5: Boolean = false,
)