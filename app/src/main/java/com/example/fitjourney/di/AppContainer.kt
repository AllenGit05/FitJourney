package com.example.fitjourney.di

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.fitjourney.FitJourneyApplication
import com.example.fitjourney.data.repository.AuthRepositoryImpl
import com.example.fitjourney.domain.repository.AuthRepository
import com.example.fitjourney.ui.auth.AuthViewModel
import com.example.fitjourney.ui.auth.ForgotPasswordViewModel
import com.example.fitjourney.ui.auth.SignUpViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*

interface AppContainer {
    val authRepository: AuthRepository
    val workoutRepository: com.example.fitjourney.domain.repository.WorkoutRepository
    val dietRepository: com.example.fitjourney.domain.repository.DietRepository
    val progressRepository: com.example.fitjourney.domain.repository.ProgressRepository
    val weeklyReportRepository: com.example.fitjourney.domain.repository.WeeklyReportRepository
    val habitRepository: com.example.fitjourney.domain.repository.HabitRepository
    val userRepository: com.example.fitjourney.domain.repository.UserRepository
    val apiRepository: com.example.fitjourney.domain.repository.ApiRepository
    val waterRepository: com.example.fitjourney.domain.repository.WaterRepository
    val chatRepository: com.example.fitjourney.domain.repository.ChatRepository
    val apiKeyStore: com.example.fitjourney.data.local.ApiKeyStore
    val syncManager: com.example.fitjourney.data.sync.SyncManager
    val firebaseStorageRepository: com.example.fitjourney.data.remote.FirebaseStorageRepository
    val adminConfig: com.example.fitjourney.data.local.AdminConfig
    val userPreferences: com.example.fitjourney.data.local.UserPreferences
    val workoutReminderManager: com.example.fitjourney.data.manager.WorkoutReminderManager
    val exportManager: com.example.fitjourney.data.manager.ExportManager
}

class DefaultAppContainer(private val context: android.content.Context) : AppContainer {
    private val database: com.example.fitjourney.data.local.FitJourneyDatabase by lazy {
        com.example.fitjourney.data.local.FitJourneyDatabase.getDatabase(context)
    }

    private val fireauth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val firestorage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }

    override val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(context, fireauth, firestore, database.userDao(), adminConfig, database)
    }
    
    override val workoutRepository: com.example.fitjourney.domain.repository.WorkoutRepository by lazy {
        com.example.fitjourney.data.repository.WorkoutRepositoryImpl(database.workoutDao(), syncManager, context)
    }
    
    override val dietRepository: com.example.fitjourney.domain.repository.DietRepository by lazy {
        com.example.fitjourney.data.repository.DietRepositoryImpl(database.dietDao(), syncManager)
    }
    
    override val progressRepository: com.example.fitjourney.domain.repository.ProgressRepository by lazy {
        com.example.fitjourney.data.repository.ProgressRepositoryImpl(
            context,
            database.stepDao(),
            database.weightDao(),
            database.photoDao(),
            database.bodyMeasurementDao(),
            firebaseStorageRepository,
            fireauth,
            syncManager
        )
    }
    
    override val weeklyReportRepository: com.example.fitjourney.domain.repository.WeeklyReportRepository by lazy {
        com.example.fitjourney.data.repository.WeeklyReportRepositoryImpl(database.weeklyReportDao(), syncManager)
    }
    
    override val habitRepository: com.example.fitjourney.domain.repository.HabitRepository by lazy {
        com.example.fitjourney.data.repository.HabitRepositoryImpl(database.habitDao(), syncManager)
    }
    
    override val userRepository: com.example.fitjourney.domain.repository.UserRepository by lazy {
        com.example.fitjourney.data.repository.UserRepositoryImpl(authRepository, firebaseStorageRepository, database.userDao())
    }

    
    override val apiRepository: com.example.fitjourney.domain.repository.ApiRepository by lazy {
        com.example.fitjourney.data.repository.ApiRepositoryImpl(aiEngine)
    }

    override val apiKeyStore: com.example.fitjourney.data.local.ApiKeyStore by lazy {
        com.example.fitjourney.data.local.ApiKeyStore(context)
    }

    private val groqApiClient by lazy {
        com.example.fitjourney.data.remote.GroqApiClient()
    }

    private val aiEngine by lazy {
        com.example.fitjourney.data.remote.AiEngine(apiKeyStore, groqApiClient)
    }

    override val waterRepository: com.example.fitjourney.domain.repository.WaterRepository by lazy {
        com.example.fitjourney.data.repository.WaterRepositoryImpl(database.waterDao(), syncManager)
    }

    override val chatRepository: com.example.fitjourney.domain.repository.ChatRepository by lazy {
        com.example.fitjourney.data.repository.ChatRepositoryImpl(database.chatDao(), syncManager)
    }

    override val syncManager: com.example.fitjourney.data.sync.SyncManager by lazy {
        com.example.fitjourney.data.sync.SyncManager(context, database, firestore)
    }

    override val firebaseStorageRepository: com.example.fitjourney.data.remote.FirebaseStorageRepository by lazy {
        com.example.fitjourney.data.remote.FirebaseStorageRepository(firestorage)
    }

    override val adminConfig: com.example.fitjourney.data.local.AdminConfig by lazy {
        com.example.fitjourney.data.local.AdminConfig(context)
    }

    override val userPreferences: com.example.fitjourney.data.local.UserPreferences by lazy {
        com.example.fitjourney.data.local.UserPreferences(context)
    }

    override val workoutReminderManager: com.example.fitjourney.data.manager.WorkoutReminderManager by lazy {
        com.example.fitjourney.data.manager.WorkoutReminderManager(context)
    }

    override val exportManager: com.example.fitjourney.data.manager.ExportManager by lazy {
        com.example.fitjourney.data.manager.ExportManager(context, workoutRepository, dietRepository, progressRepository, waterRepository)
    }
}

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            com.example.fitjourney.ui.MainViewModel(
                authRepository = fitJourneyApplication().container.authRepository
            )
        }

        initializer {
            AuthViewModel(authRepository = fitJourneyApplication().container.authRepository)
        }
        initializer {
            SignUpViewModel(
                authRepository = fitJourneyApplication().container.authRepository,
                adminConfig = fitJourneyApplication().container.adminConfig
            )
        }

        initializer {
            com.example.fitjourney.ui.admin.AdminProfileViewModel(
                authRepository = fitJourneyApplication().container.authRepository,
                adminConfig = fitJourneyApplication().container.adminConfig
            )
        }
        initializer {
            ForgotPasswordViewModel(
                authRepository = fitJourneyApplication().container.authRepository
            )
        }
        initializer {
            com.example.fitjourney.ui.client.ClientDashboardViewModel(
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
            com.example.fitjourney.ui.client.diet.DietTrackingViewModel(
                dietRepository = fitJourneyApplication().container.dietRepository,
                apiRepository = fitJourneyApplication().container.apiRepository
            )
        }
        initializer {
            com.example.fitjourney.ui.client.workout.WorkoutTrackingViewModel(
                workoutRepository = fitJourneyApplication().container.workoutRepository,
                authRepository = fitJourneyApplication().container.authRepository,
                apiRepository = fitJourneyApplication().container.apiRepository
            )
        }
        initializer {
            com.example.fitjourney.ui.client.calculators.CalculatorsViewModel()
        }
        initializer {
            com.example.fitjourney.ui.client.progress.ProgressViewModel(
                workoutRepository = fitJourneyApplication().container.workoutRepository,
                progressRepository = fitJourneyApplication().container.progressRepository
            )
        }
        initializer {
            com.example.fitjourney.ui.client.settings.SettingsViewModel(
                authRepository = fitJourneyApplication().container.authRepository
            )
        }

        initializer {
            com.example.fitjourney.ui.client.ai.AiCoachViewModel(
                userRepository = fitJourneyApplication().container.userRepository,
                workoutRepository = fitJourneyApplication().container.workoutRepository,
                dietRepository = fitJourneyApplication().container.dietRepository,
                apiRepository = fitJourneyApplication().container.apiRepository,
                chatRepository = fitJourneyApplication().container.chatRepository,
                progressRepository = fitJourneyApplication().container.progressRepository,
                waterRepository = fitJourneyApplication().container.waterRepository,
                habitRepository = fitJourneyApplication().container.habitRepository
            )
        }
        initializer {
            com.example.fitjourney.ui.client.workout.generator.WorkoutGeneratorViewModel(
                workoutRepository = fitJourneyApplication().container.workoutRepository,
                userRepository = fitJourneyApplication().container.userRepository,
                apiRepository = fitJourneyApplication().container.apiRepository
            )
        }
        initializer {
            com.example.fitjourney.ui.admin.AdminDashboardViewModel(
                syncManager = fitJourneyApplication().container.syncManager
            )
        }
        initializer {
            com.example.fitjourney.ui.admin.api.AdminApiManagementViewModel(
                apiKeyStore = fitJourneyApplication().container.apiKeyStore
            )
        }
        initializer {
            com.example.fitjourney.ui.admin.management.AdminManagementViewModel(
                firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            )
        }
        initializer {
            com.example.fitjourney.ui.client.insights.WeeklyInsightsViewModel(
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
            com.example.fitjourney.ui.client.habits.HabitViewModel(
                habitRepository = fitJourneyApplication().container.habitRepository
            )
        }
        initializer {
            com.example.fitjourney.ui.client.track.TrackViewModel(
                userRepository = fitJourneyApplication().container.userRepository,
                waterRepository = fitJourneyApplication().container.waterRepository,
                progressRepository = fitJourneyApplication().container.progressRepository,
                apiRepository = fitJourneyApplication().container.apiRepository
            )
        }
        initializer {
            com.example.fitjourney.ui.admin.diagnostic.FirebaseDiagnosticViewModel(
                application = fitJourneyApplication(),
                authRepository = fitJourneyApplication().container.authRepository,
                workoutRepository = fitJourneyApplication().container.workoutRepository,
                dietRepository = fitJourneyApplication().container.dietRepository,
                waterRepository = fitJourneyApplication().container.waterRepository,
                progressRepository = fitJourneyApplication().container.progressRepository,
                habitRepository = fitJourneyApplication().container.habitRepository,
                firebaseStorageRepository = fitJourneyApplication().container.firebaseStorageRepository,
                syncManager = fitJourneyApplication().container.syncManager
            )
        }
    }
}

fun CreationExtras.fitJourneyApplication(): FitJourneyApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as FitJourneyApplication)
