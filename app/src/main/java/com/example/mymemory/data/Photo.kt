package com.example.mymemory.data

import androidx.annotation.DrawableRes
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

import com.example.mymemory.R

data class Photo(
    @DrawableRes val dummyImageResourceId : Int,
    var isFlipped : MutableState<Boolean>,
    var pairFound : Boolean = false
)
val photos = listOf(
    Photo(R.drawable.apple, mutableStateOf(false), false),
    Photo(R.drawable.banana,mutableStateOf(false), false),
    Photo(R.drawable.coffee,mutableStateOf(false), false),
    Photo(R.drawable.dumbbel,mutableStateOf(false), false),
    Photo(R.drawable.avocado,mutableStateOf(false), false),
    Photo(R.drawable.lemon,mutableStateOf(false), false),
    Photo(R.drawable.milk,mutableStateOf(false), false),
    Photo(R.drawable.watermelon,mutableStateOf(false), false)
)


