package com.example.cuan.feature.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cuan.core.local.AppDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val appDataStore: AppDataStore
) : ViewModel() {

    val userName = appDataStore.userName
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    val isOnboardingDone = appDataStore.isOnboardingDone
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)
}