package com.nithra.nithraresume.ui.format


import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nithra.nithraresume.data.model.ResumeFormat
import com.nithra.nithraresume.data.model.UserProfile
import com.nithra.nithraresume.data.repository.ResumeFormatRepository
import com.nithra.nithraresume.data.repository.UserProfileRepository
import com.nithra.nithraresume.utils.AnalyticsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.util.Log
import javax.inject.Inject

sealed interface ResumeFormatUiState {
    data object Loading : ResumeFormatUiState
    data object Ready : ResumeFormatUiState
    data object Saved : ResumeFormatUiState
    data class Error(val message: String) : ResumeFormatUiState
}

@HiltViewModel
class ResumeFormatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val userProfileRepository: UserProfileRepository,
    private val resumeFormatRepository: ResumeFormatRepository,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    val profileId: Int = checkNotNull(savedStateHandle["profileId"])

    private val _uiState = MutableStateFlow<ResumeFormatUiState>(ResumeFormatUiState.Loading)
    val uiState: StateFlow<ResumeFormatUiState> = _uiState.asStateFlow()

    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile.asStateFlow()

    val formats: StateFlow<List<ResumeFormat>> = resumeFormatRepository
        .getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun resetState() { _uiState.value = ResumeFormatUiState.Ready }

    init {
        viewModelScope.launch {
            _profile.value = userProfileRepository.getById(profileId)
            _uiState.value = ResumeFormatUiState.Ready
        }
    }

    fun save(
        formatId: Int,
        fontStyle: String,
        fontSize: Int,
        backgroundColor: String
    ) {
        viewModelScope.launch {
            try {
                userProfileRepository.updateFormatSettings(
                    profileId, formatId, fontStyle, fontSize, backgroundColor
                )
                analyticsManager.logRfFormatSelect(formatId)
                _uiState.value = ResumeFormatUiState.Saved
            } catch (e: Exception) {
                Log.e(TAG, "save", e)
                _uiState.value = ResumeFormatUiState.Error(e.message ?: "Save failed")
            }
        }
    }

    fun onPreviewClicked(formatId: Int) { analyticsManager.logRfFormatPreview(formatId) }

    private companion object { const val TAG = "ResumeFormatVM" }
}
