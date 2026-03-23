package com.example.naturentdecker.data.repository

import com.example.naturentdecker.data.model.Contact
import com.example.naturentdecker.data.model.Tour
import com.example.naturentdecker.data.remote.api.ToursApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TourRepository @Inject constructor(
    private val api: ToursApiService,
) {
    suspend fun getAllTours(): Result<List<Tour>> =
        runCatching { api.getAllTours() }

    suspend fun getTop5Tours(): Result<List<Tour>> =
        runCatching { api.getTop5Tours() }

    suspend fun getTourDetail(id: Int): Result<Tour> =
        runCatching { api.getTourDetail(id) }

    suspend fun getContact(): Result<Contact> =
        runCatching { api.getContact() }
}
