package com.example.mymemory
import com.example.mymemory.data.Photo
data class MemoryState(
    val level : Int = 1,
    val score :Int = 0,
    val moves:Int = 0,
    val shuffledPhotos : List<Photo> = listOf()
)