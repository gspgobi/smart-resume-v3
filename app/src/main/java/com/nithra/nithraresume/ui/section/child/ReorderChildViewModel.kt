package com.nithra.nithraresume.ui.section.child

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nithra.nithraresume.data.repository.SectionChildRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReorderChildViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repo: SectionChildRepository
) : ViewModel() {

    val sectionHeadAddedId: Int = checkNotNull(savedStateHandle["sectionHeadAddedId"])
    private val childType: Int  = checkNotNull(savedStateHandle["childType"])

    val items: StateFlow<List<ReorderableItem>> = when (childType) {
        2 -> repo.getChild2List(sectionHeadAddedId)
                 .map { list -> list.map { ReorderableItem(it.id, it.indexPosition,
                     it.workRole.ifEmpty { it.companyName }) } }
        3 -> repo.getChild3List(sectionHeadAddedId)
                 .map { list -> list.map { ReorderableItem(it.id, it.indexPosition,
                     it.studyDegree.ifEmpty { it.schoolName }) } }
        6 -> repo.getChild6List(sectionHeadAddedId)
                 .map { list -> list.map { ReorderableItem(it.id, it.indexPosition, it.contentTitle) } }
        7 -> repo.getChild7List(sectionHeadAddedId)
                 .map { list -> list.map { ReorderableItem(it.id, it.indexPosition, it.contentTitle) } }
        else -> flow { emit(emptyList()) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun persistOrder(orderedItems: List<ReorderableItem>) {
        viewModelScope.launch {
            orderedItems.forEachIndexed { index, item ->
                when (childType) {
                    2 -> repo.updateChild2Position(item.id, index)
                    3 -> repo.updateChild3Position(item.id, index)
                    6 -> repo.updateChild6Position(item.id, index)
                    7 -> repo.updateChild7Position(item.id, index)
                }
            }
        }
    }
}
