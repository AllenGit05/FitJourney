package com.example.fitjourneyag

import android.app.Application
import com.example.fitjourneyag.di.AppContainer
import com.example.fitjourneyag.di.DefaultAppContainer

class FitJourneyApplication : Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
