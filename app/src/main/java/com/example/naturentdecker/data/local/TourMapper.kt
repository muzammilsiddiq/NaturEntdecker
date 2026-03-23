package com.example.naturentdecker.data.local

import com.example.naturentdecker.data.local.entity.TourEntity
import com.example.naturentdecker.data.model.Tour

fun Tour.toEntity(isTop5: Boolean = false): TourEntity {
    return TourEntity(
        id = id,
        title = title,
        shortDescription = shortDescription,
        description = description,
        price = price,
        thumb = thumb,
        isTop5 = isTop5,
        startDate = startDate,
        endDate = endDate,
        cachedAt = System.currentTimeMillis(),
    )
}

fun TourEntity.toDomain(): Tour {
    return Tour(
        id = id,
        title = title,
        shortDescription = shortDescription,
        description = description,
        price = price,
        thumb = thumb,
        startDate = startDate,
        endDate = endDate,
    )
}
