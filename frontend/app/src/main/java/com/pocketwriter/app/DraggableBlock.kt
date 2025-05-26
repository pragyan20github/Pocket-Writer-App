package com.pocketwriter.app

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete

@Composable
fun DraggableBlock(
    block: TemplateBlock,
    onPositionChange: (Float, Float) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableFloatStateOf(block.x) }
    var offsetY by remember { mutableFloatStateOf(block.y) }

    val isFixed = block.blockType == BlockType.FIXED_TEXT || block.blockType == BlockType.FIXED_IMAGE

    Box(
        modifier = modifier
            .then(
                if (isFixed) Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                else Modifier.size(120.dp, 60.dp)
            )
            .pointerInput(block.id) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    if (isFixed) {
                        offsetY += dragAmount.y
                        onPositionChange(0f, offsetY)
                    } else {
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                        onPositionChange(offsetX, offsetY)
                    }
                }
            }
            .background(Color.LightGray.copy(alpha = 0.3f))
    ) {
        Card(modifier = if (isFixed) Modifier.fillMaxSize() else Modifier.size(120.dp, 60.dp)) {
            Box {
                when (block) {
                    is TemplateBlock.TextBlock -> Text(
                        block.label,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                            .fillMaxSize()
                            .padding(start = 12.dp, top = 12.dp)
                    )
                    is TemplateBlock.ImageBlock -> Text(
                        block.label,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
                            .fillMaxSize()
                            .padding(start = 12.dp, top = 12.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete Block")
                }
            }
        }
    }
}
