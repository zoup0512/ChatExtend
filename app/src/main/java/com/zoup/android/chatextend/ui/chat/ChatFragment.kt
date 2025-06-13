package com.zoup.android.chatextend.ui.chat

import android.Manifest
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.navigation.Navigation.findNavController
import com.zoup.android.chatextend.MainActivity
import com.zoup.android.chatextend.R
import com.zoup.android.chatextend.data.database.AppDatabase
import com.zoup.android.chatextend.data.repository.ChatMessageRepository
import com.zoup.android.chatextend.utils.Constants
import com.zoup.android.chatextend.utils.MessageIdManager

class ChatFragment : Fragment() {

//    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
//    private val binding get() = _binding!!
    private lateinit var root: View
    internal val viewModel by viewModels<ChatViewModel>(
        factoryProducer = {
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    // 获取数据库实例并获取 DAO
                    val dao = AppDatabase.getInstance(requireContext()).chatMessageDao()
                    // 创建 Repository 并传入 DAO
                    val repository = ChatMessageRepository(dao)
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
        if(requireActivity() is MainActivity){
            (activity as MainActivity).setFavouriteMenuVisibility(true)
            (activity as MainActivity).setSureMenuVisibility(false)
        }
        val composeView = ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        ChatScreen(viewModel = viewModel)
                    }
                }
            }
        }
        root = composeView
        return composeView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        viewModel.chatStateFlow.asLiveData().observe(viewLifecycleOwner) { collectState ->
            if (requireActivity() is MainActivity) {
                Log.d("ChatFragment", "准备调用 setFavouriteMenuChecked")
                (activity as MainActivity).setFavouriteMenuChecked(collectState.isCollected)
            }
        }
    }

    // FragmentA.kt
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 设置结果监听器（在 Fragment 创建时注册）
        parentFragmentManager.setFragmentResultListener(
            Constants.CATEGORY_FRAGMENT_REQUEST_KEY,  // 唯一标识符，需与 Fragment B 匹配
            this
        ) { requestKey, resultBundle ->
            val categoryId = resultBundle.getInt(Constants.CATEGORY_ID_KEY)
            if (categoryId != 0 && categoryId != -1) {
                viewModel.collectChatMessages(categoryId)
            }
        }
    }

    private fun init() {
        // 初始化逻辑，例如：
        val messageId = MessageIdManager.currentMessageId
        viewModel.initViews(messageId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    public fun clickFavouriteMenuItem() {
        if (viewModel.chatStateFlow.value.isCollected) {
            viewModel.collectChatMessages(viewModel.chatStateFlow.value.categoryId)
        } else {
            val args = Bundle()
            findNavController(root).navigate(R.id.nav_category, args)
        }
    }

    // 检查权限
    private fun checkStoragePermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PERMISSION_GRANTED -> {
//                collectChatMessages()
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) -> {
                showPermissionRationaleDialog()
            }

            else -> {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    Constants.REQUEST_STORAGE_PERMISSION
                )
            }
        }
    }

    // 处理权限结果
    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            Constants.REQUEST_STORAGE_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    collectChatMessages()
                } else {
                    Toast.makeText(requireContext(), "需要存储权限才能保存文件", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 显示解释权限用途的对话框
    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("需要存储权限")
            .setMessage("保存Markdown文件到下载目录需要存储权限")
            .setPositiveButton("确定") { _, _ ->
                // 用户理解后再次请求权限
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    Constants.REQUEST_STORAGE_PERMISSION
                )
            }
            .setNegativeButton("取消", null)
            .show()
    }

}