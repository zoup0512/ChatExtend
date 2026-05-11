package com.zoup.android.chatextend.ui.notes

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.Space
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox
import com.zoup.android.chatextend.R
import com.zoup.android.chatextend.databinding.ItemDirBinding
import com.zoup.android.chatextend.databinding.ItemFileSimpleBinding
import io.github.dingyi222666.view.treeview.DataSource
import io.github.dingyi222666.view.treeview.TreeNode
import io.github.dingyi222666.view.treeview.TreeNodeEventListener
import io.github.dingyi222666.view.treeview.TreeView
import io.github.dingyi222666.view.treeview.TreeViewBinder

class NotesViewBinder : TreeViewBinder<DataSource<String>>(),
    TreeNodeEventListener<DataSource<String>> {
    // 定义回调接口
    var onNoteItemClickListener: OnNoteItemClickListener? = null
    
    override fun createView(parent: ViewGroup, viewType: Int): View {
        val layoutInflater = LayoutInflater.from(parent.context)
        return if (viewType == 1) {
            ItemDirBinding.inflate(layoutInflater, parent, false).root
        } else {
            ItemFileSimpleBinding.inflate(layoutInflater, parent, false).root
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
        val binding = ItemFileSimpleBinding.bind(holder.itemView)
        val messageId = node.data?.data
        binding.tvName.text = node.name.toString()
        
        // 根据是否有messageId显示不同的图标
        val iconView = binding.ivIcon
        val iconRes = if (messageId.isNullOrEmpty()) {
            R.drawable.baseline_folder_24
        } else {
            R.drawable.baseline_insert_drive_file_24
        }
        iconView.setImageResource(iconRes)
    }

    private fun applyDepth(holder: TreeView.ViewHolder, node: TreeNode<DataSource<String>>) {
        val itemView = holder.itemView.findViewById<Space>(R.id.space)

        itemView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            width = node.depth * 24.dp
        }
    }

    private fun applyDir(holder: TreeView.ViewHolder, node: TreeNode<DataSource<String>>) {
        val binding = ItemDirBinding.bind(holder.itemView)
        binding.tvName.text = node.name.toString()

        // 箭头旋转动画 - 使用更流畅的插值器
        val arrowRotation = ObjectAnimator.ofFloat(
            binding.ivArrow,
            "rotation",
            if (node.expand) 90f else 0f
        ).apply {
            duration = 250
            interpolator = DecelerateInterpolator(1.5f)
        }

        // 文件夹图标动画 - 展开时放大，收起时缩小
        val folderIcon = binding.ivFolder
        updateFolderIcon(folderIcon, node.depth, node.expand)
        
        val scaleX = ObjectAnimator.ofFloat(
            folderIcon,
            "scaleX",
            if (node.expand) 1.1f else 1.0f
        ).apply {
            duration = 250
            interpolator = OvershootInterpolator(1.2f)
        }
        
        val scaleY = ObjectAnimator.ofFloat(
            folderIcon,
            "scaleY",
            if (node.expand) 1.1f else 1.0f
        ).apply {
            duration = 250
            interpolator = OvershootInterpolator(1.2f)
        }

        // 组合动画
        AnimatorSet().apply {
            playTogether(arrowRotation, scaleX, scaleY)
            start()
        }
    }

    /**
     * 根据深度和展开状态更新文件夹图标
     */
    private fun updateFolderIcon(iconView: ImageView, depth: Int, isExpanded: Boolean) {
        // 根据深度使用不同的图标资源
        val iconRes = when {
            depth == 0 && isExpanded -> R.drawable.baseline_folder_open_24
            depth == 0 -> R.drawable.baseline_folder_24
            isExpanded -> R.drawable.baseline_folder_open_24
            else -> R.drawable.baseline_folder_24
        }
        iconView.setImageResource(iconRes)
    }

    override fun getCheckableView(
        node: TreeNode<DataSource<String>>,
        holder: TreeView.ViewHolder
    ): MaterialCheckBox {
        return if (node.isChild) {
            ItemDirBinding.bind(holder.itemView).checkbox
        } else {
            ItemFileSimpleBinding.bind(holder.itemView).checkbox
        }
    }

    override fun onClick(node: TreeNode<DataSource<String>>, holder: TreeView.ViewHolder) {
        // 点击时添加轻微的缩放反馈
        holder.itemView.animate()
            .scaleX(0.97f)
            .scaleY(0.97f)
            .setDuration(100)
            .withEndAction {
                holder.itemView.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(100)
                    .start()
            }
            .start()
        
        // 触发回调
        onNoteItemClickListener?.invoke(node)
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
        // 可以在这里添加刷新动画
    }

    override fun onLongClick(node: TreeNode<DataSource<String>>, holder: TreeView.ViewHolder): Boolean {
        // 长按时添加震动反馈效果
        holder.itemView.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(100)
            .withEndAction {
                holder.itemView.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(100)
                    .start()
            }
            .start()
        
        return true
    }
}

inline val Int.dp: Int
    get() = (Resources.getSystem().displayMetrics.density * this + 0.5f).toInt()

typealias OnNoteItemClickListener = (node: TreeNode<DataSource<String>>) -> Unit
