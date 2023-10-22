package com.example.myapplication.util

import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import java.util.Date

class DateTimeConverter {
    companion object {
        val systimezone = TimeZone.getDefault()

        //returns seconds for comparasion
        private fun getTimeNow(): Long = System.currentTimeMillis()


        fun getDay(timestamp: Long): String {
            val time: Long = timestamp * 1000
            val date = Date(time)
            var formatter: SimpleDateFormat = SimpleDateFormat("MMMM d")
            formatter.timeZone = systimezone
            return formatter.format(date)
        }

        fun getTime(timestamp: Long): String {
            val time: Long = timestamp * 1000
            val date = Date(time)
            var formatter: SimpleDateFormat = SimpleDateFormat("hh:mm a")
            formatter.timeZone = systimezone
            return formatter.format(date)
        }


        fun isToday(timestamp: String): Boolean {
            val time: Long = timestamp.toLong()
            return (getTimeNow() / 1000 - time) < 86400
        }

    }
}