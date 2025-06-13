package com.zoup.android.chatextend.ui.notes

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Space
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox
import com.zoup.android.chatextend.R
import com.zoup.android.chatextend.databinding.ItemDirBinding
import com.zoup.android.chatextend.databinding.ItemFileBinding
import io.github.dingyi222666.view.treeview.DataSource
import io.github.dingyi222666.view.treeview.TreeNode
import io.github.dingyi222666.view.treeview.TreeNodeEventListener
import io.github.dingyi222666.view.treeview.TreeView
import io.github.dingyi222666.view.treeview.TreeViewBinder

class NotesViewBinder : TreeViewBinder<DataSource<String>>(),
    TreeNodeEventListener<DataSource<String>> {
    // 定义回调接口
    public var onNodeLongClickListener: ((TreeNode<DataSource<String>>) -> Unit)? = null

    override fun createView(parent: ViewGroup, viewType: Int): View {
        val layoutInflater = LayoutInflater.from(parent.context)
        return if (viewType == 1) {
            ItemDirBinding.inflate(layoutInflater, parent, false).root
        } else {
            ItemFileBinding.inflate(layoutInflater, parent, false).root
        }
    }

    override fun getItemViewType(node: TreeNode<DataSource<String>>): Int {
        if (node.isChild) {
            return 1
        }
        return 0
    }

    override fun bindView(
        holder: TreeView.ViewHolder,
        node: TreeNode<DataSource<String>>,
        listener: TreeNodeEventListener<DataSource<String>>
    ) {
        if (node.isChild) {
            applyDir(holder, node)
        } else {
            applyFile(holder, node)
        }

        applyDepth(holder, node)

        getCheckableView(node, holder).apply {
            isVisible = node.selected
            isSelected = node.selected
        }
    }

    private fun applyFile(holder: TreeView.ViewHolder, node: TreeNode<DataSource<String>>) {
        val binding = ItemFileBinding.bind(holder.itemView)
        binding.tvName.text = node.name.toString()

    }

    private fun applyDepth(holder: TreeView.ViewHolder, node: TreeNode<DataSource<String>>) {
        val itemView = holder.itemView.findViewById<Space>(R.id.space)

        itemView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            width = node.depth * 22.dp
        }
    }

    private fun applyDir(holder: TreeView.ViewHolder, node: TreeNode<DataSource<String>>) {
        val binding = ItemDirBinding.bind(holder.itemView)
        binding.tvName.text = node.name.toString()

        binding
            .ivArrow
            .animate()
            .rotation(if (node.expand) 90f else 0f)
            .setDuration(200)
            .start()

    }

    override fun getCheckableView(
        node: TreeNode<DataSource<String>>,
        holder: TreeView.ViewHolder
    ): MaterialCheckBox {
        return if (node.isChild) {
            ItemDirBinding.bind(holder.itemView).checkbox
        } else {
            ItemFileBinding.bind(holder.itemView).checkbox
        }
//        return ItemDirBinding.bind(holder.itemView).checkbox
    }

    override fun onClick(node: TreeNode<DataSource<String>>, holder: TreeView.ViewHolder) {
//        Toast.makeText(this@MainActivity, "Clicked ${node.name}", Toast.LENGTH_LONG).show()
    }

    override fun onMoveView(
        srcHolder: RecyclerView.ViewHolder,
        srcNode: TreeNode<DataSource<String>>,
        targetHolder: RecyclerView.ViewHolder?,
        targetNode: TreeNode<DataSource<String>>?
    ): Boolean {
        applyDepth(srcHolder as TreeView.ViewHolder, srcNode)

        srcHolder.itemView.alpha = 0.7f

        return true
    }

    override fun onMovedView(
        srcNode: TreeNode<DataSource<String>>?,
        targetNode: TreeNode<DataSource<String>>?,
        holder: RecyclerView.ViewHolder
    ) {
        holder.itemView.alpha = 1f
    }

    override fun onToggle(
        node: TreeNode<DataSource<String>>,
        isExpand: Boolean,
        holder: TreeView.ViewHolder
    ) {
        applyDir(holder, node)
    }

    override fun onRefresh(status: Boolean) {
//        binding.progress.isVisible = status
    }

    override fun onLongClick(node: TreeNode<DataSource<String>>, holder: TreeView.ViewHolder): Boolean {
        onNodeLongClickListener?.invoke(node)
        return true
    }
}

inline val Int.dp: Int
    get() = (Resources.getSystem().displayMetrics.density * this + 0.5f).toInt()