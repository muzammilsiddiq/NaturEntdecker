package com.example.naturentdecker.di

import android.content.Context
import com.example.naturentdecker.BuildConfig
import com.example.naturentdecker.data.remote.api.ToursApiService
import com.example.naturentdecker.network.interceptor.AcceptJsonInterceptor
import com.example.naturentdecker.network.interceptor.LoggingInterceptor
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import kotlin.jvm.java

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    private const val BASE_URL = "https://bitsfabrik.com/projekte/imaginary/api/"
    private const val CONNECT_TIMEOUT_SECONDS = 30L
    private const val READ_TIMEOUT_SECONDS = 30L
    private const val WRITE_TIMEOUT_SECONDS = 30L
    
    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    @Provides
    @Singleton
    fun provideAcceptJsonInterceptor(): AcceptJsonInterceptor {
        return AcceptJsonInterceptor()
    }
    
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): LoggingInterceptor {
        return LoggingInterceptor(
            enabled = BuildConfig.DEBUG
        )
    }
    
    @Provides
    @Singleton
    fun provideOkHttpClient(
        acceptJsonInterceptor: AcceptJsonInterceptor,
        loggingInterceptor: LoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(acceptJsonInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .connectionPool(okhttp3.ConnectionPool(
                maxIdleConnections = 5,
                keepAliveDuration = 5,
                TimeUnit.MINUTES
            ))
            .build()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(
        moshi: Moshi,
        okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }
    
    @Provides
    @Singleton
    fun provideToursApiService(retrofit: Retrofit): ToursApiService {
        return retrofit.create(ToursApiService::class.java)
    }
}