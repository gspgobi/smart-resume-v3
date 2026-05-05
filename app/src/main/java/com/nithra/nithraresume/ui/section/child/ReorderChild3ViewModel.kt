package com.nithra.nithraresume.ui.section.child

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nithra.nithraresume.data.model.SectionChild3
import com.nithra.nithraresume.data.repository.SectionChildRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReorderChild3ViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sectionChildRepository: SectionChildRepository
) : ViewModel() {

    val sectionHeadAddedId: Int = checkNotNull(savedStateHandle["sectionHeadAddedId"])

    val items: StateFlow<List<SectionChild3>> = sectionChildRepository
        .getChild3List(sectionHeadAddedId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun persistOrder(orderedItems: List<SectionChild3>) {
        viewModelScope.launch {
            orderedItems.forEachIndexed { index, item ->
                sectionChildRepository.updateChild3Position(item.id, index)
            }
        }
    }
}
