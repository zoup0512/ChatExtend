package com.zoup.android.chatextend.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.zoup.android.chatextend.utils.Constants

class SettingsViewModel : ViewModel() {

    val _text = MutableLiveData<String>().apply {
        value = Constants.DEEPSEEK_API_KEY
    }
    val text: LiveData<String> = _text
}