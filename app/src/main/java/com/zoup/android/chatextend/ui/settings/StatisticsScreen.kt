package com.zoup.android.chatextend.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zoup.android.chatextend.data.database.entity.ChatMessageEntity
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StatisticsScreen(
    messages: List<ChatMessageEntity>
) {
    val scrollState = rememberScrollState()
    
    // 计算统计数据
    val stats = remember(messages) {
        calculateStatistics(messages)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "使用统计",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 总体统计卡片
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "对话总数",
                value = stats.totalConversations.toString(),
                icon = Icons.Default.Check,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "消息总数",
                value = stats.totalMessages.toString(),
                icon = Icons.Default.Info,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "收藏数",
                value = stats.collectedCount.toString(),
                icon = Icons.Default.Star,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "今日对话",
                value = stats.todayConversations.toString(),
                icon = Icons.Default.Check,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 使用趋势
        Text(
            text = "使用趋势",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                TrendItem("最近7天", stats.last7DaysCount)
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                TrendItem("最近30天", stats.last30DaysCount)
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                TrendItem("总计", stats.totalConversations)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 最活跃时间
        Text(
            text = "使用习惯",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                InfoRow("最活跃时段", stats.mostActiveHour)
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                InfoRow("平均对话长度", "${stats.avgMessagesPerConversation} 条消息")
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                InfoRow("首次使用", stats.firstUseDate)
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun TrendItem(period: String, count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = period,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "$count 次对话",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

data class Statistics(
    val totalConversations: Int,
    val totalMessages: Int,
    val collectedCount: Int,
    val todayConversations: Int,
    val last7DaysCount: Int,
    val last30DaysCount: Int,
    val mostActiveHour: String,
    val avgMessagesPerConversation: Int,
    val firstUseDate: String
)

fun calculateStatistics(messages: List<ChatMessageEntity>): Statistics {
    val now = System.currentTimeMillis()
    val todayStart = now - (now % 86400000)
    val last7DaysStart = now - 7 * 86400000
    val last30DaysStart = now - 30 * 86400000
    
    // 计算消息总数(解析content中的消息数量)
    val totalMessages = messages.sumOf { message ->
        try {
            val content = message.content
            if (content.isNotEmpty()) {
                // 简单计算:统计"role"出现的次数
                content.split("\"role\"").size - 1
            } else {
                0
            }
        } catch (e: Exception) {
            0
        }
    }
    
    // 计算最活跃时段
    val hourCounts = mutableMapOf<Int, Int>()
    messages.forEach { message ->
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = message.timestamp
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        hourCounts[hour] = (hourCounts[hour] ?: 0) + 1
    }
    val mostActiveHour = hourCounts.maxByOrNull { it.value }?.key ?: 0
    
    // 首次使用日期
    val firstMessage = messages.minByOrNull { it.timestamp }
    val firstUseDate = if (firstMessage != null) {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(firstMessage.timestamp))
    } else {
        "暂无数据"
    }
    
    return Statistics(
        totalConversations = messages.size,
        totalMessages = totalMessages,
        collectedCount = messages.count { it.isCollected },
        todayConversations = messages.count { it.timestamp >= todayStart },
        last7DaysCount = messages.count { it.timestamp >= last7DaysStart },
        last30DaysCount = messages.count { it.timestamp >= last30DaysStart },
        mostActiveHour = "${mostActiveHour}:00 - ${mostActiveHour + 1}:00",
        avgMessagesPerConversation = if (messages.isNotEmpty()) totalMessages / messages.size else 0,
        firstUseDate = firstUseDate
    )
}
