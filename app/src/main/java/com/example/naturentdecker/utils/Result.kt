package com.example.naturentdecker.utils

sealed interface Result<out T> {
    data class Success<T>(val data: T) : Result<T>
    data class Error(val exception: AppException) : Result<Nothing>
}

sealed class AppException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class NetworkException(cause: Throwable? = null) :
        AppException("No internet connection. Showing cached data.", cause)

    class ServerException(val code: Int, message: String) :
        AppException("Server error ($code): $message")

    class UnknownException(cause: Throwable? = null) :
        AppException("An unexpected error occurred.", cause)
}