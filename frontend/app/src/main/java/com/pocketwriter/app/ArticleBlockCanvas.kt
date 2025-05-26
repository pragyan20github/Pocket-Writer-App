package com.pocketwriter.app

// For Uri
import android.net.Uri

// For image picker and activity result
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

// For AsyncImage (Coil)
import coil.compose.AsyncImage

// For Compose UI elements
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@Composable
fun ArticleBlockCanvas(
    blocks: List<TemplateBlock>,
    blockInputs: Map<String, String>,
    onInputChange: (String, String) -> Unit,
    imageUris: Map<String, Uri?>,
    onImageUriChange: (String, Uri?) -> Unit
) {
    Box(
        modifier = Modifier
            .size(1000.dp, 1000.dp)
            .background(Color(0xFFF0F0F0))
            .border(1.dp, Color.LightGray)
            .verticalScroll(rememberScrollState())
            .horizontalScroll(rememberScrollState())
    ) {
        blocks.forEach { block ->
            val isFixed = when (block) {
                is TemplateBlock.TextBlock -> block.blockType == BlockType.FIXED_TEXT
                is TemplateBlock.ImageBlock -> block.blockType == BlockType.FIXED_IMAGE
            }
            when (block) {
                is TemplateBlock.TextBlock -> {
                    OutlinedTextField(
                        value = blockInputs[block.id] ?: "",
                        onValueChange = { onInputChange(block.id, it) },
                        label = { Text(block.label) },
                        modifier = if (isFixed)
                            Modifier
                                .fillMaxWidth()
                                .graphicsLayer(translationY = block.y)
                                .height(60.dp)
                        else
                            Modifier
                                .width(120.dp)
                                .graphicsLayer(
                                    translationX = block.x,
                                    translationY = block.y
                                )
                                .height(60.dp)
                    )
                }
                is TemplateBlock.ImageBlock -> {
                    val uri = imageUris[block.id]
                    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                        onImageUriChange(block.id, uri)
                    }
                    Box(
                        modifier = if (isFixed)
                            Modifier
                                .fillMaxWidth()
                                .graphicsLayer(translationY = block.y)
                                .height(60.dp)
                                .background(Color(0xFFE0E0E0))
                        else
                            Modifier
                                .width(120.dp)
                                .graphicsLayer(
                                    translationX = block.x,
                                    translationY = block.y
                                )
                                .height(60.dp)
                                .background(Color(0xFFE0E0E0))
                    ) {
                        if (uri != null) {
                            AsyncImage(
                                model = uri,
                                contentDescription = "Selected Image",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Button(
                                onClick = { launcher.launch("image/*") },
                                modifier = Modifier.align(Alignment.Center)
                            ) {
                                Text("Pick Image")
                            }
                        }
                    }
                }
            }
        }
    }
}
