package com.example.cuan.core.di

import android.content.Context
import com.example.cuan.core.local.AppDataStore
import com.example.cuan.core.local.AppDatabase
import com.example.cuan.core.network.OpenRouterApiService
import com.example.cuan.data.repository.AIRepository
import com.example.cuan.data.repository.AIRepositoryImpl
import com.example.cuan.data.repository.TransactionRepository
import com.example.cuan.data.repository.TransactionRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
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

    @Provides
    @Singleton
    fun provideOpenRouterApiService(): OpenRouterApiService {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl("https://openrouter.ai/")
            .client(okHttpClient)
            .build()
            .create(OpenRouterApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideTransactionRepository(
        database: AppDatabase
    ): TransactionRepository {
        return TransactionRepositoryImpl(database.transactionQueueDao())
    }

    @Provides
    @Singleton
    fun provideAIRepository(
        apiService: OpenRouterApiService
    ): AIRepository {
        return AIRepositoryImpl(apiService)
    }
}