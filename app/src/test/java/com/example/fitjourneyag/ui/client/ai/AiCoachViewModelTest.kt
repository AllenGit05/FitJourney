package com.example.fitjourneyag.ui.client.ai

import com.example.fitjourneyag.domain.model.ApiConfig
import com.example.fitjourneyag.domain.model.User
import com.example.fitjourneyag.domain.repository.ApiRepository
import com.example.fitjourneyag.domain.repository.DietRepository
import com.example.fitjourneyag.domain.repository.UserRepository
import com.example.fitjourneyag.domain.repository.WorkoutRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class AiCoachViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    
    private lateinit var viewModel: AiCoachViewModel
    private val userRepository: UserRepository = mock()
    private val workoutRepository: WorkoutRepository = mock()
    private val dietRepository: DietRepository = mock()
    private val apiRepository: ApiRepository = mock()
    
    private val apiConfigsFlow = MutableStateFlow<List<ApiConfig>>(emptyList())
    private val userFlow = MutableStateFlow(User(uid = "user1", email = "user@test.com", aiCredits = 10))

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        whenever(apiRepository.apiConfigs).thenReturn(apiConfigsFlow)
        whenever(userRepository.userProfile).thenReturn(userFlow)
        whenever(workoutRepository.workoutHistory).thenReturn(MutableStateFlow(emptyList()))
        whenever(dietRepository.totalCaloriesToday).thenReturn(MutableStateFlow(0))
        
        // Mock suspend functions properly
        runBlocking {
            whenever(userRepository.updateCredits(any())).thenReturn(true)
        }

        viewModel = AiCoachViewModel(
            userRepository = userRepository,
            workoutRepository = workoutRepository,
            dietRepository = dietRepository,
            apiRepository = apiRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun sendMessage_whenNoActiveApis_showsErrorMessage() = runTest {
        // Given No APIs configured
        apiConfigsFlow.value = emptyList()
        
        // When sending a message
        viewModel.sendMessage("Hello")
        
        // Then an error message should be displayed
        val currentMessages = viewModel.messages.value
        assertTrue("Error message should be present", 
            currentMessages.any { it.text.contains("currently unavailable") })
        verify(userRepository, never()).updateCredits(any())
    }

    @Test
    fun sendMessage_whenActiveApisExist_proceedsToCreditCheck() = runTest {
        // Given active API exists
        apiConfigsFlow.value = listOf(ApiConfig(id = "1", providerName = "Gemini", isActive = true))
        
        // When sending a message
        viewModel.sendMessage("Hello")
        
        // Then it should NOT show the unavailable message
        val currentMessages = viewModel.messages.value
        assertFalse("Unavailable message should NOT be present", 
            currentMessages.any { it.text.contains("currently unavailable") })
        
        // Verify it checked credits
        verify(userRepository).updateCredits(eq(1))
    }
}
