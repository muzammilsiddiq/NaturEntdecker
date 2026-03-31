package com.example.naturentdecker.di

import android.content.Context
import androidx.room.Room
import com.example.naturentdecker.data.local.NaturEntdeckerDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NaturEntdeckerDatabase =
        Room.databaseBuilder(
            context,
            NaturEntdeckerDatabase::class.java,
            "naturentdecker.db"
        )
            .fallbackToDestructiveMigration(true)
            .build()
}
