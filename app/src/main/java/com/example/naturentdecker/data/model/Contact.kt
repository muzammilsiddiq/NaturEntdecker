package com.example.naturentdecker.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Contact(
    val companyName: String,
    val street: String,
    val country: String,
    val phone: String
)