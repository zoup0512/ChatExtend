package com.zoup.android.chatextend.ui.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation.findNavController
import com.zoup.android.chatextend.MainActivity
import com.zoup.android.chatextend.R
import com.zoup.android.chatextend.data.database.entity.MessageCategoryEntity
import com.zoup.android.chatextend.databinding.FragmentCategoryBinding
import com.zoup.android.chatextend.utils.Constants
import com.zoup.android.chatextend.utils.Constants.Companion.CATEGORY_FRAGMENT_REQUEST_KEY
import io.github.dingyi222666.view.treeview.DataSource
import io.github.dingyi222666.view.treeview.DataSourceNodeGenerator
import io.github.dingyi222666.view.treeview.Tree
import io.github.dingyi222666.view.treeview.TreeView
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.getViewModel

class CategoryManagementFragment : Fragment() {

    private var _binding: FragmentCategoryBinding? = null
    private var isSlow = false

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var root: View
    private var selectedCategoryId: Int = -1
    private lateinit var categoryViewModel: CategoryViewModel
    private var addFlag: Boolean = false
    private var addCategoryName = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if(requireActivity() is MainActivity){
            (activity as MainActivity).setFavouriteMenuVisibility(false)
            (activity as MainActivity).setSureMenuVisibility(true)
        }
        categoryViewModel = getViewModel<CategoryViewModel>()

        _binding = FragmentCategoryBinding.inflate(inflater, container, false)
        root = binding.root

        // 初始化 TreeView，先设置 binder 等属性，但暂不绑定 tree
        val treeView = binding.treeview as TreeView<DataSource<String>>
        val myBinder = ViewBinder().apply {
            onNodeLongClickListener = { node ->
                val str = node.data?.data
                str?.toInt()?.let {
                    selectedCategoryId = it
                }
            }
        }
        treeView.apply {
            supportHorizontalScroll = true
            bindCoroutineScope(lifecycleScope)
            binder = myBinder
            nodeEventListener = myBinder
//            selectionMode = TreeView.SelectionMode.SINGLE
        }

        categoryViewModel.messageCategories.asLiveData().observe(viewLifecycleOwner) { categories ->
            val tree = createTree(categories ?: emptyList())
            treeView.tree = tree
            treeView.selectionMode = TreeView.SelectionMode.SINGLE
            lifecycleScope.launch {
                treeView.refresh()
            }
        }
        return root
    }

    private fun createTree(categories: List<MessageCategoryEntity>): Tree<DataSource<String>> {
        val tree = Tree<DataSource<String>>()
        val treeNodes = buildCategoryTree(categories)
        val rootDataSource = convertToDataSource(treeNodes)
        val generator = DataSourceNodeGenerator(rootDataSource)

        tree.generator = generator
        tree.initTree()
        return tree
    }

    fun clickSureMenuItem() {
        // 收藏选中类别
        if (!addFlag) {
            if (selectedCategoryId == Constants.NO_DATA_SYMBOL) {
                showNoCategoryDialog()
            } else {
                val args = Bundle()
                args.putInt(Constants.CATEGORY_ID_KEY, selectedCategoryId)
                parentFragmentManager.setFragmentResult(CATEGORY_FRAGMENT_REQUEST_KEY, args)
                findNavController(root).navigateUp()
            }
        } else {
            lifecycleScope.launch {
                val categoryEntity = MessageCategoryEntity(
                    id = 0,
                    name = addCategoryName,
                    parentCategoryId = selectedCategoryId
                )
                categoryViewModel.addMessageCategory(categoryEntity)
            }
        }
    }

    fun clickAddMenuItem() {
        val input = EditText(requireContext())
        input.hint = "请输入分类名称"

        AlertDialog.Builder(requireContext())
            .setTitle("新增分类")
            .setView(input)
            .setPositiveButton("选择上级类别") { dialog, _ ->
                val categoryName = input.text.toString()
                if (categoryName.isNotEmpty()) {
                    addCategoryName = categoryName
                    addFlag = true
                }
                dialog.dismiss()
            }
            .setNegativeButton("取消") { dialog, _ ->
                addCategoryName = ""
                addFlag = false
                dialog.dismiss()
            }
            .show()
    }


    fun clickDeleteMenuItem() {
        if (selectedCategoryId == Constants.NO_DATA_SYMBOL) {
            showNoCategoryDialog()
        } else {
            lifecycleScope.launch {
                categoryViewModel.deleteMessageCategoryById(selectedCategoryId)
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun showNoCategoryDialog() {
        Toast.makeText(
            requireContext(),
            getString(R.string.tips_no_category_selected),
            Toast.LENGTH_SHORT
        ).show()
    }
}