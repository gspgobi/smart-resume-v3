package com.nithra.nithraresume.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nithra.nithraresume.data.api.ApiRepository
import com.nithra.nithraresume.data.repository.FcmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    fcmRepository: FcmRepository,
    private val apiRepository: ApiRepository
) : ViewModel() {

    /** Unread notification count — drives the bell badge in the TopAppBar. */
    val unreadNotificationCount: StateFlow<Int> = fcmRepository
        .getUnreadCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0
        )

    fun sendFeedback(email: String, feedback: String) {
        viewModelScope.launch {
            apiRepository.postFeedback(feedback = feedback, email = email)
        }
    }
}
