package com.example.mymemory.ui.theme

import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import com.example.mymemory.data.Photo
import com.example.mymemory.data.photos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MemoryViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MemoryState())
    val uiState: StateFlow<MemoryState> = _uiState.asStateFlow()
    var shuffledPhotos: MutableList<Photo> = mutableListOf()
    private var turnedImages : MutableList<Photo> = mutableListOf()


    private fun pickRandomImagesAndShuffle(pairs:Int):MutableList<Photo> {

        val randomPhotos = photos.shuffled().take(pairs)
        shuffledPhotos = randomPhotos.flatMap { listOf(it, it.copy()) }.shuffled().toMutableList()
        shuffledPhotos.forEach{
            it.isFlipped=false
        }
        return shuffledPhotos
    }

    fun difficulty(level: String){
        val columns = when(level){
            "Easy"->2
            "Medium"->3
            "Hard"->4
            else->2
        }
        resetGame(columns=columns)
    }

    fun changeImage(photo : Photo, index:Int){

        _uiState.update { currentState->
            currentState.copy(moves =currentState.moves+1)
        }
        //checking if current photo is already flipped
        if(photo.isFlipped){
            //if photo is flipped and pair is not found
            if (!shuffledPhotos[index].pairFound){
                shuffledPhotos[index].isFlipped=false
                turnedImages.removeLast()
            }
        }
        else if(turnedImages.size == 0){
            shuffledPhotos[index].isFlipped = true
            turnedImages.add(photo)

        }
        else{

            shuffledPhotos[index].isFlipped=true
            if(turnedImages.last().dummyImageResourceId==photo.dummyImageResourceId){

                shuffledPhotos[index].pairFound=true
                val lastIndex = shuffledPhotos.indexOf(turnedImages.last())
                shuffledPhotos[lastIndex].pairFound=true
                turnedImages.removeLast()
                _uiState.update { currentState->
                    currentState.copy(score =currentState.score+10)
                }
            }
            else{
                val lastIndex = shuffledPhotos.indexOf(turnedImages.last())
                shuffledPhotos[lastIndex].isFlipped=false
                shuffledPhotos[index].isFlipped=false
                turnedImages.removeLast()

            }
        }
    }
    private fun isCorrectImage(){

    }
    fun resetGame(columns : Int = 2) {
        Log.d(TAG, "resetGame: Reset called, columns: $columns")
        shuffledPhotos.clear()
        Log.d(TAG, "resetGame: ${shuffledPhotos.size}")
        turnedImages.clear()
        _uiState.value = MemoryState(images = pickRandomImagesAndShuffle(columns*2), flippedImages = listOf(), cols = columns, pairs = 2*columns)
    }

    init {
        resetGame()
    }
}