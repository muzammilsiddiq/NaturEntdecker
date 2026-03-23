package com.example.naturentdecker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.naturentdecker.data.local.dao.TourDao
import com.example.naturentdecker.data.local.entity.TourEntity

@Database(
    entities = [TourEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class NaturEntdeckerDatabase : RoomDatabase() {
    abstract fun tourDao(): TourDao
}
