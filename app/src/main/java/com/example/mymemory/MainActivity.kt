package com.example.mymemory

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mymemory.ui.theme.MemoryViewModel
import com.example.mymemory.ui.theme.MyMemoryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyMemoryTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Main()
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Main(
    memoryViewModel : MemoryViewModel = viewModel()
) {
    val memoryUiState by memoryViewModel.uiState.collectAsState()
    var mDisplayMenu by remember { mutableStateOf(false) }
    var mSettingMenu by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current
    //val mContext = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Memory Quiz",
                        fontSize = 25.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { },
                        content = { Icon(imageVector = Icons.Default.Home, contentDescription = "") }
                    )
                },
                actions = {
                    IconButton(onClick = {mDisplayMenu=!mDisplayMenu}){
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "")
                    }
                    DropdownMenu(
                        expanded = mDisplayMenu,
                        onDismissRequest = { mDisplayMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(text = "Settings")},
                            onClick = {mSettingMenu=!mSettingMenu},
                            leadingIcon = {
                                Icon(imageVector = Icons.Outlined.Settings, contentDescription = "")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(text = "Star this repo")},
                            onClick = { uriHandler.openUri("https://github.com/Mokshit-123/TestCaseGenerator")},
                            leadingIcon = { Icon(painterResource(id = R.drawable.baseline_star_outline_24), contentDescription = "")}
                        )
                        DropdownMenuItem(
                            text = { Text(text = "Reset")},
                            onClick = { memoryViewModel.resetGame() },
                            leadingIcon = { Icon(imageVector = Icons.Outlined.Refresh, contentDescription = "")}
                        )
                    }
                    DropdownMenu(
                        expanded = mSettingMenu,
                        onDismissRequest = { mSettingMenu = false })
                    {
                        DropdownMenuItem(
                            text = { Text(text = "Easy") },
                            onClick = { memoryViewModel.difficulty("Easy") }
                        )
                        DropdownMenuItem(
                            text = { Text(text = "Medium") },
                            onClick = { memoryViewModel.difficulty("Medium") }
                        )
                        DropdownMenuItem(
                            text = { Text(text = "Hard") },
                            onClick = { memoryViewModel.difficulty("Hard") }
                        )
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )


            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp)
                ) {
                    ElevatedCard(
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Text(
                            text = "Score: ${memoryUiState.score}",
                            fontSize = 25.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    ElevatedCard(
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Text(
                            text = "Moves: ${memoryUiState.moves}",
                            fontSize = 25.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    }
                }
            }
        }
    ) {
        LazyVerticalGrid(
            contentPadding = it,// Adjust overall padding for the grid
            columns = GridCells.Fixed(memoryUiState.cols),
            modifier = Modifier
                .fillMaxSize(),

            verticalArrangement = Arrangement.SpaceEvenly, // Space between rows
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) // Space between columns
        {
            items( memoryViewModel.shuffledPhotos.size) {index->
                var photo = memoryViewModel.shuffledPhotos[index]
                Box (modifier = Modifier

                        .clickable {
                            memoryViewModel.changeImage(photo, index)

                        }
                ){
                    Image(
                        painter =  if(photo.isFlipped) painterResource(id = photo.dummyImageResourceId) else painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "",
                        modifier = Modifier
                            .size(150.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .align(Alignment.Center)

                    )
                }
            }
        }
    }
}



@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GreetingPreview() {
    MyMemoryTheme {
        Main()
    }
}
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GreetingPreview2() {
    MyMemoryTheme{
        Greeting(name = "Mokshit")
    }
}