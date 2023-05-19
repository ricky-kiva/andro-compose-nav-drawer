package com.rickyslash.composenavdrawer

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rickyslash.composenavdrawer.ui.theme.ComposeNavDrawerTheme
import kotlinx.coroutines.launch

data class MenuItem(
    val title: String,
    val icon: ImageVector
)

@Composable
fun NavDrawer() {
    val scaffoldState = rememberScaffoldState() // built-in Scaffold state to manage contained element (drawerState & snackbarHostState)
    val scope = rememberCoroutineScope() // call Coroutine inside Composable when onClick
    val context = LocalContext.current

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            MyTopBar(onMenuClick = {
                scope.launch {
                    scaffoldState.drawerState.open()
                }
            })
        },
        drawerContent = {
            MyDrawerContent (onItemSelected = { title ->
                    scope.launch {
                        scaffoldState.drawerState.close()
                        val snackbarResult = scaffoldState.snackbarHostState.showSnackbar(
                            message = context.resources.getString(R.string.coming_soon, title),
                            actionLabel = context.resources.getString(R.string.subscribe_question)
                        )
                        if (snackbarResult == SnackbarResult.ActionPerformed) {
                            Toast.makeText(context, context.resources.getString(R.string.subscribed_info), Toast.LENGTH_SHORT).show()
                        }
                    }
                })
        },
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
fun MyDrawerContent(
    modifier: Modifier = Modifier,
    onItemSelected: (title: String) -> Unit
) {
    val items = listOf(
        MenuItem(stringResource(R.string.home), Icons.Default.Home),
        MenuItem(stringResource(R.string.favourite), Icons.Default.Favorite),
        MenuItem(stringResource(R.string.profile), Icons.Default.AccountCircle)
    )
    Column(modifier.fillMaxSize()) {
        Box(modifier = Modifier
            .height(190.dp)
            .fillMaxWidth()
            .background(MaterialTheme.colors.primary)
        )
        for (item in items) {
            Row(modifier = Modifier
                .clickable { onItemSelected(item.title) }
                .padding(vertical = 12.dp, horizontal = 16.dp)
                .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = item.icon, contentDescription = item.title)
                Spacer(modifier = Modifier.width(32.dp))
                Text(text = item.title, style = MaterialTheme.typography.subtitle2)
            }
        }
        Divider()
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