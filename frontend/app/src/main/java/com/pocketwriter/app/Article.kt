package com.pocketwriter.app

data class Template(
    val id: Long? = null,
    val name: String,
    val layoutJson: String
)

data class Article(
    val id: Long? = null,
    val title: String,
    val content: String,
    val template: Template?
)
