package com.rickyslash.composenavdrawer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.rickyslash.composenavdrawer.ui.theme.ComposeNavDrawerTheme
import kotlinx.coroutines.launch

@Composable
fun NavDrawer() {
    val scaffoldState = rememberScaffoldState() // built-in Scaffold state to manage contained element (drawerState & snackbarHostState)
    val scope = rememberCoroutineScope() // call Coroutine inside Composable when onClick

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            MyTopBar(onMenuClick = {
                scope.launch {
                    scaffoldState.drawerState.open()
                }
            })
        },
        drawerContent = { Text(stringResource(R.string.hello_from_nav_drawer)) },
        drawerGesturesEnabled = scaffoldState.drawerState.isOpen
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) { Text(stringResource(R.string.hello_world)) }
    }
}

@Composable
fun MyTopBar(onMenuClick: () -> Unit) {
    TopAppBar(
        navigationIcon = { IconButton(onClick = { onMenuClick() }) {
            Icon(imageVector = Icons.Default.Menu, contentDescription = stringResource(R.string.menu))
            }
        },
        title = { Text(stringResource(R.string.app_name)) }
    )
}

@Composable
@Preview(showBackground = true)
fun NavDrawerPreview() {
    ComposeNavDrawerTheme {
        NavDrawer()
    }
}