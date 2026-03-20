package com.example.fitjourney.ui.client.workout.generator

import com.example.fitjourney.domain.model.User
import com.example.fitjourney.domain.repository.ApiRepository
import com.example.fitjourney.domain.repository.UserRepository
import com.example.fitjourney.domain.repository.WorkoutRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutGeneratorViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    
    private lateinit var viewModel: WorkoutGeneratorViewModel
    private val userRepository: UserRepository = mock()
    private val workoutRepository: WorkoutRepository = mock()
    private val apiRepository: ApiRepository = mock()
    
    private val userFlow = MutableStateFlow<User?>(null)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        whenever(userRepository.userProfile).thenReturn(userFlow)
        
        viewModel = WorkoutGeneratorViewModel(
            workoutRepository = workoutRepository,
            userRepository = userRepository,
            apiRepository = apiRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun generatePlan_insufficientCredits_showsError() = runTest {
        userFlow.value = User(uid = "user1", aiCredits = 2, isPremium = false)
        runCurrent()
        
        viewModel.generatePlan()
        runCurrent()
        
        val state = viewModel.uiState.value
        assertTrue("State should be Error, but was $state", state is GeneratorUiState.Error)
        assertEquals("Insufficient credits. 3 credits required.", (state as GeneratorUiState.Error).message)
    }

    @Test
    fun generatePlan_successfulResponse_deductsCredits() = runTest {
        userFlow.value = User(uid = "user1", aiCredits = 10, isPremium = false)
        runCurrent()
        
        val mockJson = """{ "weeklySchedule": [ { "dayName": "D1", "exercises": [ { "name": "E1", "sets": 3, "reps": "10", "restTimeSeconds": 60 } ] } ] }"""
        whenever(apiRepository.generateContent(any())).thenReturn(mockJson)
        whenever(userRepository.updateCredits(any())).thenReturn(true)
        
        viewModel.generatePlan()
        runCurrent()
        
        val state = viewModel.uiState.value
        if (state is GeneratorUiState.Error) {
            println("Test failed with error: ${state.message}")
        }
        assertTrue("State should be Success, but was $state", state is GeneratorUiState.Success)
        verify(userRepository).updateCredits(eq(3))
    }

    @Test
    fun generatePlan_malformedJson_showsErrorAndNoDeduction() = runTest {
        userFlow.value = User(uid = "user1", aiCredits = 10, isPremium = false)
        runCurrent()
        
        whenever(apiRepository.generateContent(any())).thenReturn("Invalid JSON")
        
        viewModel.generatePlan()
        runCurrent()
        
        val state = viewModel.uiState.value
        assertTrue("State should be Error, but was $state", state is GeneratorUiState.Error)
        assertEquals("Could not parse the AI response. Please try again.", (state as GeneratorUiState.Error).message)
        verify(userRepository, never()).updateCredits(any())
    }

    @Test
    fun generatePlan_apiFailure_showsError() = runTest {
        userFlow.value = User(uid = "user1", aiCredits = 10, isPremium = false)
        runCurrent()
        
        whenever(apiRepository.generateContent(any())).thenThrow(RuntimeException("Network error"))
        
        viewModel.generatePlan()
        runCurrent()
        
        val state = viewModel.uiState.value
        assertTrue("State should be Error, but was $state", state is GeneratorUiState.Error)
        assertEquals("Network error", (state as GeneratorUiState.Error).message)
    }
}
