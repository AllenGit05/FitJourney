package com.example.fitjourneyag.domain.repository

import com.example.fitjourneyag.domain.model.ApiConfig
import kotlinx.coroutines.flow.Flow

interface ApiRepository {
    suspend fun generateContent(prompt: String): String
    suspend fun generateContentWithSystem(prompt: String, systemInstruction: String): String
}
