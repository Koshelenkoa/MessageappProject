package com.example.myapplication.util

import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo


class ForegroundCheckTask {
    companion object {

        val foreground: Boolean = foregrounded()

        /**
         * called to get the state of the all activities
         */
        fun foregrounded(): Boolean {
            val appProcessInfo: RunningAppProcessInfo =
                RunningAppProcessInfo()
            ActivityManager.getMyMemoryState(appProcessInfo)
            return (appProcessInfo.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                    || appProcessInfo.importance == RunningAppProcessInfo.IMPORTANCE_VISIBLE)
        }

    }
}