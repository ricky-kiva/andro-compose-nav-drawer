package com.rickyslash.composenavdrawer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
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
                    NavDrawer()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ComposeNavDrawerTheme {
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

// - derivedStateOf(): change single or multiple State into a new State, without causing Recomposition
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

// Type of State:
// - UI Element State: Hoisted State for UI element (example: ScaffoldState)
// - Screen/UI State: State to determine component that is displayed in screen (loading, error, success, etc)

// Type of Logic:
// - UI Behavior Logic / UI Logic: associated with UI display (button activation, snackbar, etc)
// --- should be kept inside Composable
// - Business Logic: associated with app's function (checkout, payment, etc)
// --- should be kept on data layer (repository)

// Types of State Management: possible locations as a Single Source of Truth (SSoT)
// - Composable: to manage simple UI Element State
// - State Holder: to manage complex UI Element state (Contains UI Element State & UI Logic)
// - Architecture Component ViewModel: to access business logic, as well as UI State, or Screen State

// notes on each SSoT:
// - Composable can depend on multiple State Holder, or not at all
// - State Holder can depend on ViewModel if need data related to Business Logic / Screen State
// - ViewModel depend on Data Layer

// State Holder as SSoT:
// - State Holder is a class contains UI Element State & UI Logic that is interconnected
// - State Holder are compoundable (could be combined by another State Holder)
// - benefits in using State Holder:
// --- points every State change to a single place
// --- chance of out-of-sync reduced
// --- could be used for same component on different place
// --- reduce complexity of Composable
// --- make State Hoisting easier
/* example:
class FormInputState(initialInput: String) {
    var input by mutableStateOf(initialInput)
}
@Composable
fun rememberFormInputState(input: String): FormInputState =
    remember(input) {
        FormInputState(input)
    }

@Composable
fun MyForm() {
    val input = rememberFormInputState("")
    FormInput(
        state = input
    )
}

@Composable
fun FormInput(
    state: FormInputState = rememberFormInputState(""),
) {
    OutlinedTextField(
        value = state.input,
        onValueChange = { state.input = it },
        label = { Text("Name") },
        modifier = Modifier.padding(8.dp)
    )
}*/

// ViewModel as SSoT:
// - ViewModel is an Architecture Component that has 2 function (get data from Data Layer & prepare it to be displayed on screen)
// - possible benefit using ViewModel:
// --- retain data on configuration change (example: rotated)
// --- integrated with another Jetpack Library (Hilt / Navigation Component)
// --- saved on cache when on Navigation backstack and cleaned when exit
// - note: Google recommend to use ViewModel to save state in 'page' level, not 'component' level
/* example:
data class ExampleUiState(
    val dataToDisplayOnScreen: List<Example> = emptyList(),
    val errorMessages: String = “”,
    val loading: Boolean = false
)

class ExampleViewModel(
    private val repository: MyRepository,
    private val savedState: SavedStateHandle
) : ViewModel() {

    var uiState by mutableStateOf(ExampleUiState())
        private set

    val data: Flow<List<Data>> = repository.data

    // Business logic
    fun somethingRelatedToBusinessLogic() { /* ... */ }
}

@Composable
fun ExampleScreen(viewModel: ExampleViewModel = viewModel()) {

    val uiState = viewModel.uiState
    val data = viewModel.data.collectAsState
    /* ... */

    ExampleReusableComponent(
        someData = uiState.dataToDisplayOnScreen,
        onDoSomething = { viewModel.somethingRelatedToBusinessLogic() }
    )
}

@Composable
fun ExampleReusableComponent(someData: Any, onDoSomething: () -> Unit) {
    /* ... */
    Button(onClick = onDoSomething) {
        Text("Do something")
    }
}*/

// note on ViewModel example:
// - UI State could be class, enum, interface, etc that could show page's status (loading, success, error, etc)
// - mutableStateOf used to read data change. Not use `remember` because it's outside Composable
// - if the data is stream, use this extension to change it to State:
// --- Flow.collectAsState(): convert flow to state
// --- LiveData.observeAsState: convert LiveData to state (dependency: androidx.compose.runtime:runtime-livedata)
// --- Observable.subscribeAsState: convert Observable object from RxJava2 / RxJava3 to State (dependency: androidx.compose.runtime:runtime-rxjava2 / -rxjava3)

// things to be considered in using ViewModel for Composable:
// - it has longer lifetime than Composable. avoid using State that holds Composition (ex: ScaffoldState) on ViewModel
// - only use ViewModel on level: Screen Composable. Don't send ViewModel object to another composable, only send needed argument (so Recomposition is effective)
// - separate Stateless & Stateful Composable that contains ViewMode. It made Preview easier
