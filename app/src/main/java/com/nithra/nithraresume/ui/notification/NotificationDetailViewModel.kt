package com.nithra.nithraresume.ui.notification

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nithra.nithraresume.data.model.FcmData
import com.nithra.nithraresume.data.repository.FcmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface NotificationDetailUiState {
    data object Loading : NotificationDetailUiState
    data class Ready(val item: FcmData) : NotificationDetailUiState
    data object NotFound : NotificationDetailUiState
}

@HiltViewModel
class NotificationDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val fcmRepository: FcmRepository
) : ViewModel() {

    private val notificationId: Int = checkNotNull(savedStateHandle["notificationId"])

    private val _uiState = MutableStateFlow<NotificationDetailUiState>(NotificationDetailUiState.Loading)
    val uiState: StateFlow<NotificationDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val item = fcmRepository.getById(notificationId)
            _uiState.value = if (item != null) NotificationDetailUiState.Ready(item)
                             else NotificationDetailUiState.NotFound
        }
    }

    fun markAsRead() {
        viewModelScope.launch { fcmRepository.markAsRead(notificationId) }
    }
}
// update 126
