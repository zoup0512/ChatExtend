package com.zoup.android.chatextend.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.zoup.android.chatextend.MainActivity
import com.zoup.android.chatextend.databinding.FragmentSettingsBinding
import com.zoup.android.chatextend.utils.Constants
import com.zoup.android.chatextend.utils.encryptString

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var viewModel: SettingsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if(requireActivity() is MainActivity){
            (activity as MainActivity).setFavouriteMenuVisibility(false)
            (activity as MainActivity).setSureMenuVisibility(false)
        }
        viewModel =
            ViewModelProvider(this).get(SettingsViewModel::class.java)

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // API Key 设置
        val apiKeyLayout = binding.apiKeyLayout
        apiKeyLayout.setOnClickListener {
            clickChangeAPIKey()
        }
        val textView: TextView = binding.tvKey
        viewModel.text.observe(viewLifecycleOwner) {
            textView.text = encryptString(it, 4, 4)
            Constants.DEEPSEEK_API_KEY = it
        }

        // 主题设置
        val themeLayout = binding.themeLayout
        val themeTextView: TextView = binding.tvTheme
        
        // 显示当前主题
        updateThemeText(themeTextView)
        
        themeLayout.setOnClickListener {
            showThemeDialog()
        }

        // 统计信息
        val statisticsLayout = binding.statisticsLayout
        statisticsLayout.setOnClickListener {
            showStatisticsDialog()
        }

        return root
    }

    private fun showStatisticsDialog() {
        val composeView = androidx.compose.ui.platform.ComposeView(requireContext()).apply {
            setContent {
                androidx.compose.material3.MaterialTheme {
                    val viewModel = androidx.lifecycle.viewmodel.compose.viewModel<com.zoup.android.chatextend.ui.chat.ChatViewModel>(
                        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                                val dao = com.zoup.android.chatextend.data.database.AppDatabase.getInstance(requireContext()).chatMessageDao()
                                val repository = com.zoup.android.chatextend.data.repository.ChatMessageRepository(dao)
                                return com.zoup.android.chatextend.ui.chat.ChatViewModel(repository) as T
                            }
                        }
                    )
                    val messages by viewModel.getAllHistoryMessages().collectAsState(initial = emptyList())
                    
                    com.zoup.android.chatextend.ui.settings.StatisticsScreen(messages = messages)
                }
            }
        }
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("使用统计")
            .setView(composeView)
            .setPositiveButton("关闭", null)
            .show()
    }

    private fun updateThemeText(textView: TextView) {
        val prefs = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val themeMode = prefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        textView.text = when (themeMode) {
            AppCompatDelegate.MODE_NIGHT_NO -> "浅色模式"
            AppCompatDelegate.MODE_NIGHT_YES -> "深色模式"
            else -> "跟随系统"
        }
    }

    private fun showThemeDialog() {
        val prefs = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val currentMode = prefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        
        val themes = arrayOf("跟随系统", "浅色模式", "深色模式")
        val currentSelection = when (currentMode) {
            AppCompatDelegate.MODE_NIGHT_NO -> 1
            AppCompatDelegate.MODE_NIGHT_YES -> 2
            else -> 0
        }

        AlertDialog.Builder(requireContext())
            .setTitle("选择主题")
            .setSingleChoiceItems(themes, currentSelection) { dialog, which ->
                val newMode = when (which) {
                    1 -> AppCompatDelegate.MODE_NIGHT_NO
                    2 -> AppCompatDelegate.MODE_NIGHT_YES
                    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }
                
                // 保存设置
                prefs.edit().putInt("theme_mode", newMode).apply()
                
                // 应用主题
                AppCompatDelegate.setDefaultNightMode(newMode)
                
                // 更新显示
                updateThemeText(binding.tvTheme)
                
                dialog.dismiss()
            }
            .show()
    }

    fun clickChangeAPIKey() {
        val input = EditText(requireContext())
        input.hint = "请输入API Key"

        AlertDialog.Builder(requireContext())
            .setView(input)
            .setPositiveButton("确定") { dialog, _ ->
                val inputKey = input.text.toString()
                if (inputKey.isNotEmpty()) {
                    viewModel._text.value = inputKey
                }
                dialog.dismiss()
            }
            .setNegativeButton("取消") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}