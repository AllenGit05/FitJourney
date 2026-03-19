package com.example.fitjourney.data.repository

import com.example.fitjourney.data.local.dao.ChatDao
import com.example.fitjourney.data.local.entity.ChatMessageEntity
import com.example.fitjourney.domain.repository.ChatRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import java.io.Closeable

class ChatRepositoryImpl(
    private val chatDao: ChatDao,
    private val syncManager: com.example.fitjourney.data.sync.SyncManager
) : ChatRepository, Closeable {

    // The scope's lifetime matches the Application lifecycle as this is a singleton in AppContainer.
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    override fun getMessages(userId: String): Flow<List<ChatMessageEntity>> {
        return chatDao.getMessagesForUser(userId)
    }

    override suspend fun saveMessage(message: ChatMessageEntity) {
        chatDao.insertMessage(message.copy(isSynced = false))
        syncManager.startSync()
    }

    override suspend fun clearHistory(userId: String) {
        // For clearHistory, we might want to mark all as deleted
        chatDao.clearHistory(userId)
    }

    override suspend fun getMessageCount(userId: String): Int {
        return chatDao.getMessageCount(userId)
    }

    override suspend fun pruneHistory(userId: String, limit: Int) {
        chatDao.deleteOldestMessages(userId, limit)
    }

    override fun close() {
        repositoryScope.cancel()
    }
}
