package com.zoup.android.chatextend.ui.favourites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoup.android.chatextend.data.database.entity.MessageCategoryEntity
import com.zoup.android.chatextend.data.repository.MessageCategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CategoryViewModel(private val messageCategoryRepository: MessageCategoryRepository) :
    ViewModel() {

    private val _messageCategories = MutableStateFlow<List<MessageCategoryEntity>>(emptyList())
    val messageCategories = _messageCategories.asStateFlow()

    init {
        viewModelScope.launch {
            messageCategoryRepository.getAllMessageCategories().collect { list ->
                _messageCategories.value = list
            }
        }
    }

    fun deleteMessageCategoryById(id: Int) {
        viewModelScope.launch {
            messageCategoryRepository.deleteMessageCategoryById(id).collect { list ->
                _messageCategories.value = list
            }
        }
    }

    fun addMessageCategory(messageCategory: MessageCategoryEntity) {
        viewModelScope.launch {
            messageCategoryRepository.addMessageCategory(messageCategory).collect { list ->
                _messageCategories.value = list
            }
        }
    }

}