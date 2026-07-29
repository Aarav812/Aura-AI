package com.aura.ai.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.ai.domain.usecase.GlobalSearchUseCase
import com.aura.ai.domain.usecase.SearchResults
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    globalSearch: GlobalSearchUseCase
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    val results = _query
        .debounce(250)
        .flatMapLatest { q ->
            if (q.isBlank()) flowOf(SearchResults(emptyList(), emptyList()))
            else globalSearch(q)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SearchResults(emptyList(), emptyList()))

    fun onQueryChange(q: String) { _query.value = q }
}
