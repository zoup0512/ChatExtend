package com.zoup.android.chatextend.ui.notes

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoup.android.chatextend.data.database.entity.MessageCategoryEntity
import com.zoup.android.chatextend.data.repository.NotesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch


class NotesViewModel(private val repository: NotesRepository) : ViewModel() {

    private val _mergedCategories = MutableStateFlow<List<MessageCategoryEntity>>(emptyList())
    val mergedCategories: Flow<List<MessageCategoryEntity>> = _mergedCategories

    init {
        viewModelScope.launch {
            repository.getMergedCategoriesAndMessages().collect { list ->
                _mergedCategories.value = list
            }
        }
    }
}