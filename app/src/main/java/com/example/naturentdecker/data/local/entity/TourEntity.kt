package com.example.naturentdecker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tour")
data class TourEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val shortDescription: String?,
    val description: String?,
    val price: String,
    val thumb: String?,
    val isTop5: Boolean = false,
    val startDate: String,
    val endDate: String,
    val cachedAt: Long = System.currentTimeMillis(),
)