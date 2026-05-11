package com.zoup.android.chatextend.ui.category

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.fragment.app.DialogFragment
import com.zoup.android.chatextend.data.database.entity.MessageCategoryEntity

/**
 * 快速分类对话框
 * 显示常用分类标签,支持快速选择和新建
 */
class QuickCategoryDialogFragment : DialogFragment() {
    
    private var categories: List<MessageCategoryEntity> = emptyList()
    private var onCategorySelected: ((Int) -> Unit)? = null
    private var onCreateNew: (() -> Unit)? = null
    
    fun setCategories(categories: List<MessageCategoryEntity>) {
        this.categories = categories
    }
    
    fun setOnCategorySelectedListener(listener: (Int) -> Unit) {
        this.onCategorySelected = listener
    }
    
    fun setOnCreateNewListener(listener: () -> Unit) {
        this.onCreateNew = listener
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val composeView = ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    QuickCategoryDialogContent(
                        categories = categories,
                        onCategorySelected = { categoryId ->
                            onCategorySelected?.invoke(categoryId)
                            dismiss()
                        },
                        onCreateNew = {
                            onCreateNew?.invoke()
                            dismiss()
                        },
                        onDismiss = { dismiss() }
                    )
                }
            }
        }
        
        return AlertDialog.Builder(requireContext())
            .setView(composeView)
            .create()
    }
}

@Composable
fun QuickCategoryDialogContent(
    categories: List<MessageCategoryEntity>,
    onCategorySelected: (Int) -> Unit,
    onCreateNew: () -> Unit,
    onDismiss: () -> Unit
) {
    // 获取顶级分类(parentCategoryId == -1)
    val topLevelCategories = categories.filter { it.parentCategoryId == -1 }
    
    // 预设的快速标签
    val quickTags = listOf(
        QuickTag("工作", "💼"),
        QuickTag("学习", "📚"),
        QuickTag("生活", "🏠"),
        QuickTag("娱乐", "🎮"),
        QuickTag("其他", "📝")
    )
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "选择分类",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 快速标签
        Text(
            text = "快速标签",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 快速标签网格
        QuickTagGrid(
            tags = quickTags,
            categories = categories,
            onTagSelected = { tag ->
                // 查找或创建对应的分类
                val category = topLevelCategories.find { it.name == tag.name }
                if (category != null) {
                    onCategorySelected(category.id)
                } else {
                    // 如果不存在,提示创建
                    onCreateNew()
                }
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 现有分类
        if (topLevelCategories.isNotEmpty()) {
            Text(
                text = "我的分类",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
            ) {
                items(topLevelCategories) { category ->
                    CategoryItem(
                        category = category,
                        onClick = { onCategorySelected(category.id) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // 新建分类按钮
        OutlinedButton(
            onClick = onCreateNew,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = "新建")
            Spacer(modifier = Modifier.width(8.dp))
            Text("新建分类")
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 取消按钮
        TextButton(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("取消")
        }
    }
}

@Composable
fun QuickTagGrid(
    tags: List<QuickTag>,
    categories: List<MessageCategoryEntity>,
    onTagSelected: (QuickTag) -> Unit
) {
    Column {
        tags.chunked(3).forEach { rowTags ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowTags.forEach { tag ->
                    QuickTagChip(
                        tag = tag,
                        isExist = categories.any { it.name == tag.name && it.parentCategoryId == -1 },
                        onClick = { onTagSelected(tag) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // 填充空白
                repeat(3 - rowTags.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun QuickTagChip(
    tag: QuickTag,
    isExist: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(56.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isExist) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = tag.emoji,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tag.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isExist)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (isExist) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "已存在",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun CategoryItem(
    category: MessageCategoryEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "选择",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

data class QuickTag(
    val name: String,
    val emoji: String
)
