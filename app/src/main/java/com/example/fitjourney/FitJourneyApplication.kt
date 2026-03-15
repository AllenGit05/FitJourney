package com.example.fitjourney

import android.app.Application
import com.example.fitjourney.di.AppContainer
import com.example.fitjourney.di.DefaultAppContainer

class FitJourneyApplication : Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
