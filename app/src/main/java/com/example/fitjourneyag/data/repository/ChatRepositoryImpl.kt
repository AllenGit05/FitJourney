package com.example.fitjourneyag.data.repository

import com.example.fitjourneyag.data.local.dao.ChatDao
import com.example.fitjourneyag.data.local.entity.ChatMessageEntity
import com.example.fitjourneyag.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow

class ChatRepositoryImpl(private val chatDao: ChatDao) : ChatRepository {
    override fun getMessages(userId: String): Flow<List<ChatMessageEntity>> {
        return chatDao.getMessagesForUser(userId)
    }

    override suspend fun saveMessage(message: ChatMessageEntity) {
        chatDao.insertMessage(message)
    }

    override suspend fun clearHistory(userId: String) {
        chatDao.clearHistory(userId)
    }
}
