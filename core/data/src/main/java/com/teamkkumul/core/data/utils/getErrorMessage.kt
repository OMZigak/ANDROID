package com.teamkkumul.core.data.utils

import com.teamkkumul.core.network.dto.response.BaseResponse
import com.teamkkumul.model.network.Error
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import timber.log.Timber
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

private const val UNKNOWN_ERROR_MESSAGE = "Unknown error"
private const val NETWORK_CONNECT_ERROR_MESSAGE = "네트워크 연결이 원활하지 않습니다"
private const val INTERNET_CONNECTION_ERROR_MESSAGE = "인터넷에 연결해 주세요"
private const val TIMEOUT_ERROR_MESSAGE = "서버가 응답하지 않습니다"
private const val NULL_ERROR_MESSAGE = "error message is null"

private fun HttpException.getErrorMessage(): String {
    val errorBody = response()?.errorBody()?.string() ?: return UNKNOWN_ERROR_MESSAGE
    return parseErrorMessage(errorBody)
}

private fun parseErrorMessage(errorBody: String): String =
    try {
        val errorResponse = Json.decodeFromString<BaseResponse<BaseResponse.BaseError>>(errorBody)
        errorResponse.error?.message ?: NULL_ERROR_MESSAGE
    } catch (e: Exception) {
        UNKNOWN_ERROR_MESSAGE
    }

fun Throwable.toCustomError(): Throwable {
    Timber.e(this)
    return when (this) {
        is HttpException -> Error.ApiError(this.getErrorMessage())
        is UnknownHostException -> Error.NetWorkConnectError(NETWORK_CONNECT_ERROR_MESSAGE)
        is ConnectException -> Error.NetWorkConnectError(INTERNET_CONNECTION_ERROR_MESSAGE)
        is SocketTimeoutException -> Error.TimeOutError(TIMEOUT_ERROR_MESSAGE)
        else -> Error.UnknownError(this.message ?: UNKNOWN_ERROR_MESSAGE)
    }
}

fun <T> Throwable.handleThrowable(): Result<T> = Result.failure(this.toCustomError())
