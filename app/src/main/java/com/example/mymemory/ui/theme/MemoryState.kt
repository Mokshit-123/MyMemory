package com.example.mymemory.ui.theme
import com.example.mymemory.data.Photo
data class MemoryState(
    val score:Int = 0,
    val moves:Int = 0,
    val cols:Int = 2,
    val gridSize:Int = cols * 4,
    val pairs:Int = gridSize/2,
    val images: List<Photo> = listOf(),
    val flippedImages : List<Photo> = listOf()
)
