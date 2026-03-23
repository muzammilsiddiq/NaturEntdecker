package com.example.naturentdecker.data.usecases

import com.example.naturentdecker.data.TourRepository
import com.example.naturentdecker.data.model.Tour
import com.example.naturentdecker.utils.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTourDetailUseCase @Inject constructor(
    private val repository: TourRepository
) {
    operator fun invoke(id: Int): Flow<Tour?> = repository.getTourDetailFlow(id)
    suspend fun refresh(id: Int): Result<Unit> = repository.refreshTourDetail(id)
}
