package com.example.naturentdecker.data.remote.api

import com.example.naturentdecker.data.model.Contact
import com.example.naturentdecker.data.model.Tour
import retrofit2.http.GET
import retrofit2.http.Path

interface ToursApiService {
    @GET("tours/")
    suspend fun getAllTours(): List<Tour>

    @GET("tours/top5/")
    suspend fun getTop5Tours(): List<Tour>

    @GET("tours/{id}/")
    suspend fun getTourDetail(@Path("id") id: Int): Tour

    @GET("contact/")
    suspend fun getContact(): Contact
}