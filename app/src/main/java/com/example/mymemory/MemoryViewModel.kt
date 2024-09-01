package com.example.mymemory

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import com.example.mymemory.data.Photo
import com.example.mymemory.data.photos
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface MemoryUiState{
    object startScreen : MemoryUiState
    object showingImages : MemoryUiState
    object gameScreen : MemoryUiState
    object finishScreen : MemoryUiState
}
class MemoryViewModel : ViewModel() {
    private val _memoryState = MutableStateFlow(MemoryState())
    val memoryState = _memoryState.asStateFlow()
    var memoryUiState:MemoryUiState by mutableStateOf(MemoryUiState.startScreen)
    var gameFinish  = false
    lateinit var result : String

    private var flippedPhotos = mutableListOf<Photo>() // Keep track of flipped photos

    init {
        Log.d("TAG", "INIT: INIT called")
        resetGame()
    }
    fun updateShuffledPhotos(level: Int) : List<Photo>{
        Log.d("Memory ViewModel", "updateShuffledPhotos: update photos $level")
        val pairs = pairs(level)
        val randomPhotos = photos.shuffled().take(pairs)

        // Create two separate instances of each photo with unique state
        val shuffledPhotos = randomPhotos.flatMap { originalPhoto ->
            listOf(
                Photo(
                    dummyImageResourceId = originalPhoto.dummyImageResourceId,
                    isFlipped = mutableStateOf(false), // New state instance for the duplicate
                ),
                Photo(
                    dummyImageResourceId = originalPhoto.dummyImageResourceId,
                    isFlipped = mutableStateOf(false) // New state instance for the original
                )
            )
        }.shuffled().toMutableList()
        Log.d("Memory ViewModel", "updateShuffledPhotos: updated photos")
        return shuffledPhotos
    }


    fun photoClicked(photo: Photo) : Boolean{
        if (photo.isFlipped.value || flippedPhotos.size == 2 || memoryUiState==MemoryUiState.showingImages) {
            Log.d("Memory ViewModel", "photoClicked: Click ignored")
            // Ignore clicks if the photo is already flipped or two photos are already flipped
            return true
        }

        // Flip the clicked photo
        Log.d("Memory ViewModel", "photoClicked: flipping photo ${photo}")
        photo.isFlipped.value = true
        flippedPhotos.add(photo)
        _memoryState.update {
            it.copy(moves = it.moves+1)
        }

        if (flippedPhotos.size == 2) {
            // Check if the two flipped photos match
            if (flippedPhotos[0].dummyImageResourceId == flippedPhotos[1].dummyImageResourceId) {
                Log.d("Memory ViewModel", "photoClicked: Photos matched")
                // It's a match, keep them flipped and clear the list
                flippedPhotos.clear()
                _memoryState.update {
                    it.copy(score = it.score+10)
                }
                allFlipped()
            } else {
                // Not a match, flip them back after a delay
                Log.d("Memory ViewModel", "photoClicked: photos didn't match")
                CoroutineScope(Dispatchers.Main).launch {
                    delay(250) // 0.25 second delay to show both images flipped
                    flippedPhotos.forEach { it.isFlipped.value = false }
                    flippedPhotos.clear()
                }
                return false
            }
        }
        return true
    }

    fun pairs(level: Int = memoryState.value.level): Int {
        return when (level) {
            1 -> 4
            2 -> 6
            3 -> 8
            else -> 4
        }
    }

    private fun allFlipped(){
        val pairs = pairs(memoryState.value.level)
        if(memoryState.value.score==pairs*10){
            memoryUiState=MemoryUiState.finishScreen
            gameFinish=true
            result = when(memoryState.value.moves){
                (pairs*2) -> "Good"
                ((pairs*2)+2) -> ""
                else->"Bad"
            }
        }
    }
    fun updateLevel(level: Int) {
        Log.d("Memory ViewModel", "updateLevel: $level")
        if (_memoryState.value.level != level) {
            _memoryState.update { it.copy(level = level) }
            resetGame(level)
        }
    }

    fun resetGame(level: Int = 1){
        Log.d("Memory ViewModel", "resetGame: RestGame $level")
        flippedPhotos.clear()
        val shufflePhotos = updateShuffledPhotos(level)
        _memoryState.update {
            MemoryState(level=level, shuffledPhotos = shufflePhotos)
        }
        gameFinish=false
        Log.d("Memory ViewModel", "resetGame: flipping images to show once")
        memoryUiState=MemoryUiState.showingImages
        CoroutineScope(Dispatchers.Main).launch{
            delay(100)
            _memoryState.value.shuffledPhotos.forEach{
                it.isFlipped.value=true
                delay(100)
            }
            delay(1000)
            _memoryState.value.shuffledPhotos.forEach {
                it.isFlipped.value=false
                delay(100)
            }
        }
        memoryUiState=MemoryUiState.gameScreen
        Log.d("TAG", "resetGame: complete ${_memoryState.value.level}")
    }
}