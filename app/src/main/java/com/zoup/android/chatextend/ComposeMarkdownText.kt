package com.zoup.android.chatextend

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import io.noties.markwon.Markwon

@Composable
fun ComposeMarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    markwon: Markwon
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            MarkdownText(context).apply {
                setMarkdown(markdown)
            }
        },
        update = { view ->
            view.setMarkdown(markdown)
        }
    )
}