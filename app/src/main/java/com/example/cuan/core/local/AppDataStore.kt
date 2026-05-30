package com.example.cuan.core.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "c_one_prefs")

// DataStore keys for CuOne app preferences 
object DataStoreKeys {
    // Onboarding
    val USER_NAME = stringPreferencesKey("user_name")
    val IS_ONBOARDING_DONE = booleanPreferencesKey("is_onboarding_done")

    // Profil lengkap
    val USER_OCCUPATION = stringPreferencesKey("user_occupation")
    val USER_INCOME_RANGE = stringPreferencesKey("user_income_range")
    val USER_MONTHLY_BUDGET = longPreferencesKey("user_monthly_budget")
    val IS_PROFILE_COMPLETE = booleanPreferencesKey("is_profile_complete")

    // Koneksi Sheets
    val SHEETS_URL = stringPreferencesKey("sheets_url")
    val SHEETS_ID = stringPreferencesKey("sheets_id")
    val IS_SHEETS_CONNECTED = booleanPreferencesKey("is_sheets_connected")
    val LAST_SYNC_AT = longPreferencesKey("last_sync_at")

    // AI
    val OPENROUTER_API_KEY = stringPreferencesKey("openrouter_api_key")
    val GOOGLE_ACCESS_TOKEN = stringPreferencesKey("google_access_token")

    // Notifikasi & preferensi
    val DAILY_REMINDER_ENABLED = booleanPreferencesKey("daily_reminder_enabled")
    val DAILY_REMINDER_HOUR = intPreferencesKey("daily_reminder_hour")
}

@Singleton
class AppDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    // ========== Onboarding ==========
    val userName: Flow<String> = dataStore.data
        .catch { exception -> if (exception is IOException) emit(androidx.datastore.preferences.core.emptyPreferences()) else throw exception }
        .map { it[DataStoreKeys.USER_NAME] ?: "" }

    val isOnboardingDone: Flow<Boolean> = dataStore.data
        .catch { exception -> if (exception is IOException) emit(androidx.datastore.preferences.core.emptyPreferences()) else throw exception }
        .map { it[DataStoreKeys.IS_ONBOARDING_DONE] ?: false }

    suspend fun saveUserName(name: String) {
        dataStore.edit { it[DataStoreKeys.USER_NAME] = name }
    }

    suspend fun setOnboardingDone(done: Boolean) {
        dataStore.edit { it[DataStoreKeys.IS_ONBOARDING_DONE] = done }
    }

    // ========== Profile ==========
    val userOccupation: Flow<String> = dataStore.data.map { it[DataStoreKeys.USER_OCCUPATION] ?: "" }
    val userIncomeRange: Flow<String> = dataStore.data.map { it[DataStoreKeys.USER_INCOME_RANGE] ?: "" }
    val userMonthlyBudget: Flow<Long> = dataStore.data.map { it[DataStoreKeys.USER_MONTHLY_BUDGET] ?: 0L }
    val isProfileComplete: Flow<Boolean> = dataStore.data.map { it[DataStoreKeys.IS_PROFILE_COMPLETE] ?: false }

    suspend fun saveUserOccupation(occupation: String) {
        dataStore.edit { it[DataStoreKeys.USER_OCCUPATION] = occupation }
    }

    suspend fun saveUserIncomeRange(incomeRange: String) {
        dataStore.edit { it[DataStoreKeys.USER_INCOME_RANGE] = incomeRange }
    }

    suspend fun saveUserMonthlyBudget(budget: Long) {
        dataStore.edit { it[DataStoreKeys.USER_MONTHLY_BUDGET] = budget }
    }

    suspend fun setProfileComplete(complete: Boolean) {
        dataStore.edit { it[DataStoreKeys.IS_PROFILE_COMPLETE] = complete }
    }

    // ========== Sheets Connection ==========
    val sheetsUrl: Flow<String> = dataStore.data.map { it[DataStoreKeys.SHEETS_URL] ?: "" }
    val sheetsId: Flow<String> = dataStore.data.map { it[DataStoreKeys.SHEETS_ID] ?: "" }
    val isSheetsConnected: Flow<Boolean> = dataStore.data.map { it[DataStoreKeys.IS_SHEETS_CONNECTED] ?: false }
    val lastSyncAt: Flow<Long> = dataStore.data.map { it[DataStoreKeys.LAST_SYNC_AT] ?: 0L }
    val googleAccessToken: Flow<String> = dataStore.data.map { it[DataStoreKeys.GOOGLE_ACCESS_TOKEN] ?: "" }

    suspend fun saveSheetsUrl(url: String) {
        dataStore.edit { it[DataStoreKeys.SHEETS_URL] = url }
    }

    suspend fun saveSheetsId(id: String) {
        dataStore.edit { it[DataStoreKeys.SHEETS_ID] = id }
    }

    suspend fun setSheetsConnected(connected: Boolean) {
        dataStore.edit { it[DataStoreKeys.IS_SHEETS_CONNECTED] = connected }
    }

    suspend fun setLastSyncAt(timestamp: Long) {
        dataStore.edit { it[DataStoreKeys.LAST_SYNC_AT] = timestamp }
    }

    suspend fun saveGoogleAccessToken(token: String) {
        dataStore.edit { it[DataStoreKeys.GOOGLE_ACCESS_TOKEN] = token }
    }

    // ========== AI ==========
    val openRouterApiKey: Flow<String> = dataStore.data.map { it[DataStoreKeys.OPENROUTER_API_KEY] ?: "" }

    suspend fun saveOpenRouterApiKey(apiKey: String) {
        dataStore.edit { it[DataStoreKeys.OPENROUTER_API_KEY] = apiKey }
    }

    // ========== Notifications ==========
    val dailyReminderEnabled: Flow<Boolean> = dataStore.data.map { it[DataStoreKeys.DAILY_REMINDER_ENABLED] ?: false }
    val dailyReminderHour: Flow<Int> = dataStore.data.map { it[DataStoreKeys.DAILY_REMINDER_HOUR] ?: 20 }

    suspend fun setDailyReminderEnabled(enabled: Boolean) {
        dataStore.edit { it[DataStoreKeys.DAILY_REMINDER_ENABLED] = enabled }
    }

    suspend fun setDailyReminderHour(hour: Int) {
        dataStore.edit { it[DataStoreKeys.DAILY_REMINDER_HOUR] = hour }
    }
}