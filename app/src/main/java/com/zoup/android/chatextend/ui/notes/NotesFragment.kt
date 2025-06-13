package com.zoup.android.chatextend.ui.notes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.zoup.android.chatextend.MainActivity
import com.zoup.android.chatextend.data.database.entity.MessageCategoryEntity
import com.zoup.android.chatextend.databinding.FragmentNotesBinding
import com.zoup.android.chatextend.ui.notes.NotesViewModel
import com.zoup.android.chatextend.ui.notes.NotesViewBinder
import com.zoup.android.chatextend.ui.notes.buildCategoryTree
import com.zoup.android.chatextend.ui.notes.convertToDataSource
import io.github.dingyi222666.view.treeview.DataSource
import io.github.dingyi222666.view.treeview.DataSourceNodeGenerator
import io.github.dingyi222666.view.treeview.Tree
import io.github.dingyi222666.view.treeview.TreeView
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.getViewModel

class NotesFragment : Fragment() {

    private var _binding: FragmentNotesBinding? = null
    private val binding get() = _binding!!
    private lateinit var root: View
    private var selectedCategoryId: Int = -1
    private lateinit var notesViewModel: NotesViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (requireActivity() is MainActivity) {
            (activity as MainActivity).setFavouriteMenuVisibility(false)
            (activity as MainActivity).setSureMenuVisibility(false)
        }
        notesViewModel = getViewModel<NotesViewModel>()

        _binding = FragmentNotesBinding.inflate(inflater, container, false)
        root = binding.root

        // 初始化 TreeView，先设置 binder 等属性，但暂不绑定 tree
        val treeView = binding.treeview as TreeView<DataSource<String>>
        val myBinder = NotesViewBinder().apply {
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

        notesViewModel.messageCategories.asLiveData().observe(viewLifecycleOwner) { categories ->
            val tree = createTree(categories ?: emptyList())
            treeView.tree = tree
            treeView.selectionMode = TreeView.SelectionMode.NONE
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