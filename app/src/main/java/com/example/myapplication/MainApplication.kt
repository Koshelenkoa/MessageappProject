package com.example.myapplication

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        sApplication = this
    }

    companion object {

        private var activityVisible: Boolean = false

        private lateinit var sApplication: Application

        fun getApplication(): Application = sApplication

        fun getContext(): Context = getApplication().applicationContext

        fun isActivityVisible(): Boolean {
            return activityVisible
        }

        fun activityResumed() {
            activityVisible = true
        }

        fun activityPaused() {
            activityVisible = false
        }


    }
}