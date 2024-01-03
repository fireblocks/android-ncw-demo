package com.fireblocks.sdkdemo.ui.compose.components

import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.fireblocks.sdkdemo.ui.compose.FireblocksNCWDemoTheme

/**
 * Created by Fireblocks Ltd. on 18/09/2023
 */
@Composable
fun FireblocksText(modifier: Modifier = Modifier,
                   text: String? = null,
                   textStyle: TextStyle = FireblocksNCWDemoTheme.typography.b1,
                   textColor: Color = Color.White,
                   maxLines: Int = 50,
                   annotatedString: AnnotatedString? = null,
                   inlineContent: Map<String, InlineTextContent> = mapOf(),
                   textAlign: TextAlign = TextAlign.Start) {

    val finalText : AnnotatedString = when {
        annotatedString != null -> annotatedString
        text != null -> text.toAnnotatedString()
        else -> "".toAnnotatedString()
    }
    Text(
        text = finalText,
        style = textStyle,
        color = textColor,
        modifier = modifier.wrapContentHeight(),
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        textAlign = textAlign,
        inlineContent = inlineContent
    )
}

fun getSpannableText(spannableTexts : List<SpannableTextData>): AnnotatedString {
    return buildAnnotatedString {
        spannableTexts.forEach {
            withStyle(style = it.spanStyle) {
                append(it.text)
                if(it.addSpace) {
                    append(" ")
                }
            }
        }
    }
}

data class SpannableTextData(val text : String, val textStyle: TextStyle, val addSpace : Boolean = true) {
    val spanStyle : SpanStyle
        get() = textStyle.toSpanStyle()
}

fun String?.toAnnotatedString() : AnnotatedString {
    val sting = this ?: ""
    return AnnotatedString(sting)
}