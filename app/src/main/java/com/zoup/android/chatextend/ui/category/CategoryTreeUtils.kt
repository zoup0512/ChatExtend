package com.zoup.android.chatextend.ui.category

import com.zoup.android.chatextend.data.database.entity.MessageCategoryEntity
import io.github.dingyi222666.view.treeview.MultipleDataSource
import io.github.dingyi222666.view.treeview.SingleDataSource

data class CategoryTreeNode(
    val id: Int,
    val name: String,
    val parentId: Int,
    var children: MutableList<CategoryTreeNode> = mutableListOf()
)

fun buildCategoryTree(categories: List<MessageCategoryEntity>): List<CategoryTreeNode> {
    val nodeMap = categories.associateBy({ it.id }, { CategoryTreeNode(it.id, it.name, it.parentCategoryId) })

    val rootNodes = mutableListOf<CategoryTreeNode>()

    for (category in categories) {
        val node = nodeMap[category.id]!!
        if (category.parentCategoryId == -1 || !nodeMap.containsKey(category.parentCategoryId)) {
            // 如果 parentCategoryId 是 -1 或者父节点不存在，则作为根节点
            rootNodes.add(node)
        } else {
            // 否则添加到对应父节点的 children 列表中
            nodeMap[category.parentCategoryId]?.children?.add(node)
        }
    }

    return rootNodes
}

fun convertToDataSource(rootNodes: List<CategoryTreeNode>): MultipleDataSource<String> {
    val rootDataSource = MultipleDataSource<String>("Root", null)

    fun addNodeToDataSource(node: CategoryTreeNode, parentDataSource: MultipleDataSource<String>) {
        val dataSource = if (node.children.isEmpty()) {
            SingleDataSource(name = node.name, data = node.id.toString(), parent = parentDataSource)
        } else {
            val multipleDataSource = MultipleDataSource<String>(name = node.name, data = node.id.toString(), parent = parentDataSource)
            node.children.forEach { child ->
                addNodeToDataSource(child, multipleDataSource)
            }
            multipleDataSource
        }
        parentDataSource.add(dataSource)
    }

    rootNodes.forEach { rootNode ->
        addNodeToDataSource(rootNode, rootDataSource)
    }

    return rootDataSource
}

