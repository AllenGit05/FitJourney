package com.example.fitjourneyag.di

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.fitjourneyag.FitJourneyApplication
import com.example.fitjourneyag.data.repository.AuthRepositoryImpl
import com.example.fitjourneyag.domain.repository.AuthRepository
import com.example.fitjourneyag.ui.auth.AuthViewModel
import com.example.fitjourneyag.ui.auth.ForgotPasswordViewModel
import com.example.fitjourneyag.ui.auth.SignUpViewModel
import kotlinx.coroutines.*

interface AppContainer {
    val authRepository: AuthRepository
    val workoutRepository: com.example.fitjourneyag.domain.repository.WorkoutRepository
    val dietRepository: com.example.fitjourneyag.domain.repository.DietRepository
    val progressRepository: com.example.fitjourneyag.domain.repository.ProgressRepository
    val weeklyReportRepository: com.example.fitjourneyag.domain.repository.WeeklyReportRepository
    val habitRepository: com.example.fitjourneyag.domain.repository.HabitRepository
    val userRepository: com.example.fitjourneyag.domain.repository.UserRepository
    val apiRepository: com.example.fitjourneyag.domain.repository.ApiRepository
    val waterRepository: com.example.fitjourneyag.domain.repository.WaterRepository
    val chatRepository: com.example.fitjourneyag.domain.repository.ChatRepository
    val apiKeyStore: com.example.fitjourneyag.data.local.ApiKeyStore
}

class DefaultAppContainer(private val context: android.content.Context) : AppContainer {
    private val database: com.example.fitjourneyag.data.local.FitJourneyDatabase by lazy {
        com.example.fitjourneyag.data.local.FitJourneyDatabase.getDatabase(context)
    }

    override val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(context)
    }
    
    override val workoutRepository: com.example.fitjourneyag.domain.repository.WorkoutRepository by lazy {
        com.example.fitjourneyag.data.repository.WorkoutRepositoryImpl(database.workoutDao())
    }
    
    override val dietRepository: com.example.fitjourneyag.domain.repository.DietRepository by lazy {
        com.example.fitjourneyag.data.repository.DietRepositoryImpl(database.dietDao())
    }
    
    override val progressRepository: com.example.fitjourneyag.domain.repository.ProgressRepository by lazy {
        com.example.fitjourneyag.data.repository.ProgressRepositoryImpl(
            database.stepDao(),
            database.weightDao(),
            database.photoDao(),
            database.bodyMeasurementDao()
        )
    }
    
    override val weeklyReportRepository: com.example.fitjourneyag.domain.repository.WeeklyReportRepository by lazy {
        com.example.fitjourneyag.data.repository.WeeklyReportRepositoryImpl(database.weeklyReportDao())
    }
    
    override val habitRepository: com.example.fitjourneyag.domain.repository.HabitRepository by lazy {
        com.example.fitjourneyag.data.repository.HabitRepositoryImpl(database.habitDao())
    }
    
    override val userRepository: com.example.fitjourneyag.domain.repository.UserRepository by lazy {
        com.example.fitjourneyag.data.repository.UserRepositoryImpl(authRepository, database.userDao())
    }
    
    override val apiRepository: com.example.fitjourneyag.domain.repository.ApiRepository by lazy {
        com.example.fitjourneyag.data.repository.ApiRepositoryImpl(aiEngine)
    }

    override val apiKeyStore: com.example.fitjourneyag.data.local.ApiKeyStore by lazy {
        com.example.fitjourneyag.data.local.ApiKeyStore(context)
    }

    private val groqApiClient by lazy {
        com.example.fitjourneyag.data.remote.GroqApiClient()
    }

    private val aiEngine by lazy {
        com.example.fitjourneyag.data.remote.AiEngine(apiKeyStore, groqApiClient)
    }

    override val waterRepository: com.example.fitjourneyag.domain.repository.WaterRepository by lazy {
        com.example.fitjourneyag.data.repository.WaterRepositoryImpl(database.waterDao())
    }

    override val chatRepository: com.example.fitjourneyag.domain.repository.ChatRepository by lazy {
        com.example.fitjourneyag.data.repository.ChatRepositoryImpl(database.chatDao())
    }

    init {
        // Seed prototype data for testing
        CoroutineScope(Dispatchers.IO).launch {
            com.example.fitjourneyag.data.repository.PrototypeDataSeeder(
                database.workoutDao(),
                database.dietDao(),
                database.stepDao(),
                database.weightDao(),
                database.habitDao()
            ).seedIfNeeded()
        }
    }
}

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            AuthViewModel(authRepository = fitJourneyApplication().container.authRepository)
        }
        initializer {
            SignUpViewModel(authRepository = fitJourneyApplication().container.authRepository)
        }
        initializer {
            ForgotPasswordViewModel()
        }
        initializer {
            com.example.fitjourneyag.ui.client.ClientDashboardViewModel(
                workoutRepository = fitJourneyApplication().container.workoutRepository,
                dietRepository = fitJourneyApplication().container.dietRepository,
                authRepository = fitJourneyApplication().container.authRepository,
                userRepository = fitJourneyApplication().container.userRepository,
                waterRepository = fitJourneyApplication().container.waterRepository,
                progressRepository = fitJourneyApplication().container.progressRepository,
                apiRepository = fitJourneyApplication().container.apiRepository
            )
        }
        initializer {
            com.example.fitjourneyag.ui.client.diet.DietTrackingViewModel(
                dietRepository = fitJourneyApplication().container.dietRepository,
                apiRepository = fitJourneyApplication().container.apiRepository
            )
        }
        initializer {
            com.example.fitjourneyag.ui.client.workout.WorkoutTrackingViewModel(
                workoutRepository = fitJourneyApplication().container.workoutRepository,
                authRepository = fitJourneyApplication().container.authRepository,
                apiRepository = fitJourneyApplication().container.apiRepository
            )
        }
        initializer {
            com.example.fitjourneyag.ui.client.calculators.CalculatorsViewModel()
        }
        initializer {
            com.example.fitjourneyag.ui.client.progress.ProgressViewModel(
                workoutRepository = fitJourneyApplication().container.workoutRepository,
                progressRepository = fitJourneyApplication().container.progressRepository
            )
        }
        initializer {
            com.example.fitjourneyag.ui.client.settings.SettingsViewModel(authRepository = fitJourneyApplication().container.authRepository)
        }
        initializer {
            com.example.fitjourneyag.ui.client.ai.AiCoachViewModel(
                userRepository = fitJourneyApplication().container.userRepository,
                workoutRepository = fitJourneyApplication().container.workoutRepository,
                dietRepository = fitJourneyApplication().container.dietRepository,
                apiRepository = fitJourneyApplication().container.apiRepository,
                chatRepository = fitJourneyApplication().container.chatRepository,
                progressRepository = fitJourneyApplication().container.progressRepository,
                waterRepository = fitJourneyApplication().container.waterRepository
            )
        }
        initializer {
            com.example.fitjourneyag.ui.client.workout.generator.WorkoutGeneratorViewModel(
                workoutRepository = fitJourneyApplication().container.workoutRepository,
                userRepository = fitJourneyApplication().container.userRepository
            )
        }
        initializer {
            com.example.fitjourneyag.ui.admin.AdminDashboardViewModel()
        }
        initializer {
            com.example.fitjourneyag.ui.admin.api.AdminApiManagementViewModel(
                apiKeyStore = fitJourneyApplication().container.apiKeyStore
            )
        }
        initializer {
            com.example.fitjourneyag.ui.admin.management.AdminManagementViewModel()
        }
        initializer {
            com.example.fitjourneyag.ui.client.insights.WeeklyInsightsViewModel(
                dietRepository = fitJourneyApplication().container.dietRepository,
                workoutRepository = fitJourneyApplication().container.workoutRepository,
                progressRepository = fitJourneyApplication().container.progressRepository,
                weeklyReportRepository = fitJourneyApplication().container.weeklyReportRepository,
                userRepository = fitJourneyApplication().container.userRepository,
                apiRepository = fitJourneyApplication().container.apiRepository,
                waterRepository = fitJourneyApplication().container.waterRepository
            )
        }
        initializer {
            com.example.fitjourneyag.ui.client.habits.HabitViewModel(
                habitRepository = fitJourneyApplication().container.habitRepository
            )
        }
        initializer {
            com.example.fitjourneyag.ui.client.track.TrackViewModel(
                userRepository = fitJourneyApplication().container.userRepository,
                waterRepository = fitJourneyApplication().container.waterRepository,
                progressRepository = fitJourneyApplication().container.progressRepository,
                apiRepository = fitJourneyApplication().container.apiRepository
            )
        }
    }
}

fun CreationExtras.fitJourneyApplication(): FitJourneyApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as FitJourneyApplication)
