package com.zoup.android.chatextend.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.zoup.android.chatextend.data.database.AppDatabase
import com.zoup.android.chatextend.data.repository.ChatRepository
import com.zoup.android.chatextend.databinding.FragmentHistoryBinding
import com.zoup.android.chatextend.ui.chat.ChatViewModel

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val viewModel by viewModels<ChatViewModel>(
        factoryProducer = {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    // 获取数据库实例并获取 DAO
                    val dao = AppDatabase.getInstance(requireContext()).chatMessageDao()
                    // 创建 Repository 并传入 DAO
                    val repository = ChatRepository(dao)
                    return ChatViewModel(repository) as T
                }
            }
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val composeView = ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        HistoryScreen(viewModel = viewModel)
                    }
                }
            }
        }

        return composeView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}