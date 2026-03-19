package com.example.fitjourney

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.fitjourney.data.manager.AuthSession
import com.example.fitjourney.ui.navigation.FitJourneyNavGraph
import com.example.fitjourney.ui.theme.FitJourneyTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.Executor

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = (application as FitJourneyApplication).container
        val userPreferences = appContainer.userPreferences

        lifecycleScope.launch {
            val isLockEnabled = userPreferences.biometricLockEnabled.first()
            if (isLockEnabled && !AuthSession.isUnlocked) {
                showBiometricPrompt()
            } else {
                startApp()
            }
        }
    }

    private fun showBiometricPrompt() {
        val biometricManager = BiometricManager.from(this)
        val executor = ContextCompat.getMainExecutor(this)
        
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // User requested: "On failure or dismissal, finish the activity."
                    finish()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    AuthSession.isUnlocked = true
                    startApp()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                }
            })

        val promptInfoBuilder = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Verify it's you")
            .setSubtitle("Authenticate to access FitJourney")

        // check if biometrics are available
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG
        val canAuthenticate = biometricManager.canAuthenticate(authenticators)

        if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            promptInfoBuilder.setNegativeButtonText("Use PIN")
        } else {
            // Fallback gracefully to PIN if biometrics unavailable
            promptInfoBuilder.setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
        }

        biometricPrompt.authenticate(promptInfoBuilder.build())
    }

    private fun startApp() {
        setContent {
            FitJourneyTheme {
                val navController = rememberNavController()
                FitJourneyNavGraph(navController = navController)
            }
        }
    }
}
