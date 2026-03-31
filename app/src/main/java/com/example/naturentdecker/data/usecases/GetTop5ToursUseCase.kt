package com.example.naturentdecker.data.usecases

import com.example.naturentdecker.data.repository.TourRepository
import com.example.naturentdecker.data.model.Tour
import com.example.naturentdecker.utils.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTop5ToursUseCase @Inject constructor(
    private val repository: TourRepository
) {
    operator fun invoke(): Flow<List<Tour>> = repository.getTop5ToursFlow()
    suspend fun refresh(): Result<Unit> = repository.refreshTop5Tours()
}