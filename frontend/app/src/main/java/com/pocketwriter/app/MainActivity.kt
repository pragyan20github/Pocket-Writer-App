package com.pocketwriter.app


// ALL IMPORTS:

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.pocketwriter.app.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import com.google.gson.JsonParser
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.pocketwriter.app.util.resolveImageUrl
import android.util.Log
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import com.google.gson.Gson
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.filled.ArrowBack
import com.pocketwriter.app.ui.theme.Pink80


// ARTICLE FEED SCREEN

@Composable
fun ArticleFeedScreen(
    onArticleClick: (Long) -> Unit,
    onAddArticleClick: () -> Unit,
    onTemplatesClick: () -> Unit
) {
    var articles by remember { mutableStateOf<List<Article>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            try {
                val result = ApiClient.retrofitService.getArticles()
                articles = result
                isLoading = false
            } catch (e: Exception) {
                errorMessage = "Failed to load articles: ${e.localizedMessage}"
                isLoading = false
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            Row(
                modifier = Modifier.padding(end = 16.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                FloatingActionButton(onClick = onAddArticleClick) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Article")
                }
                Spacer(modifier = Modifier.width(16.dp))
                FloatingActionButton(onClick = onTemplatesClick) {
                    Icon(Icons.Filled.Edit, contentDescription = "Templates")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                errorMessage != null -> {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(articles) { article ->
                            // --- Begin: Thumbnail and Preview Extraction ---
                            val blocks =
                                article.template?.layoutJson?.let { parseBlocksFromJson(it) }
                                    ?: emptyList()
                            val contentMap: Map<String, String> = try {
                                Gson().fromJson(article.content, Map::class.java)
                                    .mapKeys { it.key.toString() }
                                    .mapValues { it.value?.toString() ?: "" }
                            } catch (_: Exception) {
                                emptyMap()
                            }

                            // Thumbnail: first image block's URL (if any)
                            val thumbnailUrl = blocks.filterIsInstance<TemplateBlock.ImageBlock>()
                                .mapNotNull { contentMap[it.id] }
                                .firstOrNull { it.isNotBlank() }

                            // Preview: first text block's value (if any)
                            val previewText = blocks.filterIsInstance<TemplateBlock.TextBlock>()
                                .mapNotNull { contentMap[it.id] }
                                .firstOrNull { it.isNotBlank() }
                                ?.take(100) ?: "No preview available"
                            // --- End: Thumbnail and Preview Extraction ---

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .clickable { onArticleClick(article.id ?: 0L) },
                                elevation = CardDefaults.cardElevation(8.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(modifier = Modifier.padding(16.dp)) {
                                    if (thumbnailUrl != null) {
                                        AsyncImage(
                                            model = resolveImageUrl(thumbnailUrl),
                                            contentDescription = "Thumbnail",
                                            modifier = Modifier
                                                .size(64.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Image(
                                            painter = painterResource(id = R.drawable.default_thumbnail),
                                            contentDescription = "Default Thumbnail",
                                            modifier = Modifier
                                                .size(64.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = article.title,
                                            style = MaterialTheme.typography.titleLarge,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = previewText,
                                            style = MaterialTheme.typography.bodyMedium,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}



// ARTICLE DETAIL SCREEN

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(articleId: Long, onBack: () -> Unit, onEdit: () -> Unit) {

    var article by remember { mutableStateOf<Article?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }
    var deleteSuccess by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(articleId) {
        scope.launch(Dispatchers.IO) {
            try {
                val result = ApiClient.retrofitService.getArticleById(articleId)
                article = result
                isLoading = false
            } catch (e: Exception) {
                errorMessage = "Failed to load article: ${e.localizedMessage}"
                isLoading = false
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Article") },
            text = { Text("Are you sure you want to delete this article? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        deleting = true
                        scope.launch(Dispatchers.IO) {
                            try {
                                val response =
                                    ApiClient.retrofitService.deleteArticle(articleId)
                                if (response.isSuccessful) {
                                    deleting = false
                                    deleteSuccess = true
                                } else {
                                    deleting = false
                                    errorMessage =
                                        "Failed to delete article: ${response.code()} ${response.message()}"
                                }
                            } catch (e: Exception) {
                                deleting = false
                                errorMessage = "Failed to delete article: ${e.localizedMessage}"
                            }
                        }
                    }
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (deleteSuccess) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(1000)
            onBack()
        }
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text("Article deleted.", color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Article Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit Article")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete Article")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when {
                isLoading || deleting -> CircularProgressIndicator(
                    modifier = Modifier.align(
                        androidx.compose.ui.Alignment.Center
                    )
                )

                errorMessage != null -> Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )

                article != null -> {
                    val currentArticle = article!!
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = currentArticle.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        val template = currentArticle.template
                        val content = currentArticle.content ?: ""
                        var showedTemplateFields = false
                        if (template != null && content.trim().startsWith("{")) {
                            val blocks = template.layoutJson?.let { parseBlocksFromJson(it) }
                                ?: emptyList()
                            val contentMap: Map<String, String> = try {
                                Gson().fromJson(content, Map::class.java)
                                    .mapKeys { it.key.toString() }
                                    .mapValues { it.value?.toString() ?: "" }
                            } catch (_: Exception) {
                                emptyMap()
                            }
                            if (blocks.isNotEmpty() && contentMap.isNotEmpty()) {
                                showedTemplateFields = true
                                blocks.forEach { block ->
                                    when (block) {
                                        is TemplateBlock.TextBlock -> {
                                            val value = contentMap[block.id].orEmpty()
                                            if (value.isNotBlank()) {
                                                Text(
                                                    text = value,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    modifier = Modifier.padding(bottom = 8.dp)
                                                )
                                            }
                                        }

                                        is TemplateBlock.ImageBlock -> {
                                            val imageUrl = contentMap[block.id].orEmpty()
                                            if (imageUrl.isNotBlank()) {
                                                Log.d(
                                                    "ArticleImageDebug",
                                                    "Image block value: '$imageUrl'"
                                                )
                                                AsyncImage(
                                                    model = resolveImageUrl(imageUrl),
                                                    contentDescription = "Article Image",
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(200.dp)
                                                        .padding(bottom = 12.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (!showedTemplateFields) {
                            Text(text = content, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}



// ADD ARTICLE SCREEN
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddArticleScreen(onBack: () -> Unit) {

    var title by remember { mutableStateOf("") }
    var templates by remember { mutableStateOf<List<Template>>(emptyList()) }
    var selectedTemplate by remember { mutableStateOf<Template?>(null) }
    var blockInputs by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var imageUris by remember { mutableStateOf<Map<String, Uri?>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var success by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // --- NEW STATE FOR IMAGE SOURCE DIALOG AND URL INPUT ---
    var showImageSourceDialog by remember { mutableStateOf<String?>(null) }
    var showUrlInputDialog by remember { mutableStateOf<String?>(null) }
    var urlInput by remember { mutableStateOf("") }
    var imagePickerBlockId by remember { mutableStateOf<String?>(null) }

    // Device image picker launcher (top-level, not inside any lambda)
    val deviceImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imagePickerBlockId?.let { blockId ->
            if (uri != null) {
                imageUris = imageUris.toMutableMap().apply { put(blockId, uri) }
                blockInputs = blockInputs.toMutableMap().apply { remove(blockId) }
            }
            imagePickerBlockId = null
        }
    }

    // Fetch templates on screen load
    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            try {
                templates = ApiClient.retrofitService.getTemplates()
            } catch (e: Exception) {
                errorMessage = "Failed to load templates: ${e.localizedMessage}"
            }
        }
    }

    val templateBlocks = remember(selectedTemplate) {
        selectedTemplate?.layoutJson?.let { parseBlocksFromJson(it) } ?: emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Article") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (success) {
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(1000)
                    onBack()
                }
                Text(
                    "Article added successfully!",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .align(Alignment.TopCenter)
                        .verticalScroll(scrollState)
                ) {
                    if (errorMessage != null) {
                        Text(
                            errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    // Template Dropdown
                    if (templates.isNotEmpty()) {
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = selectedTemplate?.name ?: "Select Template",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Template") },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                templates.forEach { template ->
                                    DropdownMenuItem(
                                        text = { Text(template.name) },
                                        onClick = {
                                            selectedTemplate = template
                                            expanded = false
                                            blockInputs = emptyMap()
                                            imageUris = emptyMap()
                                        }
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    // Render input fields for each block in the template
                    templateBlocks.forEach { block ->
                        when (block) {
                            is TemplateBlock.TextBlock -> {
                                OutlinedTextField(
                                    value = blockInputs[block.id] ?: "",
                                    onValueChange = { newValue ->
                                        blockInputs = blockInputs.toMutableMap().apply { put(block.id, newValue) }
                                    },
                                    label = { Text(block.label) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp)
                                )
                            }
                            is TemplateBlock.ImageBlock -> {
                                val uri = imageUris[block.id]
                                val url = blockInputs[block.id]
                                Column(modifier = Modifier.padding(bottom = 12.dp)) {
                                    Button(
                                        onClick = { showImageSourceDialog = block.id },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Pink80, // Change this to your desired color
//                                            contentColor = MaterialTheme.colorScheme.onPrimary  // For the text/icon color
                                        )
                                    ) {
                                        Text(
                                            when {
                                                uri != null || !url.isNullOrBlank() -> "Change Image"
                                                else -> "Add Image"
                                            }
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    when {
                                        uri != null -> AsyncImage(
                                            model = uri,
                                            contentDescription = "Selected Image",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(180.dp)
                                        )
                                        !url.isNullOrBlank() -> AsyncImage(
                                            model = url,
                                            contentDescription = "Image from Web",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(180.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            scope.launch(Dispatchers.IO) {
                                if (title.isBlank()) {
                                    errorMessage = "Title is required."
                                    return@launch
                                }
                                if (selectedTemplate != null && templateBlocks.any { it is TemplateBlock.TextBlock && (blockInputs[it.id].isNullOrBlank()) }) {
                                    errorMessage = "Please fill all template fields."
                                    return@launch
                                }
                                errorMessage = null
                                isLoading = true
                                val updatedInputs = blockInputs.toMutableMap()
                                for (block in templateBlocks) {
                                    if (block is TemplateBlock.ImageBlock) {
                                        val uri = imageUris[block.id]
                                        val url = blockInputs[block.id]
                                        if (uri != null) {
                                            try {
                                                val inputStream = context.contentResolver.openInputStream(uri)
                                                val tempFile = File.createTempFile("upload", ".jpg", context.cacheDir)
                                                val outputStream = FileOutputStream(tempFile)
                                                inputStream?.copyTo(outputStream)
                                                inputStream?.close()
                                                outputStream.close()
                                                val requestFile = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
                                                val body = MultipartBody.Part.createFormData("file", tempFile.name, requestFile)
                                                val response = ApiClient.retrofitService.uploadImage(body)
                                                val imageUrl = response.url
                                                updatedInputs[block.id] = imageUrl
                                                tempFile.delete()
                                            } catch (e: Exception) {
                                                isLoading = false
                                                errorMessage = "Failed to upload image: ${e.localizedMessage}"
                                                return@launch
                                            }
                                        } else if (!url.isNullOrBlank()) {
                                            updatedInputs[block.id] = url
                                        }
                                    }
                                }
                                try {
                                    val contentJson = Gson().toJson(updatedInputs)
                                    val newArticle = ArticleCreateRequest(
                                        title = title,
                                        content = contentJson,
                                        template = selectedTemplate
                                    )
                                    ApiClient.retrofitService.createArticle(newArticle)
                                    isLoading = false
                                    success = true
                                } catch (e: Exception) {
                                    isLoading = false
                                    errorMessage = "Failed to add article: ${e.localizedMessage}"
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }

    // --- DIALOG FOR IMAGE SOURCE SELECTION ---
    if (showImageSourceDialog != null) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = null },
            title = { Text("Select Image Source") },
            text = { Text("Choose an image from your device or enter a web URL.") },
            confirmButton = {
                Column(Modifier.padding(16.dp)) {
                    Button(
                        onClick = {
                            imagePickerBlockId = showImageSourceDialog
                            deviceImageLauncher.launch("image/*")
                            showImageSourceDialog = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("From Device")
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            showUrlInputDialog = showImageSourceDialog
                            showImageSourceDialog = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("From Web")
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { showImageSourceDialog = null },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel")
                    }
                }
            },
            dismissButton = {}
        )
    }

    // --- DIALOG FOR IMAGE URL INPUT ---
    if (showUrlInputDialog != null) {
        AlertDialog(
            onDismissRequest = { showUrlInputDialog = null },
            title = { Text("Paste Image URL") },
            text = {
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    label = { Text("Image URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        blockInputs = blockInputs.toMutableMap().apply { put(showUrlInputDialog!!, urlInput) }
                        imageUris = imageUris.toMutableMap().apply { remove(showUrlInputDialog!!) }
                        urlInput = ""
                        showUrlInputDialog = null
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showUrlInputDialog = null }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}




// EDIT ARTICLE SCREEN

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditArticleScreen(articleId: Long, onBack: () -> Unit) {

    var title by remember { mutableStateOf("") }
    var templates by remember { mutableStateOf<List<Template>>(emptyList()) }
    var selectedTemplate by remember { mutableStateOf<Template?>(null) }
    var blockInputs by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var imageUris by remember { mutableStateOf<Map<String, Uri?>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var success by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Load the existing article and templates
    LaunchedEffect(articleId) {
        scope.launch(Dispatchers.IO) {
            try {
                val article = ApiClient.retrofitService.getArticleById(articleId)
                title = article.title
                selectedTemplate = article.template
                val loadedTemplates = ApiClient.retrofitService.getTemplates()
                templates = loadedTemplates

                // Parse blocks and fill initial values from article content (if present)
                val blocks =
                    article.template?.layoutJson?.let { parseBlocksFromJson(it) } ?: emptyList()
                val initialInputs = mutableMapOf<String, String>()
                if (article.content.isNotBlank()) {
                    try {
                        val map: Map<String, String> =
                            Gson().fromJson(article.content, Map::class.java)
                                .mapKeys { it.key.toString() }
                                .mapValues { it.value?.toString() ?: "" }
                        for (block in blocks) {
                            val id = when (block) {
                                is TemplateBlock.TextBlock -> block.id
                                is TemplateBlock.ImageBlock -> block.id
                            }
                            initialInputs[id] = map[id] ?: ""
                        }
                    } catch (_: Exception) { /* ignore parse errors */
                    }
                }
                blockInputs = initialInputs
                isLoading = false
            } catch (e: Exception) {
                errorMessage = "Failed to load article/templates: ${e.localizedMessage}"
                isLoading = false
            }
        }
    }

    // Parse blocks from selected template
    val templateBlocks = remember(selectedTemplate) {
        selectedTemplate?.layoutJson?.let { parseBlocksFromJson(it) } ?: emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Article") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(androidx.compose.ui.Alignment.Center))
            } else if (success) {
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(1000)
                    onBack()
                }
                Text(
                    "Article updated successfully!",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(androidx.compose.ui.Alignment.Center)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .align(androidx.compose.ui.Alignment.TopCenter)
                ) {
                    if (errorMessage != null) {
                        Text(
                            errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    // Template Dropdown
                    if (templates.isNotEmpty()) {
                        var expanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = selectedTemplate?.name ?: "Select Template",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Template") },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = expanded
                                    )
                                }
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                templates.forEach { template ->
                                    DropdownMenuItem(
                                        text = { Text(template.name) },
                                        onClick = {
                                            selectedTemplate = template
                                            expanded = false
                                            blockInputs =
                                                emptyMap() // Reset block inputs when template changes
                                            imageUris = emptyMap()
                                        }
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    // Render input fields for each block in the template
                    templateBlocks.forEach { block ->
                        when (block) {
                            is TemplateBlock.TextBlock -> {
                                OutlinedTextField(
                                    value = blockInputs[block.id] ?: "",
                                    onValueChange = { newValue ->
                                        blockInputs = blockInputs.toMutableMap()
                                            .apply { put(block.id, newValue) }
                                    },
                                    label = { Text(block.label) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp)
                                )
                            }

                            is TemplateBlock.ImageBlock -> {
                                val existingUrl = blockInputs[block.id] ?: ""
                                var uri by remember { mutableStateOf<Uri?>(null) }
                                val launcher =
                                    rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { pickedUri: Uri? ->
                                        uri = pickedUri
                                    }
                                Column(modifier = Modifier.padding(bottom = 12.dp)) {
                                    Button(onClick = { launcher.launch("image/*") }) {
                                        Text(if (uri == null && existingUrl.isBlank()) "Pick Image" else "Change Image")
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    when {
                                        uri != null -> AsyncImage(
                                            model = uri,
                                            contentDescription = "Selected Image",
                                            modifier = Modifier.fillMaxWidth().height(180.dp)
                                        )

                                        existingUrl.isNotBlank() -> AsyncImage(
                                            model = resolveImageUrl(existingUrl),
                                            contentDescription = "Existing Image",
                                            modifier = Modifier.fillMaxWidth().height(180.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                errorMessage = "Title is required."
                                return@Button
                            }
                            if (selectedTemplate != null && templateBlocks.any { it is TemplateBlock.TextBlock && (blockInputs[it.id].isNullOrBlank()) }) {
                                errorMessage = "Please fill all template fields."
                                return@Button
                            }
                            errorMessage = null
                            isLoading = true
                            scope.launch(Dispatchers.IO) {
                                val updatedInputs = blockInputs.toMutableMap()
                                // --- IMAGE UPLOAD LOGIC ---
                                for (block in templateBlocks) {
                                    if (block is TemplateBlock.ImageBlock) {
                                        // Only upload if user picked a new image
                                        val uri = imageUris[block.id]
                                        if (uri != null) {
                                            try {
                                                val inputStream =
                                                    context.contentResolver.openInputStream(uri)
                                                val tempFile = File.createTempFile(
                                                    "upload",
                                                    ".jpg",
                                                    context.cacheDir
                                                )
                                                val outputStream = FileOutputStream(tempFile)
                                                inputStream?.copyTo(outputStream)
                                                inputStream?.close()
                                                outputStream.close()
                                                val requestFile =
                                                    tempFile.asRequestBody("image/*".toMediaTypeOrNull())
                                                val body = MultipartBody.Part.createFormData(
                                                    "file",
                                                    tempFile.name,
                                                    requestFile
                                                )
                                                val response =
                                                    ApiClient.retrofitService.uploadImage(body)
                                                val imageUrl = response.url
                                                updatedInputs[block.id] = imageUrl
                                                tempFile.delete()
                                            } catch (e: Exception) {
                                                isLoading = false
                                                errorMessage =
                                                    "Failed to upload image: ${e.localizedMessage}"
                                                return@launch
                                            }
                                        }
                                    }
                                }
                                // --- END IMAGE UPLOAD LOGIC ---
                                try {
                                    val contentJson = Gson().toJson(updatedInputs)
                                    val updatedArticle = ArticleCreateRequest(
                                        title = title,
                                        content = contentJson,
                                        template = selectedTemplate
                                    )
                                    ApiClient.retrofitService.updateArticle(
                                        articleId,
                                        updatedArticle
                                    )
                                    isLoading = false
                                    success = true
                                } catch (e: Exception) {
                                    isLoading = false
                                    errorMessage =
                                        "Failed to update article: ${e.localizedMessage}"
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Update")
                    }
                }
            }
        }
    }
}




//TEMPLATE FEED SCREEN

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateFeedScreen(
    onBack: () -> Unit,
    onEditTemplateClick: (Long) -> Unit,
    onOpenBuilder: () -> Unit
) {

    var templates by remember { mutableStateOf<List<Template>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Load templates when entering the screen
    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            try {
                templates = ApiClient.retrofitService.getTemplates()
                isLoading = false
            } catch (e: Exception) {
                errorMessage = "Failed to load templates: ${e.localizedMessage}"
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Templates") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onOpenBuilder) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = "Create Template (Builder)"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.align(androidx.compose.ui.Alignment.Center))
                errorMessage != null -> Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )

                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(templates) { template ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .clickable { onEditTemplateClick(template.id ?: 0L) },
                                elevation = CardDefaults.cardElevation(8.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = template.name,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}



// EDIT TEMPLATE SCREEN

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTemplateScreen(templateId: Long, onBack: () -> Unit) {

    var template by remember { mutableStateOf<Template?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isDeleting by remember { mutableStateOf(false) }
    var deleteSuccess by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Load template data
    LaunchedEffect(templateId) {
        scope.launch(Dispatchers.IO) {
            try {
                val loaded = ApiClient.retrofitService.getTemplateById(templateId)
                template = loaded
                isLoading = false
            } catch (e: Exception) {
                errorMessage = "Failed to load template: ${e.localizedMessage}"
                isLoading = false
            }
        }
    }

    if (deleteSuccess) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(1000)
            onBack()
        }
        Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            Text("Template deleted.", color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Template") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        isDeleting = true
                        scope.launch(Dispatchers.IO) {
                            try {
                                val response =
                                    ApiClient.retrofitService.deleteTemplate(templateId)
                                if (response.isSuccessful) {
                                    isDeleting = false
                                    deleteSuccess = true
                                } else {
                                    isDeleting = false
                                    errorMessage =
                                        "Failed to delete template: ${response.code()} ${response.message()}"
                                }
                            } catch (e: Exception) {
                                isDeleting = false
                                errorMessage =
                                    "Failed to delete template: ${e.localizedMessage}"
                            }
                        }
                    }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete Template")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(Modifier.padding(innerPadding).fillMaxSize()) {
            when {
                isLoading || isDeleting -> CircularProgressIndicator(
                    modifier = Modifier.align(
                        androidx.compose.ui.Alignment.Center
                    )
                )

                errorMessage != null -> Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(androidx.compose.ui.Alignment.Center)
                )

                template != null -> {
                    TemplateBuilderScreen(
                        initialName = template!!.name,
                        initialBlocks = parseBlocksFromJson(template!!.layoutJson),
                        onSave = { name, blocks ->
                            scope.launch(Dispatchers.IO) {
                                try {
                                    val layoutJson = serializeBlocks(blocks)
                                    val updatedTemplate =
                                        template!!.copy(name = name, layoutJson = layoutJson)
                                    ApiClient.retrofitService.updateTemplate(
                                        templateId,
                                        updatedTemplate
                                    )
                                    withContext(Dispatchers.Main) { onBack() }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        errorMessage =
                                            "Failed to update template: ${e.localizedMessage}"
                                    }
                                }
                            }
                        },
                        onCancel = { onBack() }
                    )
                }
            }
        }
    }
}



// TEMPLATE BUILDER SCREEN

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateBuilderScreen(
    initialName: String = "",
    initialBlocks: List<TemplateBlock> = emptyList(),
    onSave: (String, List<TemplateBlock>) -> Unit,
    onCancel: () -> Unit
) {
    var templateName by remember { mutableStateOf(initialName) }
    var blocks by remember { mutableStateOf(initialBlocks) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Template Builder") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cancel")
                    }
                }
            )
        }
    ) { innerPadding ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(24.dp)
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            OutlinedTextField(
                value = templateName,
                onValueChange = { templateName = it },
                label = { Text("Template Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row {
                Button(onClick = {
                    blocks = blocks + TemplateBlock.TextBlock(
                        id = java.util.UUID.randomUUID().toString(),
                        label = "Text Block",
                        blockType = BlockType.FREE_TEXT // <-- required!
                    )
                }) { Text("Add Text Block") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    blocks = blocks + TemplateBlock.ImageBlock(
                        id = java.util.UUID.randomUUID().toString(),
                        label = "Image Block",
                        blockType = BlockType.FREE_IMAGE // <-- required!
                    )
                }) { Text("Add Image Block") }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Blocks:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 0.dp, max = 300.dp)
            ) {
                LazyColumn {
                    items(blocks.size) { idx ->
                        val block = blocks[idx]
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                modifier = Modifier.padding(8.dp)
                            ) {
                                Text(
                                    when (block) {
                                        is TemplateBlock.TextBlock -> "Text: ${block.label}"
                                        is TemplateBlock.ImageBlock -> "Image: ${block.label}"
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = {
                                    blocks = blocks.toMutableList().also { it.removeAt(idx) }
                                }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Remove Block")
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            if (errorMessage != null) {
                Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
            }
            Button(
                onClick = {
                    if (templateName.isBlank() || blocks.isEmpty()) {
                        errorMessage = "Template name and at least one block required."
                    } else {
                        errorMessage = null
                        onSave(templateName, blocks)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Template")
            }
        }
    }
}



// FUNCTION FOR PASSING BLOCKS FROM JSON

fun parseBlocksFromJson(layoutJson: String): List<TemplateBlock> {
    val blocks = mutableListOf<TemplateBlock>()
    val jsonArray = JsonParser.parseString(layoutJson).asJsonArray
    for (element in jsonArray) {
        val obj = element.asJsonObject
        val type = obj["type"].asString
        val id = obj["id"].asString
        val label = obj["label"].asString
        // Default to FREE types if not specified in JSON
        when (type) {
            "text" -> blocks.add(
                TemplateBlock.TextBlock(
                    id = id,
                    label = label,
                    blockType = BlockType.FREE_TEXT
                )
            )
            "image" -> blocks.add(
                TemplateBlock.ImageBlock(
                    id = id,
                    label = label,
                    blockType = BlockType.FREE_IMAGE
                )
            )
        }
    }
    return blocks
}



// FUNCTION FOR SERIALIZING THE BLOCKS

fun serializeBlocks(blocks: List<TemplateBlock>): String {
    val gson = Gson()
    val jsonArray = JsonArray()
    blocks.forEach { block ->
        val obj = JsonObject()
        when (block) {
            is TemplateBlock.TextBlock -> {
                obj.addProperty("type", "text")
                obj.addProperty("id", block.id)
                obj.addProperty("label", block.label)
            }
            is TemplateBlock.ImageBlock -> {
                obj.addProperty("type", "image")
                obj.addProperty("id", block.id)
                obj.addProperty("label", block.label)
            }
        }
        jsonArray.add(obj)
    }
    return gson.toJson(jsonArray)
}




// THE MAIN ACTIVITY CLASS:

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                NavHost(navController, startDestination = "feed") {
                    composable("feed") {
                        ArticleFeedScreen(
                            onArticleClick = { articleId ->
                                navController.navigate("details/$articleId")
                            },
                            onAddArticleClick = {
                                navController.navigate("addArticle")
                            },
                            onTemplatesClick = {
                                navController.navigate("templates")
                            }
                        )
                    }
                    composable(
                        "details/{articleId}",
                        arguments = listOf(navArgument("articleId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val articleId = backStackEntry.arguments?.getLong("articleId") ?: 0L
                        ArticleDetailScreen(
                            articleId = articleId,
                            onBack = { navController.popBackStack() },
                            onEdit = { navController.navigate("editArticle/$articleId") }
                        )
                    }
                    // --- ONLY THIS LINE IS CHANGED ---
                    composable("addArticle") {
                        AddArticleScreen(onBack = { navController.popBackStack() })
                    }
                    composable(
                        "editArticle/{articleId}",
                        arguments = listOf(navArgument("articleId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val articleId = backStackEntry.arguments?.getLong("articleId") ?: 0L
                        EditArticleScreen(articleId = articleId, onBack = { navController.popBackStack() })
                    }
                    // Template CRUD screens
                    composable("templates") {
                        TemplateFeedScreen(
                            onBack = { navController.popBackStack() },
                            onEditTemplateClick = { templateId -> navController.navigate("editTemplate/$templateId") },
                            onOpenBuilder = { navController.navigate("templateBuilder") }
                        )
                    }
                    composable(
                        "editTemplate/{templateId}",
                        arguments = listOf(navArgument("templateId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val templateId = backStackEntry.arguments?.getLong("templateId") ?: 0L
                        EditTemplateScreen(templateId = templateId, onBack = { navController.popBackStack() })
                    }
                    composable("templateBuilder") {
                        val scope = rememberCoroutineScope()
                        var isSavingTemplate by remember { mutableStateOf(false) }
                        var saveTemplateError by remember { mutableStateOf<String?>(null) }

                        TemplateBuilderScreen(
                            onSave = { name, blocks ->
                                isSavingTemplate = true
                                saveTemplateError = null
                                scope.launch(Dispatchers.IO) {
                                    try {
                                        val layoutJson = serializeBlocks(blocks)
                                        val newTemplate = Template(
                                            id = null,
                                            name = name,
                                            layoutJson = layoutJson
                                        )
                                        ApiClient.retrofitService.createTemplate(newTemplate)
                                        withContext(Dispatchers.Main) {
                                            isSavingTemplate = false
                                            navController.popBackStack()
                                        }
                                    } catch (e: Exception) {
                                        withContext(Dispatchers.Main) {
                                            isSavingTemplate = false
                                            saveTemplateError = "Failed to save template: ${e.localizedMessage}"
                                        }
                                    }
                                }
                            },
                            onCancel = { navController.popBackStack() }
                        )
                        if (isSavingTemplate) {
                            Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                        if (saveTemplateError != null) {
                            Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                                Text(saveTemplateError!!, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}
