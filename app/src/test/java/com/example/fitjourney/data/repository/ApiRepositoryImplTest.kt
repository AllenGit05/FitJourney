package com.example.fitjourney.data.repository

import com.example.fitjourney.domain.model.ApiConfig
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class ApiRepositoryImplTest {

    @Test
    fun addApiConfig_increasesListSize() = runBlocking {
        val repository = ApiRepositoryImpl()
        val initialList = repository.apiConfigs.first()
        assertEquals(0, initialList.size)

        repository.addApiConfig(ApiConfig(providerName = "Gemini", apiKey = "test_key"))
        
        val updatedList = repository.apiConfigs.first()
        assertEquals(1, updatedList.size)
        assertEquals("Gemini", updatedList[0].providerName)
        assertFalse(updatedList[0].id.isEmpty())
    }

    @Test
    fun removeApiConfig_decreasesListSize() = runBlocking {
        val repository = ApiRepositoryImpl()
        repository.addApiConfig(ApiConfig(providerName = "Gemini", apiKey = "test_key"))
        val listAfterAdd = repository.apiConfigs.first()
        val idToRemove = listAfterAdd[0].id

        repository.removeApiConfig(idToRemove)
        
        val listAfterRemove = repository.apiConfigs.first()
        assertTrue(listAfterRemove.isEmpty())
    }

    @Test
    fun toggleApiStatus_changesActiveState() = runBlocking {
        val repository = ApiRepositoryImpl()
        repository.addApiConfig(ApiConfig(providerName = "Gemini", apiKey = "test_key", isActive = true))
        val list = repository.apiConfigs.first()
        val id = list[0].id

        repository.toggleApiStatus(id)
        
        val updatedList = repository.apiConfigs.first()
        assertFalse(updatedList[0].isActive)
    }
}
