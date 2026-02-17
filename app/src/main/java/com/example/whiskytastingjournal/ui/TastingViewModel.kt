package com.example.whiskytastingjournal.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.whiskytastingjournal.model.AromaTag
import com.example.whiskytastingjournal.model.TastingAroma
import com.example.whiskytastingjournal.model.TastingEntry
import com.example.whiskytastingjournal.model.Whisky
import com.example.whiskytastingjournal.model.WhiskyWithTastings
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

    val aromaTags: StateFlow<List<AromaTag>> = repository.allAromaTags
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch { repository.seedAromaTagsIfEmpty() }
    }

    val whiskiesWithTastings: StateFlow<List<WhiskyWithTastings>> =
        combine(repository.allWhiskiesWithTastings, _searchQuery, _sortOption) { list, query, sort ->
            list
                .filter { wt ->
                    if (query.isBlank()) true
                    else {
                        val q = query.lowercase()
                        wt.whisky.whiskyName.lowercase().contains(q) ||
                            wt.whisky.distillery.lowercase().contains(q) ||
                            wt.whisky.region.lowercase().contains(q)
                    }
                }
                .let { filtered ->
                    when (sort) {
                        SortOption.DATE_DESC -> filtered.sortedByDescending { wt ->
                            wt.tastings.maxOfOrNull { it.date }
                        }
                        SortOption.DATE_ASC -> filtered.sortedBy { wt ->
                            wt.tastings.minOfOrNull { it.date }
                        }
                        SortOption.NAME_ASC -> filtered.sortedBy { it.whisky.whiskyName.lowercase() }
                        SortOption.DISTILLERY_ASC -> filtered.sortedBy { it.whisky.distillery.lowercase() }
                        SortOption.RATING_DESC -> filtered.sortedByDescending { wt ->
                            wt.tastings.maxOfOrNull { it.effectiveOverallScore } ?: 0f
                        }
                        SortOption.RATING_ASC -> filtered.sortedBy { wt ->
                            wt.tastings.minOfOrNull { it.effectiveOverallScore } ?: 0f
                        }
                    }
                }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortOption(option: SortOption) {
        _sortOption.value = option
    }

    // --- Whisky CRUD ---
    fun addWhisky(whisky: Whisky) {
        viewModelScope.launch { repository.insertWhisky(whisky) }
    }

    fun updateWhisky(whisky: Whisky) {
        viewModelScope.launch { repository.updateWhisky(whisky) }
    }

    fun deleteWhisky(whisky: Whisky) {
        viewModelScope.launch { repository.deleteWhisky(whisky) }
    }

    fun deleteWhiskyById(id: String) {
        viewModelScope.launch { repository.deleteWhiskyById(id) }
    }

    suspend fun getWhiskyById(id: String): Whisky? = repository.getWhiskyById(id)

    suspend fun getWhiskyWithTastings(id: String): WhiskyWithTastings? = repository.getWhiskyWithTastings(id)

    // --- Tasting CRUD ---
    fun addTasting(entry: TastingEntry) {
        viewModelScope.launch { repository.insertTasting(entry) }
    }

    fun updateTasting(entry: TastingEntry) {
        viewModelScope.launch { repository.updateTasting(entry) }
    }

    fun deleteTasting(entry: TastingEntry) {
        viewModelScope.launch { repository.deleteTasting(entry) }
    }

    suspend fun getTastingById(id: String): TastingEntry? = repository.getTastingById(id)

    suspend fun findDuplicateTasting(whiskyId: String, date: String, alias: String): TastingEntry? =
        repository.findDuplicateTasting(whiskyId, date, alias)

    // --- Tasting + Aromas combined save ---
    fun saveTastingWithAromas(tasting: TastingEntry, aromas: List<TastingAroma>) {
        viewModelScope.launch { repository.saveTastingWithAromas(tasting, aromas) }
    }

    fun updateTastingWithAromas(tasting: TastingEntry, aromas: List<TastingAroma>) {
        viewModelScope.launch { repository.updateTastingWithAromas(tasting, aromas) }
    }

    // --- Aroma queries ---
    suspend fun getAromasForTasting(tastingId: String): List<TastingAroma> =
        repository.getAromasForTasting(tastingId)

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
