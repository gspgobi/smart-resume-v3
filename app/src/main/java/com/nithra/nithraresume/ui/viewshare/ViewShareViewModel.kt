package com.nithra.nithraresume.ui.viewshare

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nithra.nithraresume.data.model.UserProfile
import com.nithra.nithraresume.data.repository.UserProfileRepository
import com.nithra.nithraresume.utils.AdMobManager
import com.nithra.nithraresume.utils.DOT_PDF
import com.nithra.nithraresume.utils.GENERATE_COUNT_SHOW_AD
import com.nithra.nithraresume.utils.InterstitialAdHelper
import com.nithra.nithraresume.utils.PrefsManager
import com.nithra.nithraresume.utils.SrDir
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

sealed interface ViewShareUiState {
    data object Loading : ViewShareUiState
    data class Ready(val profile: UserProfile, val pdfFile: File?) : ViewShareUiState
}

@HiltViewModel
class ViewShareViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
    private val userProfileRepository: UserProfileRepository,
    private val prefsManager: PrefsManager
) : ViewModel() {

    val profileId: Int = checkNotNull(savedStateHandle["profileId"])
    val justGenerated: Boolean = savedStateHandle.get<Boolean>("justGenerated") ?: false

    val generateAdHelper = InterstitialAdHelper()

    private val _uiState = MutableStateFlow<ViewShareUiState>(ViewShareUiState.Loading)
    val uiState: StateFlow<ViewShareUiState> = _uiState.asStateFlow()

    private val _showGenerateAd = MutableStateFlow(false)
    val showGenerateAd: StateFlow<Boolean> = _showGenerateAd.asStateFlow()

    fun resetShowGenerateAd() { _showGenerateAd.value = false }

    init {
        if (justGenerated) {
            viewModelScope.launch {
                prefsManager.incrementResumeGeneratedCount()
                val count = prefsManager.resumeGeneratedCount.first()
                if (count % GENERATE_COUNT_SHOW_AD == 0) {
                    val loaded = generateAdHelper.loadSuspend(context, AdMobManager.interstitial02Id())
                    if (loaded) _showGenerateAd.value = true
                }
            }
        }
        load()
    }

    fun reload() { load() }

    private fun load() {
        viewModelScope.launch {
            val profile = userProfileRepository.getById(profileId) ?: return@launch
            val pdfFile = profile.resumeFileName
                ?.takeIf { it.isNotEmpty() }
                ?.let { name ->
                    File(context.getExternalFilesDir(null), "${SrDir.GENERATED_RESUME}/$name$DOT_PDF")
                        .takeIf { it.exists() }
                }
            _uiState.value = ViewShareUiState.Ready(profile, pdfFile)
        }
    }
}
