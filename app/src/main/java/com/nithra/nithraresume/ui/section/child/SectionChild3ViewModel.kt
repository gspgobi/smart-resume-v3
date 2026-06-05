package com.nithra.nithraresume.ui.section.child


import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nithra.nithraresume.data.model.SectionChild3
import com.nithra.nithraresume.data.model.SectionHeadAdded
import com.nithra.nithraresume.data.repository.SectionChildRepository
import com.nithra.nithraresume.data.repository.SectionHeadRepository
import com.nithra.nithraresume.utils.AnalyticsManager
import com.nithra.nithraresume.utils.MAX_CHILD_ITEMS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SectionChild3ViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sectionHeadRepository: SectionHeadRepository,
    private val sectionChildRepository: SectionChildRepository,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    val sectionHeadAddedId: Int = checkNotNull(savedStateHandle["sectionHeadAddedId"])

    private val _sha = MutableStateFlow<SectionHeadAdded?>(null)
    val sha: StateFlow<SectionHeadAdded?> = _sha.asStateFlow()

    val items: StateFlow<List<SectionChild3>> = sectionChildRepository
        .getChild3List(sectionHeadAddedId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _snackbar = MutableStateFlow<String?>(null)
    val snackbar: StateFlow<String?> = _snackbar.asStateFlow()

    fun clearSnackbar() { _snackbar.value = null }

    init {
        viewModelScope.launch {
            _sha.value = sectionHeadRepository.getAddedById(sectionHeadAddedId)
        }
    }

    fun saveTitle(title: String) {
        viewModelScope.launch {
            sectionHeadRepository.updateAddedTitle(sectionHeadAddedId, title)
            _sha.value = sectionHeadRepository.getAddedById(sectionHeadAddedId)
            analyticsManager.logSc3SaveTitle()
        }
    }

    fun canAddItem(currentCount: Int): Boolean = currentCount < MAX_CHILD_ITEMS

    fun deleteItem(item: SectionChild3, allItems: List<SectionChild3>) {
        viewModelScope.launch {
            try {
                allItems.filter { it.indexPosition > item.indexPosition }
                    .forEach { sectionChildRepository.updateChild3Position(it.id, it.indexPosition - 1) }
                sectionChildRepository.deleteChild3(item)
                analyticsManager.logSc3DeleteDetail()
            } catch (e: Exception) {
                _snackbar.value = "Failed to delete item"
            }
        }
    }
}
