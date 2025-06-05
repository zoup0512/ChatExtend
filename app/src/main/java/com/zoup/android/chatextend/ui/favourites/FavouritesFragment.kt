package com.zoup.android.chatextend.ui.favourites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.zoup.android.chatextend.MainActivity
import com.zoup.android.chatextend.data.database.entity.MessageCategoryEntity
import com.zoup.android.chatextend.databinding.FragmentFavouritesBinding
import io.github.dingyi222666.view.treeview.DataSource
import io.github.dingyi222666.view.treeview.DataSourceNodeGenerator
import io.github.dingyi222666.view.treeview.Tree
import io.github.dingyi222666.view.treeview.TreeView
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.getViewModel

class FavouritesFragment : Fragment() {

    private var _binding: FragmentFavouritesBinding? = null
    private var isSlow = false

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if(requireActivity() is MainActivity){
            (activity as MainActivity).setMenuVisibility(false)
        }
        val categoryViewModel = getViewModel<CategoryViewModel>()

        _binding = FragmentFavouritesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // 初始化 TreeView，先设置 binder 等属性，但暂不绑定 tree
        val treeView = binding.treeview as TreeView<DataSource<String>>
        treeView.apply {
            supportHorizontalScroll = true
            bindCoroutineScope(lifecycleScope)
            binder = ViewBinder()
            nodeEventListener = binder as ViewBinder
            selectionMode = TreeView.SelectionMode.NONE
        }

        categoryViewModel.messageCategories.asLiveData().observe(viewLifecycleOwner) { categories ->
            val tree = createTree(categories ?: emptyList())
            treeView.tree = tree
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}