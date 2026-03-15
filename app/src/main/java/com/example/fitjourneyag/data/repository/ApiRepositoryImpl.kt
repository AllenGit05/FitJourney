package com.example.fitjourneyag.data.repository

import com.example.fitjourneyag.data.remote.AiEngine
import com.example.fitjourneyag.domain.repository.ApiRepository

class ApiRepositoryImpl(
    private val aiEngine: AiEngine
) : ApiRepository {

    override suspend fun generateContent(prompt: String): String {
        val result = aiEngine.generate(prompt)
        if (result.error != null) throw Exception(result.error)
        return result.content
    }

    override suspend fun generateContentWithSystem(
        prompt: String,
        systemInstruction: String
    ): String {
        val result = aiEngine.generate(prompt, systemInstruction)
        if (result.error != null) throw Exception(result.error)
        return result.content
    }
}
