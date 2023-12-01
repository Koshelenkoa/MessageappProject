package com.koshelenkoa.securemessaging.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.koshelenkoa.securemessaging.data.local.Chat
import com.koshelenkoa.securemessaging.data.local.Message

@Database(
    entities = [Message::class, Chat::class],
    version = 1
)
abstract class ThisAppsDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun chatDao(): ChatDao

    companion object {

        @Volatile
        private var instance: ThisAppsDatabase? = null

        fun getInstance(context: Context): ThisAppsDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        fun buildDatabase(context: Context): ThisAppsDatabase {
            return Room.databaseBuilder(
                context,
                ThisAppsDatabase::class.java,
                context.filesDir.absolutePath + "/app_database"
            ).build()
        }
    }
}

