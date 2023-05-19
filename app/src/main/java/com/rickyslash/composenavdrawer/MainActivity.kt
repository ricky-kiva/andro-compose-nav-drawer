package com.rickyslash.composenavdrawer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.rickyslash.composenavdrawer.ui.theme.ComposeNavDrawerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeNavDrawerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Greeting("Android")
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ComposeNavDrawerTheme {
        Greeting("Android")
    }
}

// Composable Lifecycle:
// - Enter Composition -> Recompose (0 times or more) -> exiting Composition

// Side Effect API is used for:
// - ensure Effect run on the right lifecycle
// - ensure Effect cleaned when exiting Composition
// - ensure Suspend Function cancelled when exiting Composition
// - Effect that depends on various input automatically cleaned and being re-run when the value changed

// Examples of Side Effect API:

// - launchedEffect(): to use certain action that only being called once, when Initial Composition / Parameter Key is changed
// --- it's Coroutine Scope, so suspend function could be called inside it
// --- often used for Splash Screen, display SnackBar, or save Saved Query
/* usage:
fun MyCountdown() {
    var timer by remember { mutableStateOf(60) }
    Text("Countdown : $timer")
    LaunchedEffect(true) {
        while (timer > 0) {
            delay(1000)
            timer--
        }
    }
}*/

// - rememberUpdatedState(): to mark value so it's not restarted even the Key is changed
// --- good to save memory for Effect that eats a lot of time / resources
/* this code will call callback `onTimeout()` when the timer is already done
@Composable
fun MyCountdownTimeout(onTimeout: () -> Unit) {
    var timer by remember { mutableStateOf(10) }
    val currentOnTimeout by rememberUpdatedState(onTimeout)
    Text("Countdown : $timer")
    LaunchedEffect(true) {
        while (timer > 0) {
            delay(1000)
            timer--
        }
        currentOnTimeout()
    }
}

@Composable
fun MyApp() {
    var showTimeOutScreen by remember { mutableStateOf(false) }
    if(showTimeOutScreen) {
          TimeOutScreen()
    } else {
           MyCountdownTimeout(onTimeout = {
                Log.d("MyApp", "onTimeout called")
                showTimeOutScreen = true
           })
    }
}*/

// - rememberCoroutineScope(): make Coroutine Scope to run Suspend Function outside Composable Function (whose aware of compose lifecycle)
// --- unlike LaunchedEffect, it could be configured manually, means it also could be run using onClick() event
/* example:
@Composable
fun MyRememberedCoroutineScope() {
    var timer by remember { mutableStateOf(60) }
    val scope = rememberCoroutineScope()
    var job: Job? by remember { mutableStateOf(null) }
    Column {
        Text(text = "Countdown : $timer")
        Button(onClick = {
            job?.cancel()
            timer = 60
            job = scope.launch {
                while (timer > 0) {
                    delay(1000)
                    timer--
                }
            }
        }){
            Text("Start")
        }
    }
}*/

// - SideEffect(): execute Side Effects within a composable function when Composable is recomposed
// --- Side effects are actions that have an impact outside of the composable function, such as interacting with external systems
// --- not a Coroutine Scope, means can't call suspend inside it
// --- used to re-new data from Firebase, determine TextField's focus, configure system UI such status bar, etc
/* example:
*/

// - DisposableEffect(): often used to clear something when exiting Composition
// --- when 'key' (Effect's parameter) is changed, earlier process will be disbanded & replaced to a new process
// --- DisposableEffect's end could be detected using `onDispose()`
/* example:
@Composable
fun MyCountdownDisposableEffect() {
    var timer by remember { mutableStateOf(60) }
    val scope = rememberCoroutineScope()
    var job: Job? by remember { mutableStateOf(null) }
    val lifecycleOwner = LocalLifecycleOwner.current
    Column {
        Text(text = "Countdown : $timer")
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_START) {
                    job?.cancel()
                    timer = 60
                    job = scope.launch {
                        while (timer > 0) {
                            delay(1000)
                            timer--
                        }
                    }
                } else if (event == Lifecycle.Event.ON_STOP) {
                    job?.cancel()
                    timer = 60
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
    }
}*/

// productState(): to make non-Compose State to a new Compose State
// --- often used to convert data from repository to UI state
// --- to detect about function, use `awaitDispose()`
/* example on converting data from ViewModel:
data class ImageUiState(
    val imageData: String? = null,
    val isLoading: Boolean = false,
    val isError: Boolean = false
)

@Composable
fun ImageScreen(
    modifier: Modifier = Modifier,
    viewModel: MyViewModel = viewModel()
) {

    val uiState by produceState(initialValue = ImageUiState(isLoading = true)) {
        val image = viewModel.image
        value = if (image != null) {
            ImageUiState(imageData = image)
        } else {
            ImageUiState(isError = true)
        }
    }
}*/

// - derivedStateOf(): change multiple State into a new State
// --- the value change won't cause Recomposition
/* example by making 'Jump to Bottom' button:
val jumpToBottomButtonEnabled by remember {
    derivedStateOf {
         scrollState.firstVisibleItemIndex != 0 ||
         scrollState.firstVisibleItemScrollOffset > jumpThreshold
    }
}*/

// - snapshotFlow(): convert State from Compose to Flow
// --- we could call this in another Side API, like LaunchedEffect()
/* example by making "Jump to Top" button with condition:
@Composable
fun MySnapshotFlow() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val listState = rememberLazyListState()

        LazyColumn(state = listState) {
            items(1000) { index ->
                Text(text = "Item: $index")
            }
        }

        var showButtonSnapshot by remember { mutableStateOf(false) }

        Column(modifier = Modifier.align(Alignment.BottomCenter)) {
            AnimatedVisibility(showButtonSnapshot) {
                Button({}) {
                    Text("Jump to Top")
                }
            }
        }

        LaunchedEffect(listState) {
            snapshotFlow { listState.firstVisibleItemIndex }
                .map { index -> index > 2 }
                .distinctUntilChanged()
                .collect {
                    showButtonSnapshot = it
                }
        }
    }
}*/

// Diagram about Side Effect API connection:
// - https://dicoding-web-img.sgp1.cdn.digitaloceanspaces.com/original/academy/dos:b45a6accea3c26a56baeb10cd177fd1c20221020174233.png