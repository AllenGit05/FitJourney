package com.example.fitjourney.util

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

object RateLimitHandler {

    fun parseRetrySeconds(errorMessage: String): Int {
        return try {
            val regex = Regex("retry in (\\d+\\.?\\d*)s")
            val match = regex.find(errorMessage)
            match?.groupValues?.get(1)?.toDouble()?.toInt()?.plus(2) ?: 60
        } catch (e: Exception) { 60 }
    }

    fun isRateLimitError(error: String): Boolean {
        return error.contains("quota", ignoreCase = true) ||
               error.contains("rate limit", ignoreCase = true) ||
               error.contains("RESOURCE_EXHAUSTED", ignoreCase = true) ||
               error.contains("ResourceExhausted", ignoreCase = true) ||
               error.contains("exceeded your current quota", ignoreCase = true) ||
               error.contains("429", ignoreCase = true)
    }


    fun shouldFallbackToGroq(error: String): Boolean {
        return isRateLimitError(error) ||
               error.contains("404", ignoreCase = true) ||
               error.contains("503", ignoreCase = true) ||
               error.contains("timeout", ignoreCase = true) ||
               error.contains("Unable to resolve host", ignoreCase = true)
    }

    fun countdownFlow(seconds: Int): Flow<Int> = flow {
        for (i in seconds downTo 0) {
            emit(i)
            if (i > 0) delay(1000L)
        }
    }
}
