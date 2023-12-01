package com.koshelenkoa.securemessaging.di

import android.content.Context
import com.koshelenkoa.securemessaging.data.local.room.ThisAppsDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DbModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): ThisAppsDatabase {
        return ThisAppsDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideMessageDao(db: ThisAppsDatabase) = db.messageDao()

    @Provides
    fun provideChatDao(db: ThisAppsDatabase) = db.chatDao()

}