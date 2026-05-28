package com.nithra.nithraresume.ui.section.head


import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nithra.nithraresume.data.model.SectionHeadAdded
import com.nithra.nithraresume.data.repository.SectionHeadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReorderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sectionHeadRepository: SectionHeadRepository
) : ViewModel() {

    val profileId: Int = checkNotNull(savedStateHandle["profileId"])
    val groupId: Int   = checkNotNull(savedStateHandle["groupId"])

    val items: StateFlow<List<SectionHeadAdded>> = sectionHeadRepository
        .getAddedByProfileId(profileId)
        .map { list -> list.filter { it.groupBaseId == groupId }.sortedBy { it.indexPosition } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun persistOrder(reorderedItems: List<SectionHeadAdded>, startIndex: Int = 0) {
        viewModelScope.launch {
            reorderedItems.forEachIndexed { index, item ->
                val position = startIndex + index
                if (item.indexPosition != position) {
                    sectionHeadRepository.updateAddedPosition(item.id, position)
                }
            }
        }
    }
}
