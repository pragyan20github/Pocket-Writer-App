package com.pocketwriter.app

data class ArticleCreateRequest(
    val title: String,
    val content: String,
    val template: Template? = null
)
