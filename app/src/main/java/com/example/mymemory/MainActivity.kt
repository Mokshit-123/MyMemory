package com.example.mymemory

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mymemory.ui.theme.MyMemoryTheme
import com.example.mymemory.ui.theme.Orbitron
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


val Context.dataStore by preferencesDataStore(name = "settings")
private val HAPTIC_FEEDBACK_KEY = booleanPreferencesKey("HAPTIC_FEEDBACK")

class SettingsRepository(private val context: Context) {

    val hapticFeedbackFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[HAPTIC_FEEDBACK_KEY] ?: true
        }

    suspend fun setHapticFeedback(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[HAPTIC_FEEDBACK_KEY] = enabled
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val settingsRepository = SettingsRepository(this)

        setContent {
            MyMemoryTheme {
                val navController = rememberNavController()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(navController = navController, startDestination = "startScreen") {
                        composable("startScreen") {
                            StartScreen(navController = navController,settingsRepository=settingsRepository)
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
                                backHandler = { navController.popBackStack("startScreen", inclusive = false) },
                                settingsRepository = settingsRepository
                            )
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartScreen(navController: NavHostController, settingsRepository: SettingsRepository) {
    var showSettings by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    var showInfo by remember { mutableStateOf(false) }
    if(showSettings){
        ModalBottomSheet(
            onDismissRequest = { showSettings = false},
            sheetState=sheetState,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ) {
            SettingsSheet(settingsRepository = settingsRepository)
        }
    }
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
            Spacer(modifier = Modifier.weight(1f))
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

            DifficultyButton("Easy", navController)
            DifficultyButton("Medium", navController)
            DifficultyButton("Hard", navController)

            Spacer(modifier = Modifier.weight(1f))

            Row {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = "Info",
                    modifier = Modifier.clickable {
                        showInfo = true
                    },
                    tint = Color.White
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings",
                    modifier = Modifier.clickable {
                        showSettings = true
                    },
                    tint = Color.White
                )
            }
            if(showInfo){
                InfoBox(onDismiss = { showInfo=false })
            }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    memoryViewModel: MemoryViewModel,
    backHandler: () -> Unit,
    modifier: Modifier = Modifier
) {
    var mDisplayMenu by remember { mutableStateOf(false) }
    var mLevelsMenu by remember { mutableStateOf(false) }
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
                    text = { Text(text = "Level",color = Color.Black)},
                    onClick = {
                        mLevelsMenu=!mLevelsMenu
                        mDisplayMenu = false
                        Log.d("TAG", "TopBar: Clicked Settings button")
                    },
                    leadingIcon = {
                        Icon(painter = painterResource(R.drawable.bar_chart), contentDescription = "Levels")
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
                expanded = mLevelsMenu,
                onDismissRequest = { mLevelsMenu = false })
            {
                DropdownMenuItem(
                    text = { Text(text = "Easy", color = Color.Black) },
                    onClick = {
                        mLevelsMenu = false
                        memoryViewModel.resetGame()
                    },
                    leadingIcon = {
                        Icon(painterResource(id = R.drawable.baseline_sentiment_very_satisfied_24), contentDescription = null)
                    }
                )
                DropdownMenuItem(
                    text = { Text(text = "Medium",color = Color.Black) },
                    onClick = {
                        mLevelsMenu=false
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
                        mLevelsMenu=false
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
fun GameScreen(
    memoryViewModel: MemoryViewModel,
    memoryUiState: MemoryState,
    exitApp: () -> Unit = {},
    backHandler: () -> Unit,
    settingsRepository: SettingsRepository,
) {
    var showResetAlert by remember { mutableStateOf(false) }
    var vibrateFlag by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val hapticFeedbackEnabled by settingsRepository.hapticFeedbackFlow.collectAsState(initial = true)

    Scaffold(
        topBar = { TopBar(memoryViewModel, backHandler) },
        bottomBar = { BottomBar(memoryState = memoryUiState) }
    ) { paddingValues ->
        LazyVerticalGrid(
            contentPadding = paddingValues,
            columns = GridCells.Fixed(memoryViewModel.pairs() / 2),
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceEvenly,
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
                            val flipped = memoryViewModel.photoClicked(photo)
                            if (!flipped && hapticFeedbackEnabled) {
                                vibrateFlag = true
                            }
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
                    if (vibrateFlag) {
                        LaunchedEffect(Unit) {
                            Vibrate(context)
                            vibrateFlag = false
                        }
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




fun Vibrate(context: Context) {
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        vibrator.vibrate(100) // Vibrate for 100 milliseconds
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
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.onPrimaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Text(
                    text = "Score: ${memoryState.score}",
                    color = Color.White,
                    fontSize = 25.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            ElevatedCard(
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.onPrimaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Text(
                    text = "Moves: ${memoryState.moves}",
                    color = Color.White,
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
fun SettingsSheet(settingsRepository: SettingsRepository) {
    val hapticFeedbackEnabled by settingsRepository.hapticFeedbackFlow.collectAsState(initial = true)
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .padding(16.dp)
    ) {
        Text(text = "Settings", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Haptic Feedback", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
            Spacer(modifier = Modifier.weight(1f))
            Switch(
                checked = hapticFeedbackEnabled,
                onCheckedChange = { isChecked ->
                    coroutineScope.launch {
                        settingsRepository.setHapticFeedback(isChecked)
                    }
                }
            )
        }
    }
}

@Composable
fun InfoBox(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "My Memory",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 8.dp),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "My Memory is a fun and engaging way to challenge your memory skills! " +
                            "Start by selecting a difficulty level—Easy, Medium, or Hard. Based on your selection, " +
                            "you'll be presented with a set of images for a short period of time. Your task is to memorize " +
                            "the images quickly. Once the images flip over, tap on the ones that are the same to match them. " +
                            "Test your memory and have fun!",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("OK")
                }
            }
        }
    }
}

@Preview
@Composable
private fun PrevInfoBox() {
    InfoBox(onDismiss = { /*TODO*/ })
}

@Preview
@Composable
private fun PrevBottomBar() {
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
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.onPrimaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Text(
                    text = "Score: 00",
                    color = Color.White,
                    fontSize = 25.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            ElevatedCard(
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.onPrimaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Text(
                    text = "Moves: 00",
                    color = Color.White,
                    fontSize = 25.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
        }
    }
}

