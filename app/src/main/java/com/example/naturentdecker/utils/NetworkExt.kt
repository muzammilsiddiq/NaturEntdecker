package com.example.naturentdecker.utils

import retrofit2.HttpException
import java.io.IOException

suspend fun <T> safeApiCall(call: suspend () -> T): Result<T> {
    return try {
        Result.Success(call())
    } catch (e: IOException) {
        Result.Error(AppException.NetworkException(e))
    } catch (e: HttpException) {
        Result.Error(AppException.ServerException(e.code(), e.message()))
    } catch (e: Exception) {
        Result.Error(AppException.UnknownException(e))
    }
}
