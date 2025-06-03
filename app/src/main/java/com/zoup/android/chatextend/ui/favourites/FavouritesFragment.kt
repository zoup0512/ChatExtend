package com.zoup.android.chatextend.ui.favourites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.zoup.android.chatextend.MainActivity
import com.zoup.android.chatextend.databinding.FragmentFavouritesBinding
import com.zoup.android.chatextend.databinding.FragmentSettingsBinding
import com.zoup.android.chatextend.ui.settings.SettingsViewModel
import io.github.dingyi222666.view.treeview.AbstractTree
import io.github.dingyi222666.view.treeview.Branch
import io.github.dingyi222666.view.treeview.CreateDataScope
import io.github.dingyi222666.view.treeview.DataSource
import io.github.dingyi222666.view.treeview.Leaf
import io.github.dingyi222666.view.treeview.Tree
import io.github.dingyi222666.view.treeview.TreeNode
import io.github.dingyi222666.view.treeview.TreeNodeGenerator
import io.github.dingyi222666.view.treeview.TreeView
import io.github.dingyi222666.view.treeview.buildTree
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

class FavouritesFragment : Fragment() {

    private var _binding: FragmentFavouritesBinding? = null
    private var isSlow = false

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var tree: Tree<DataSource<String>>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if(requireActivity() is MainActivity){
            (activity as MainActivity).setMenuVisibility(false)
        }
        val slideshowViewModel =
            ViewModelProvider(this).get(SettingsViewModel::class.java)

        _binding = FragmentFavouritesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textSlideshow
        slideshowViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        val tree = createTree()
        this.tree = tree
        (binding.treeview as TreeView<DataSource<String>>).apply {
            supportHorizontalScroll = true
            bindCoroutineScope(lifecycleScope)
            this.tree = tree
            binder = ViewBinder()
            nodeEventListener = binder as ViewBinder
            selectionMode = TreeView.SelectionMode.NONE
        }

        lifecycleScope.launch {
            binding.treeview.refresh()
            //  binding.treeview.expandUntil(1,true)
            //  binding.treeview.expandAll(true)
        }
        return root
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}