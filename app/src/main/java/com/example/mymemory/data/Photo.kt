package com.example.mymemory.data

import androidx.annotation.DrawableRes

import com.example.mymemory.R

data class Photo(
    @DrawableRes val dummyImageResourceId : Int,
    var isFlipped : Boolean,
    var pairFound : Boolean
)
val photos = listOf<Photo>(
    Photo(R.drawable.brave, false, false),
    Photo(R.drawable.chatgpt,false, false),
    Photo(R.drawable.chrome,false, false),
    Photo(R.drawable.codechef,false, false),
    Photo(R.drawable.codeforces,false, false),
    Photo(R.drawable.gemini,false, false),
    Photo(R.drawable.google,false, false),
    Photo(R.drawable.leetcode,false, false)
)


