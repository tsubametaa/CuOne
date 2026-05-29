package com.example.cuan.core.di

import android.content.Context
import com.example.cuan.core.local.AppDataStore
import com.example.cuan.core.local.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing app-level dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDataStore(
        @ApplicationContext context: Context
    ): AppDataStore {
        return AppDataStore(context)
    }

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return AppDatabase.getInstance(context)
    }
}