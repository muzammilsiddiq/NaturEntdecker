package com.example.naturentdecker.data.usecases

import com.example.naturentdecker.data.TourRepository
import com.example.naturentdecker.data.model.Tour
import com.example.naturentdecker.utils.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllToursUseCase @Inject constructor(
    private val repository: TourRepository
) {
    operator fun invoke(): Flow<List<Tour>> = repository.getAllToursFlow()
    suspend fun refresh(): Result<Unit> = repository.refreshAllTours()
}