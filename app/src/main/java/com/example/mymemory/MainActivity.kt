package com.example.mymemory

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mymemory.ui.theme.MyMemoryTheme
import com.example.mymemory.ui.theme.Orbitron

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyMemoryTheme {
                // Initialize the NavController
                val navController = rememberNavController()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Set up NavHost to manage the navigation
                    NavHost(navController = navController, startDestination = "startScreen") {
                        composable("startScreen") {
                            StartScreen(navController = navController)
                        }
                        composable("gameScreen/{level}") { backStackEntry ->
                            val level = backStackEntry.arguments?.getString("level")?.toIntOrNull() ?: 1
                            val memoryViewModel = remember { MemoryViewModel() }
                            val memoryUiState by memoryViewModel.memoryState.collectAsState()
                            memoryViewModel.updateLevel(level)
                            GameScreen(
                                memoryViewModel = memoryViewModel,
                                memoryUiState = memoryUiState,
                                exitApp = { finishAffinity() },
                                backHandler = { navController.popBackStack("startScreen", inclusive = false) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GameScreen(
    memoryViewModel: MemoryViewModel,
    memoryUiState: MemoryState,
    exitApp: () -> Unit = {},
    backHandler: () -> Unit,
) {
    var showResetAlert by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopBar(memoryViewModel, backHandler) },
        bottomBar = { BottomBar(memoryState = memoryUiState) }
    ) { paddingValues ->
        LazyVerticalGrid(
            contentPadding = paddingValues, // Adjust overall padding for the grid
            columns = GridCells.Fixed(memoryViewModel.pairs() / 2),
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceEvenly, // Space between rows
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(memoryUiState.shuffledPhotos) { photo ->
                val dummyImageSize = animateDpAsState(
                    targetValue = if (photo.isFlipped.value) 100.dp else 0.dp,
                    animationSpec = tween(
                        durationMillis = 1000,
                        easing = LinearOutSlowInEasing
                    )
                )
                val imageSize = animateDpAsState(
                    targetValue = if (photo.isFlipped.value) 0.dp else 125.dp,
                    animationSpec = tween(
                        durationMillis = 1000,
                        easing = LinearOutSlowInEasing
                    )
                )
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clickable {
                            memoryViewModel.photoClicked(photo)
                        }
                ) {
                    if (photo.isFlipped.value) {
                        Image(
                            painter = painterResource(id = photo.dummyImageResourceId),
                            contentDescription = null,
                            modifier = Modifier
                                .size(dummyImageSize.value)
                                .align(Alignment.Center)
                                .clip(RoundedCornerShape(25.dp))
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
                            contentDescription = null,
                            modifier = Modifier
                                .size(imageSize.value)
                                .align(Alignment.Center)
                                .clip(RoundedCornerShape(25.dp))
                        )
                    }
                }
            }
        }

        if (memoryViewModel.gameFinish) {
            ResultScreen(
                onResetGame = { backHandler() },
                onRestartGame = { memoryViewModel.resetGame(level = memoryViewModel.memoryState.value.level) },
                onExitGame = { exitApp() },
                result = memoryViewModel.result
            )
        }

        BackHandler {
            showResetAlert = true
        }

        if (showResetAlert) {
            ResetAlert(
                message = "Are you sure? All your current progress will be lost!",
                onResetGame =
                {
                    backHandler()
                    showResetAlert = false
                },
                onResetLevel =
                {
                    memoryViewModel.resetGame(level = memoryViewModel.memoryState.value.level)
                    showResetAlert = false
                },
                onDismiss = { showResetAlert = false }
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    memoryViewModel: MemoryViewModel,
    backHandler: () -> Unit,
    modifier: Modifier = Modifier
) {
    var mDisplayMenu by remember { mutableStateOf(false) }
    var mSettingMenu by remember { mutableStateOf(false) }
    var mResetAlert by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "Memory Quiz",
                fontSize = 25.sp
            )
        },
        actions = {
            IconButton(onClick = {mDisplayMenu=!mDisplayMenu}){
                Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Menu")
            }
            DropdownMenu(
                expanded = mDisplayMenu,
                onDismissRequest = { mDisplayMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text(text = "Settings",color = Color.Black)},
                    onClick = {
                        mSettingMenu=!mSettingMenu
                        mDisplayMenu = false
                        Log.d("TAG", "TopBar: Clicked Settings button")
                    },
                    leadingIcon = {
                        Icon(imageVector = Icons.Outlined.Settings, contentDescription = "Settings")
                    }
                )
                DropdownMenuItem(
                    text = { Text(text = "Star this repo",color = Color.Black)},
                    onClick = {
                        uriHandler.openUri("https://github.com/Mokshit-123/MyMemory")
                        mDisplayMenu = false
                    },
                    leadingIcon = { Icon(painterResource(id = R.drawable.baseline_star_outline_24), contentDescription = "Settings")}
                )
                DropdownMenuItem(
                    text = { Text(text = "Reset",color = Color.Black)},
                    onClick = {
                        mDisplayMenu = false
                        mResetAlert = true
                    },
                    leadingIcon = { Icon(imageVector = Icons.Outlined.Refresh, contentDescription = "Reset")}
                )
            }
            DropdownMenu(
                expanded = mSettingMenu,
                onDismissRequest = { mSettingMenu = false })
            {
                DropdownMenuItem(
                    text = { Text(text = "Easy", color = Color.Black) },
                    onClick = {
                        mSettingMenu = false
                        memoryViewModel.resetGame()
                    },
                    leadingIcon = {
                        Icon(painterResource(id = R.drawable.baseline_sentiment_very_satisfied_24), contentDescription = null)
                    }
                )
                DropdownMenuItem(
                    text = { Text(text = "Medium",color = Color.Black) },
                    onClick = {
                        mSettingMenu=false
                        memoryViewModel.updateLevel(2)
                        Log.d("TAG", "TopBar: trying to update to 2")
                    },
                    leadingIcon = {
                        Icon(painterResource(id = R.drawable.baseline_sentiment_neutral_24), contentDescription = null )
                    }
                )
                DropdownMenuItem(
                    text = { Text(text = "Hard",color = Color.Black) },
                    onClick = {
                        mSettingMenu=false
                        memoryViewModel.updateLevel(level = 3)
                    },
                    leadingIcon = {
                        Icon(painterResource(id = R.drawable.baseline_mood_bad_24), contentDescription = null)
                    }
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
    if (mResetAlert){
        ResetAlert(
            message = "Are you sure? All your current progress will be lost!",
            onResetGame =
            {
                backHandler()
                mResetAlert = false
            },
            onResetLevel =
            {
                memoryViewModel.resetGame(level = memoryViewModel.memoryState.value.level)
                mResetAlert = false
            },
            onDismiss = { mResetAlert = false }
        )
    }
}

@Composable
fun BottomBar(
    memoryState: MemoryState,
    modifier: Modifier = Modifier
){
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
                    text = "Score: ${memoryState.score}",
                    color = Color.Black,
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
                    text = "Moves: ${memoryState.moves}",
                    color = Color.Black,
                    fontSize = 25.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun ResetAlert(
    message:String,
    onResetGame: () -> Unit,
    onResetLevel: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = { onDismiss() }) {
        Card (
            modifier = Modifier
                .fillMaxWidth()
                .height(175.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.secondaryContainer)
        ){
            Text(
                text = message,
                modifier = Modifier.padding(16.dp)
            )
            Row (
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ){
                TextButton(onClick = { onDismiss() }) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = {
                    onResetGame()
                }) {
                    Text("Reset game")
                }
                TextButton(onClick = { onResetLevel() }) {
                    Text("Reset level")
                }
            }
        }
    }
}

@Composable
fun ResultScreen(
    onResetGame: () -> Unit,
    onRestartGame: () -> Unit,
    onExitGame:()->Unit,
    result:String
) {
    Dialog(onDismissRequest = { onResetGame() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(375.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                val painter = when(result){
                    "Bad"->R.drawable.skull
                    "Good"->R.drawable.moai
                    else->R.drawable.yawning_face
                }
                Image(
                    painter = painterResource(id = painter),
                    contentDescription = result,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize(0.5f)
                )
                val message = when(result){
                    "Bad"->"Really bro? this many moves?"
                    "Good"->"Damn bro!! Only this much moves?"
                    else->"Meh...I guess it was okay..."
                }
                Text(
                    text = message,
                    modifier = Modifier.padding(16.dp),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    TextButton(
                        onClick = { onRestartGame() },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text("Reset level")
                    }
                    TextButton(
                        onClick = { onResetGame() },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text("Reset Game")
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(
                        onClick = { onExitGame() },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text("Exit")
                    }
                }
            }
        }
    }
}


@Composable
fun StartScreen(navController: NavHostController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Image(
            painter = painterResource(R.drawable.fantasy_style_galaxy_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(radiusX = 30.dp, radiusY = 10.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .align(Alignment.Center),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "My Memory",
                color = Color.White,
                modifier = Modifier.padding(16.dp),
                fontSize = 46.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Orbitron
            )
            Text(
                text = "Test Your Memory",
                color = Color.White,
                modifier = Modifier.padding(16.dp),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Orbitron
            )

            Spacer(modifier = Modifier.height(32.dp))

            DifficultyButton("Easy",navController)
            DifficultyButton("Medium",navController)
            DifficultyButton("Hard",navController)
        }
    }
}

@Composable
fun DifficultyButton(text: String, navController: NavHostController) {
    Button(
        onClick = {
            when (text) {
                "Easy" -> navController.navigate("gameScreen/1")
                "Medium" -> navController.navigate("gameScreen/2")
                "Hard" -> navController.navigate("gameScreen/3")
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),

    ) {
        Text(
            text = when (text) {
                "Easy" -> "\uD83D\uDFE2" // Green circle emoji
                "Medium" -> "\uD83D\uDFE1" // Yellow circle emoji
                "Hard" -> "\uD83D\uDD34" // Red circle emoji
                else -> "⚫" // Black circle emoji for default
            },
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(end = 8.dp), // Space between emoji and text
            fontSize = 20.sp // Adjust size for emoji
        )
        Text(
            text = text,
            modifier = Modifier
                .clickable {
                    when (text) {
                        "Easy" -> navController.navigate("gameScreen/1")
                        "Medium" -> navController.navigate("gameScreen/2")
                        else -> navController.navigate("gameScreen/3")
                    }
                }
                .align(Alignment.CenterVertically),
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = Orbitron,
            textAlign = TextAlign.Center
        )
    }
}

@Preview
@Composable
private fun FinalScreenPreview() {
    ResultScreen(
        onResetGame = { /*TODO*/ },
        onRestartGame = { /*TODO*/ },
        onExitGame = { /*TODO*/ },
        result = "Good"
    )
}