package com.zoup.android.chatextend.ui.favourites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation.findNavController
import com.zoup.android.chatextend.MainActivity
import com.zoup.android.chatextend.data.database.entity.MessageCategoryEntity
import com.zoup.android.chatextend.databinding.FragmentFavouritesBinding
import com.zoup.android.chatextend.utils.Constants
import com.zoup.android.chatextend.utils.Constants.Companion.CATEGORY_FRAGMENT_REQUEST_KEY
import io.github.dingyi222666.view.treeview.AbstractTree
import io.github.dingyi222666.view.treeview.Branch
import io.github.dingyi222666.view.treeview.CreateDataScope
import io.github.dingyi222666.view.treeview.DataSource
import io.github.dingyi222666.view.treeview.DataSourceNodeGenerator
import io.github.dingyi222666.view.treeview.Leaf
import io.github.dingyi222666.view.treeview.Tree
import io.github.dingyi222666.view.treeview.TreeNode
import io.github.dingyi222666.view.treeview.TreeNodeGenerator
import io.github.dingyi222666.view.treeview.TreeView
import io.github.dingyi222666.view.treeview.buildTree
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.getViewModel
import java.util.UUID

class FavouritesFragment : Fragment() {

    private var _binding: FragmentFavouritesBinding? = null
    private var isSlow = false

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var root: View
    private var selectedCategoryId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if(requireActivity() is MainActivity){
            (activity as MainActivity).setFavouriteMenuVisibility(false)
            (activity as MainActivity).setSureMenuVisibility(true)
        }
        val categoryViewModel = getViewModel<CategoryViewModel>()

        _binding = FragmentFavouritesBinding.inflate(inflater, container, false)
        root = binding.root

        // 初始化 TreeView，先设置 binder 等属性，但暂不绑定 tree
        val treeView = binding.treeview as TreeView<DataSource<String>>
        val myBinder = ViewBinder().apply {
            onNodeLongClickListener = { node ->
                selectedCategoryId = node.id
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

//        val tree = createTree()
//        treeView.tree = tree
//        treeView.selectionMode = TreeView.SelectionMode.SINGLE
//        lifecycleScope.launch {
//            treeView.refresh()
//        }
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


    private fun createTree(): Tree<DataSource<String>> {
        val dataCreator: CreateDataScope<String> = { _, _ -> UUID.randomUUID().toString() }
        val tree = buildTree(dataCreator) {
            Branch("app") {
                Branch("src") {
                    Branch("main") {
                        Branch("kotlin") {
                            Branch("com.dingyi.treeview") {
                                Leaf("MainActivity.kt")
                            }
                        }
                        Branch("java") {
                            Branch("com.dingyi.treeview") {
                                Leaf("MainActivity.java")
                            }
                        }
                        Branch("res") {
                            Branch("drawable") {

                            }
                            Branch("xml") {}
                        }
                        Leaf("AndroidManifest.xml")
                    }
                    Branch("test") {
                        Branch("java") {
                            Branch("com.dingyi.treeview") {
                                Leaf("ExampleUnitTest.kt")
                            }
                        }
                    }

                }
                Leaf("build.gradle")
                Leaf("gradle.properties")
            }
            Branch("build") {
                Branch("generated") {
                    Branch("source") {
                        Branch("buildConfig") {
                            Branch("debug") {
                                Leaf("com.dingyi.treeview.BuildConfig.java")
                            }
                        }
                    }
                }
                Branch("outputs") {
                    Branch("apk") {
                        Branch("debug") {
                            Leaf("app-debug.apk")
                        }
                    }
                }
            }
        }

        val oldGenerator = tree.generator

        tree.generator = object : TreeNodeGenerator<DataSource<String>> {
            override suspend fun fetchChildData(targetNode: TreeNode<DataSource<String>>): Set<DataSource<String>> {
                if (isSlow) {
                    delay(2000L)
                }
                return oldGenerator.fetchChildData(targetNode)
            }

            override fun createNode(
                parentNode: TreeNode<DataSource<String>>,
                currentData: DataSource<String>,
                tree: AbstractTree<DataSource<String>>
            ): TreeNode<DataSource<String>> {
                return oldGenerator.createNode(parentNode, currentData, tree)
            }

            override suspend fun moveNode(
                srcNode: TreeNode<DataSource<String>>,
                targetNode: TreeNode<DataSource<String>>,
                tree: AbstractTree<DataSource<String>>
            ): Boolean {
                return oldGenerator.moveNode(srcNode, targetNode, tree)
            }
        }

        return tree
    }

    fun clickSureMenuItem() {
        val args = Bundle()
        args.putInt(Constants.CATEGORY_ID_KEY, selectedCategoryId)
        parentFragmentManager.setFragmentResult(CATEGORY_FRAGMENT_REQUEST_KEY, args)
        findNavController(root).navigateUp()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}