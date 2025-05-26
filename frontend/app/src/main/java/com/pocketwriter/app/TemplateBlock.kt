package com.pocketwriter.app

enum class BlockType { FIXED_TEXT, FIXED_IMAGE, FREE_TEXT, FREE_IMAGE }

sealed class TemplateBlock {
    abstract val id: String
    abstract val label: String
    abstract val x: Float
    abstract val y: Float
    abstract val blockType: BlockType

    data class TextBlock(
        override val id: String,
        override val label: String = "Text",
        override val x: Float = 0f,
        override val y: Float = 0f,
        override val blockType: BlockType
    ) : TemplateBlock()

    data class ImageBlock(
        override val id: String,
        override val label: String = "Image",
        override val x: Float = 0f,
        override val y: Float = 0f,
        override val blockType: BlockType
    ) : TemplateBlock()
}
