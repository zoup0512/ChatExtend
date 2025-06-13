package com.zoup.android.chatextend.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SettingsViewModel : ViewModel() {

    val _text = MutableLiveData<String>().apply {
        value = "无"
    }
    val text: LiveData<String> = _text
}