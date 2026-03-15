package com.example.fitjourney.domain.repository

import com.example.fitjourney.data.local.entity.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getMessages(userId: String): Flow<List<ChatMessageEntity>>
    suspend fun saveMessage(message: ChatMessageEntity)
    suspend fun clearHistory(userId: String)
}
