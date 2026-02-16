/*
 *     This file is a part of WebsocketCAM (https://www.github.com/UmerCodez/WebsocketCAM)
 *     Copyright (C) 2026 Umer Farooq (umerfarooq2383@gmail.com)
 *
 *     WebsocketCAM is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     WebsocketCAM is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with WebsocketCAM. If not, see <https://www.gnu.org/licenses/>.
 *
 */
package app.umerfarooq.websocketcam.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import app.umerfarooq.websocketcam.ui.screens.about.AboutScreen
import app.umerfarooq.websocketcam.ui.screens.camera.CameraScreen
import app.umerfarooq.websocketcam.ui.screens.camerasettings.CameraSettingsScreen
import app.umerfarooq.websocketcam.ui.screens.server.WebsocketServerScreen
import kotlinx.coroutines.launch
import java.net.BindException
import java.net.UnknownHostException

sealed interface NavKey {
    data object CameraScreen : NavKey
    data object CameraSettingsScreen : NavKey
    data object WebsocketServerScreen : NavKey
    data object AboutScreen: NavKey
}

sealed class NavItem(val navKey: NavKey, val imageVector: ImageVector, val label: String) {
    data object CameraSettings : NavItem(
        navKey = NavKey.CameraSettingsScreen,
        imageVector = Icons.Default.Camera,
        label = "Camera Setting"
    )

    data object Server : NavItem(
        navKey = NavKey.WebsocketServerScreen,
        imageVector = Icons.Default.Public,
        label = "Websocket Server"
    )
}

private val navItems = listOf(NavItem.CameraSettings, NavItem.Server)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Navigation() {

    // Create a back stack, specifying the key the app should start with
    val backStack = rememberSaveable { mutableStateListOf<Any>(NavKey.CameraSettingsScreen) }
    var selectedNavItemIndex by remember { mutableIntStateOf(navItems.indexOf(NavItem.CameraSettings)) }
    var showBottomBar by remember { mutableStateOf(true) }
    var showTopBar by remember { mutableStateOf(true) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Remove hide BottomBar when top element of backStack is CameraScreen
    LaunchedEffect(backStack.lastOrNull()) {
        showBottomBar = backStack.lastOrNull() != NavKey.CameraScreen
        showTopBar = backStack.lastOrNull() != NavKey.CameraScreen
    }


    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
        topBar = {
            AnimatedVisibility(showTopBar) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = when (backStack.lastOrNull()) {
                                is NavKey.CameraSettingsScreen -> "Camera Settings"
                                is NavKey.WebsocketServerScreen -> "Websocket Server"
                                is NavKey.AboutScreen -> "About"
                                else -> ""
                            }
                        )
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                backStack.add(NavKey.AboutScreen)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null
                            )
                        }
                    }
                )
            }
        },
        bottomBar = {
            AnimatedVisibility(showBottomBar) {
                NavigationBar {
                    navItems.forEach { navItem ->
                        NavigationBarItem(
                            selected = navItems.indexOf(navItem) == selectedNavItemIndex,
                            icon = {
                                Icon(
                                    imageVector = navItem.imageVector,
                                    contentDescription = null
                                )
                            },
                            label = { Text(text = navItem.label) },
                            onClick = {
                                selectedNavItemIndex = navItems.indexOf(navItem)

                                backStack.removeLastOrNull()
                                backStack.add(navItem.navKey)

                            }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            AnimatedVisibility(showBottomBar) {
                FloatingActionButton(
                    onClick = {
                        backStack.add(NavKey.CameraScreen)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = null
                    )
                }
            }
        }
    ) { innerPadding ->
        NavDisplay(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryProvider = entryProvider {

                entry<NavKey.CameraScreen> {
                    CameraScreen(
                        onServerNotRunning = {
                            scope.launch {
                                snackbarHostState.showSnackbar("Websocket server is not running")
                            }
                        }
                    )
                }
                entry<NavKey.CameraSettingsScreen> {
                    CameraSettingsScreen()
                }
                entry<NavKey.WebsocketServerScreen> {
                    WebsocketServerScreen(
                        onError = { exception ->
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = when (exception) {
                                        is BindException -> "Port is already in use"
                                        is UnknownHostException -> "Unable to obtain IP address"
                                        else -> exception.message ?: "Unknown error"
                                    }
                                )
                            }
                        }
                    )
                }

                entry<NavKey.AboutScreen> {
                    AboutScreen(
                        onBackClick = {
                            backStack.removeLastOrNull()
                        }
                    )
                }

            }
        )
    }

}