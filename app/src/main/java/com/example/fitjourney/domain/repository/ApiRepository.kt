package com.example.fitjourney.domain.repository

import com.example.fitjourney.domain.model.ApiConfig
import kotlinx.coroutines.flow.Flow

interface ApiRepository {
    suspend fun generateContent(prompt: String): String
    suspend fun generateContentWithSystem(prompt: String, systemInstruction: String): String
}
