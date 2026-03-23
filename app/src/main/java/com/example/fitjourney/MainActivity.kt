package com.example.fitjourney

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.fitjourney.data.manager.AuthSession
import com.example.fitjourney.ui.navigation.FitJourneyNavGraph
import com.example.fitjourney.ui.theme.FitJourneyTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Always start the UI immediately — never block on biometrics
        startApp()

        // Then check biometric lock in background
        lifecycleScope.launch {
            try {
                val appContainer =
                    (application as FitJourneyApplication).container
                val userPreferences = appContainer.userPreferences
                val isLockEnabled =
                    userPreferences.biometricLockEnabled.first()

                if (isLockEnabled && !AuthSession.isUnlocked) {
                    showBiometricPrompt()
                }
            } catch (e: Exception) {
                // If anything fails with biometric setup,
                // just let the user in — don't crash
                e.printStackTrace()
            }
        }
    }

    private fun startApp() {
        setContent {
            FitJourneyTheme {
                val navController = rememberNavController()
                FitJourneyNavGraph(navController = navController)
            }
        }
    }

    private fun showBiometricPrompt() {
        try {
            val biometricManager = BiometricManager.from(this)
            val canAuthenticate = biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )

            if (canAuthenticate != BiometricManager.BIOMETRIC_SUCCESS) {
                // Device does not support biometrics — skip silently
                AuthSession.isUnlocked = true
                return
            }

            val executor = ContextCompat.getMainExecutor(this)
            val biometricPrompt = BiometricPrompt(this, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(
                        errorCode: Int,
                        errString: CharSequence
                    ) {
                        super.onAuthenticationError(errorCode, errString)
                        if (errorCode ==
                            BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                            errorCode ==
                            BiometricPrompt.ERROR_USER_CANCELED) {
                            finish()
                        }
                        // Any other error — let user in rather than
                        // locking them out permanently
                        AuthSession.isUnlocked = true
                    }

                    override fun onAuthenticationSucceeded(
                        result: BiometricPrompt.AuthenticationResult
                    ) {
                        super.onAuthenticationSucceeded(result)
                        AuthSession.isUnlocked = true
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        // Single failure — do not crash, let user retry
                    }
                }
            )

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Verify it's you")
                .setSubtitle("Authenticate to access FitJourney")
                .setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
                .build()

            biometricPrompt.authenticate(promptInfo)
        } catch (e: Exception) {
            // Biometric prompt failed to show — let user in
            AuthSession.isUnlocked = true
            e.printStackTrace()
        }
    }
}
