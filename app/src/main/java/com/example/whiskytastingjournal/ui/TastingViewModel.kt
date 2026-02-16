package com.example.whiskytastingjournal.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.whiskytastingjournal.model.TastingEntry
import com.example.whiskytastingjournal.repository.TastingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SortOption(val label: String) {
    DATE_DESC("Newest First"),
    DATE_ASC("Oldest First"),
    NAME_ASC("Name A-Z"),
    DISTILLERY_ASC("Distillery A-Z"),
    RATING_DESC("Highest Rated"),
    RATING_ASC("Lowest Rated")
}

class TastingViewModel(private val repository: TastingRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.DATE_DESC)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    val tastings: StateFlow<List<TastingEntry>> =
        combine(repository.allTastings, _searchQuery, _sortOption) { list, query, sort ->
            list
                .filter { entry ->
                    if (query.isBlank()) true
                    else {
                        val q = query.lowercase()
                        entry.whiskyName.lowercase().contains(q) ||
                            entry.distillery.lowercase().contains(q) ||
                            entry.notes.lowercase().contains(q)
                    }
                }
                .let { filtered ->
                    when (sort) {
                        SortOption.DATE_DESC -> filtered.sortedByDescending { it.date }
                        SortOption.DATE_ASC -> filtered.sortedBy { it.date }
                        SortOption.NAME_ASC -> filtered.sortedBy { it.whiskyName.lowercase() }
                        SortOption.DISTILLERY_ASC -> filtered.sortedBy { it.distillery.lowercase() }
                        SortOption.RATING_DESC -> filtered.sortedByDescending { it.overallScore }
                        SortOption.RATING_ASC -> filtered.sortedBy { it.overallScore }
                    }
                }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortOption(option: SortOption) {
        _sortOption.value = option
    }

    fun addTasting(entry: TastingEntry) {
        viewModelScope.launch { repository.insert(entry) }
    }

    fun updateTasting(entry: TastingEntry) {
        viewModelScope.launch { repository.update(entry) }
    }

    fun deleteTasting(entry: TastingEntry) {
        viewModelScope.launch { repository.delete(entry) }
    }

    suspend fun getTastingById(id: String): TastingEntry? = repository.getById(id)

    class Factory(private val repository: TastingRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TastingViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return TastingViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
