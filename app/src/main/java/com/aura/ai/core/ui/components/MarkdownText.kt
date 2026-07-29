package com.aura.ai.core.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Lightweight Markdown renderer for chat: handles headings, bold/italic/inline-code,
 * bullet & numbered lists, block quotes, tables, and fenced code blocks with a copy button.
 * (Robust enough for streaming; swap for a full CommonMark renderer if needed.)
 */
@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    val blocks = remember(markdown) { parseBlocks(markdown) }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        blocks.forEach { block ->
            when (block) {
                is MdBlock.Code -> CodeBlock(block.language, block.code)
                is MdBlock.Table -> MarkdownTable(block.rows)
                is MdBlock.Quote -> Row {
                    Box(Modifier.size(width = 3.dp, height = 20.dp).background(MaterialTheme.colorScheme.primary))
                    Text(block.text, modifier = Modifier.padding(start = 8.dp),
                        color = color.copy(alpha = 0.8f), style = MaterialTheme.typography.bodyLarge)
                }
                is MdBlock.Text -> Text(
                    inlineAnnotated(block.text),
                    color = color,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

private sealed interface MdBlock {
    data class Text(val text: String) : MdBlock
    data class Code(val language: String, val code: String) : MdBlock
    data class Quote(val text: String) : MdBlock
    data class Table(val rows: List<List<String>>) : MdBlock
}

private fun parseBlocks(md: String): List<MdBlock> {
    val blocks = mutableListOf<MdBlock>()
    val lines = md.lines()
    var i = 0
    val textBuf = StringBuilder()

    fun flushText() {
        if (textBuf.isNotBlank()) blocks.add(MdBlock.Text(textBuf.trimEnd().toString()))
        textBuf.clear()
    }

    while (i < lines.size) {
        val line = lines[i]
        when {
            line.trimStart().startsWith("```") -> {
                flushText()
                val lang = line.trimStart().removePrefix("```").trim()
                val code = StringBuilder()
                i++
                while (i < lines.size && !lines[i].trimStart().startsWith("```")) {
                    code.appendLine(lines[i]); i++
                }
                blocks.add(MdBlock.Code(lang.ifBlank { "code" }, code.toString().trimEnd()))
            }
            line.trimStart().startsWith(">") -> {
                flushText()
                blocks.add(MdBlock.Quote(line.trimStart().removePrefix(">").trim()))
            }
            line.contains("|") && i + 1 < lines.size && lines[i + 1].contains("---") -> {
                flushText()
                val rows = mutableListOf<List<String>>()
                rows.add(splitRow(line))
                i += 2 // skip separator
                while (i < lines.size && lines[i].contains("|")) {
                    rows.add(splitRow(lines[i])); i++
                }
                blocks.add(MdBlock.Table(rows))
                continue
            }
            else -> textBuf.appendLine(line)
        }
        i++
    }
    flushText()
    return blocks
}

private fun splitRow(line: String): List<String> =
    line.trim().trim('|').split("|").map { it.trim() }

private fun inlineAnnotated(text: String) = buildAnnotatedString {
    var t = text
    // Headings -> bold larger handled inline as bold
    t.lines().forEachIndexed { idx, raw ->
        if (idx > 0) append("\n")
        var line = raw
        val heading = Regex("^(#{1,6})\\s+(.*)").find(line)
        if (heading != null) {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp)) { appendInline(heading.groupValues[2]) }
        } else if (line.trimStart().startsWith("- ") || line.trimStart().startsWith("* ")) {
            append("  •  "); appendInline(line.trimStart().drop(2))
        } else if (Regex("^\\d+\\.\\s").containsMatchIn(line.trimStart())) {
            appendInline(line)
        } else {
            appendInline(line)
        }
    }
}

private fun androidx.compose.ui.text.AnnotatedString.Builder.appendInline(text: String) {
    var i = 0
    while (i < text.length) {
        when {
            text.startsWith("**", i) -> {
                val end = text.indexOf("**", i + 2)
                if (end != -1) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(text.substring(i + 2, end)) }
                    i = end + 2
                } else { append(text[i]); i++ }
            }
            text.startsWith("`", i) -> {
                val end = text.indexOf("`", i + 1)
                if (end != -1) {
                    withStyle(SpanStyle(fontFamily = FontFamily.Monospace, background = Color(0x22000000))) {
                        append(text.substring(i + 1, end))
                    }
                    i = end + 1
                } else { append(text[i]); i++ }
            }
            text.startsWith("*", i) -> {
                val end = text.indexOf("*", i + 1)
                if (end != -1) {
                    withStyle(SpanStyle(textDecoration = TextDecoration.None, fontWeight = FontWeight.Medium)) {
                        append(text.substring(i + 1, end))
                    }
                    i = end + 1
                } else { append(text[i]); i++ }
            }
            else -> { append(text[i]); i++ }
        }
    }
}

@Composable
private fun CodeBlock(language: String, code: String) {
    val context = LocalContext.current
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF1E1E2E))
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(language, color = Color(0xFF9D7BFF), style = MaterialTheme.typography.labelSmall)
            Box(Modifier.weight(1f))
            Icon(
                Icons.Rounded.ContentCopy, "Copy code", tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp).clickableNoRipple { copyToClipboard(context, code) }
            )
        }
        Text(
            code,
            modifier = Modifier.horizontalScroll(rememberScrollState()).padding(12.dp),
            color = Color(0xFFE0E0E0),
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun MarkdownTable(rows: List<List<String>>) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        rows.forEachIndexed { idx, row ->
            Row(Modifier.fillMaxWidth().padding(8.dp)) {
                row.forEach { cell ->
                    Text(
                        cell,
                        modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (idx == 0) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText("code", text))
}
