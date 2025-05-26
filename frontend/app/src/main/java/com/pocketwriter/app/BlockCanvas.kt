package com.pocketwriter.app

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@Composable
fun BlockCanvas(
    blocks: List<TemplateBlock>,
    onBlockPositionChange: (String, Float, Float) -> Unit,
    onDelete: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F0F0))
            .border(1.dp, Color.LightGray)
            .verticalScroll(rememberScrollState())
            .horizontalScroll(rememberScrollState())
    ) {
        blocks.forEach { block ->
            val isFixed = block.blockType == BlockType.FIXED_TEXT || block.blockType == BlockType.FIXED_IMAGE
            DraggableBlock(
                block = block,
                onPositionChange = { newX, newY ->
                    onBlockPositionChange(block.id, newX, newY)
                },
                onDelete = { onDelete(block.id) },
                modifier = if (isFixed)
                    Modifier.graphicsLayer(translationY = block.y)
                else
                    Modifier.graphicsLayer(translationX = block.x, translationY = block.y)
            )
        }
    }
}
