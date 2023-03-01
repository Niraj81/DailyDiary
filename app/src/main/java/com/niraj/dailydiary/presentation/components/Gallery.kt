package com.niraj.dailydiary.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.w3c.dom.Text
import java.lang.Integer.max

@Composable
fun Gallery(
    modifier: Modifier = Modifier,
    images: List<String>,
    imageSize: Dp = 40.dp,
    spaceBetween: Dp = 10.dp,
    imageShape: CornerBasedShape = Shapes().small
){
    BoxWithConstraints {
        val numberOfVisibleImages = remember {
            derivedStateOf {
                max(
                    0,
                    this.maxHeight.div(spaceBetween + imageSize).toInt().minus(1)
                )
            }
        }

        val remainingImages = remember {
            derivedStateOf {
                images.size - numberOfVisibleImages.value
            }
        }

        Row {
            images.take(numberOfVisibleImages.value).forEach{ imageLink ->
                AsyncImage(
                    modifier = Modifier
                        .clip(imageShape)
                        .size(imageSize),
                    model = ImageRequest
                        .Builder(LocalContext.current)
                        .data(imageLink)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Gallery Image"
                )
            }
            if(remainingImages.value > 0){
                LastImageOverlay(
                    imageSize = imageSize,
                    imageShape = imageShape,
                    remainingImage = remainingImages.value
                )
            }
        }
    }
}

@Composable
fun LastImageOverlay(
    imageSize: Dp,
    imageShape: CornerBasedShape,
    remainingImage: Int
){
    Box(contentAlignment = Alignment.Center){
        Surface(
            modifier = Modifier
                .clip(imageShape)
                .size(imageSize),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {}
        Text(
            text = "+$remainingImage",
            style = TextStyle(
                fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}