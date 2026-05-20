package com.nithra.nithraresume.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nithra.nithraresume.utils.PrefsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppSettingsViewModel @Inject constructor(
    private val prefsManager: PrefsManager
) : ViewModel() {

    val notificationsEnabled: StateFlow<Boolean> = prefsManager.v1NotificationsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { prefsManager.setV1NotificationsEnabled(enabled) }
    }
}
