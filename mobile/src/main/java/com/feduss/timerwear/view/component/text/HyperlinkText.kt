package com.feduss.buswear.view.component.text

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.TextUnit
import com.feduss.timerwear.uistate.extension.Orange

@Composable
fun HyperlinkText(
    modifier: Modifier = Modifier,
    text: String,
    textAlign: TextAlign = TextAlign.Unspecified,
    linkText: List<String>,
    hyperlinks: List<String>,
    linkTextColor: Color = Color.Orange,
    linkTextFontWeight: FontWeight = FontWeight.Normal,
    linkTextDecoration: TextDecoration = TextDecoration.Underline,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontFamily: FontFamily = FontFamily.Default
) {
    val uriHandler = LocalUriHandler.current

    val annotatedString = buildAnnotatedString {
        var lastIndex = 0
        linkText.forEachIndexed { index, link ->
            val startIndex = text.indexOf(link, lastIndex)
            val endIndex = startIndex + link.length

            if (startIndex > lastIndex) append(text.substring(lastIndex, startIndex))

            val linkUrL = LinkAnnotation.Url(
                hyperlinks[index], TextLinkStyles(
                    SpanStyle(
                        color = linkTextColor,
                        fontSize = fontSize,
                        fontWeight = linkTextFontWeight,
                        textDecoration = linkTextDecoration,
                        fontFamily = fontFamily
                    )
                )
            ) {
                val url = (it as LinkAnnotation.Url).url
                uriHandler.openUri(url)
            }
            withLink(linkUrL) { append(link) }
            append(" ")
            lastIndex = endIndex + 1
        }
        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
        }
        addStyle(
            style = SpanStyle(
                fontSize = fontSize, fontFamily = fontFamily
            ), start = 0, end = text.length
        )
    }
    Text(
        modifier = modifier,
        text = annotatedString,
        textAlign = textAlign
    )
}