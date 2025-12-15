package com.nithra.nithraresume.ui.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nithra.nithraresume.data.model.FcmData
import com.nithra.nithraresume.data.repository.FcmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationListViewModel @Inject constructor(
    private val fcmRepository: FcmRepository
) : ViewModel() {

    val notifications: StateFlow<List<FcmData>> = fcmRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun delete(item: FcmData) {
        viewModelScope.launch { fcmRepository.delete(item) }
    }

    fun deleteAll() {
        viewModelScope.launch { fcmRepository.deleteAll() }
    }
}
// update 128
