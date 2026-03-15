package com.example.fitjourneyag.data.repository

import com.example.fitjourneyag.domain.model.UserRole
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthRepositoryImplTest {

    private val repository = AuthRepositoryImpl()

    @Test
    fun login_withAdminEmail_returnsAdminRole() = runBlocking {
        val email = "admin@fitjourney.com"
        val password = "anyPassword"
        
        val result = repository.login(email, password)
        
        assertTrue(result.isSuccess)
        val user = result.getOrNull()
        assertEquals(UserRole.ADMIN, user?.role)
        assertEquals("admin-uid-001", user?.uid)
    }

    @Test
    fun login_withRegularEmail_returnsClientRole() = runBlocking {
        val email = "user@example.com"
        val password = "anyPassword"
        
        val result = repository.login(email, password)
        
        assertTrue(result.isSuccess)
        val user = result.getOrNull()
        assertEquals(UserRole.CLIENT, user?.role)
        assertEquals("mock-uid-001", user?.uid)
    }

    @Test
    fun login_emptyCredentials_returnsFailure() = runBlocking {
        val result = repository.login("", "")
        assertTrue(result.isFailure)
    }
}
