package com.aura.ai.presentation.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.ai.domain.model.Chat
import com.aura.ai.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

enum class LibraryFilter(val label: String) { ALL("All"), FAVORITES("Favorites"), ARCHIVED("Archived") }
enum class LibraryLayout { LIST, GRID }

data class LibraryUiState(
    val chats: List<Chat> = emptyList(),
    val filter: LibraryFilter = LibraryFilter.ALL,
    val layout: LibraryLayout = LibraryLayout.LIST,
    val query: String = ""
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val filter = MutableStateFlow(LibraryFilter.ALL)
    private val layout = MutableStateFlow(LibraryLayout.LIST)
    private val query = MutableStateFlow("")

    val uiState = combine(
        chatRepository.observeChats(includeArchived = true),
        filter, layout, query
    ) { chats, f, l, q ->
        val filtered = chats
            .filter { q.isBlank() || it.title.contains(q, true) }
            .filter {
                when (f) {
                    LibraryFilter.ALL -> !it.archived
                    LibraryFilter.FAVORITES -> it.favorite
                    LibraryFilter.ARCHIVED -> it.archived
                }
            }
        LibraryUiState(filtered, f, l, q)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LibraryUiState())

    fun setFilter(f: LibraryFilter) { filter.value = f }
    fun toggleLayout() { layout.value = if (layout.value == LibraryLayout.LIST) LibraryLayout.GRID else LibraryLayout.LIST }
    fun setQuery(q: String) { query.value = q }
}
