package com.koshelenkoa.securemessaging

import android.app.Application
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