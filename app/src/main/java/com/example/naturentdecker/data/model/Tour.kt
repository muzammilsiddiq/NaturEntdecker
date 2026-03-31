package com.example.naturentdecker.data.model

import com.example.naturentdecker.utils.formatAsPrice
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Tour(
    val id: Int,
    val title: String,
    val shortDescription: String?,
    val description: String?,
    val thumb: String?,
    val startDate: String,
    val endDate: String,
    val price: String
)

val Tour.formattedPrice: String
    get() = price.formatAsPrice()