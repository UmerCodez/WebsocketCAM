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
package app.umerfarooq.websocketcam.ui.screens.server

import android.Manifest
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.umerfarooq.websocketcam.domain.model.websocketserver.ServerInfo
import app.umerfarooq.websocketcam.domain.service.ServerState
import app.umerfarooq.websocketcam.ui.screens.components.EditTextPref
import app.umerfarooq.websocketcam.ui.screens.components.SwitchPref
import app.umerfarooq.websocketcam.ui.screens.server.components.ServerControllerButton
import app.umerfarooq.websocketcam.ui.theme.WebsocketCAMTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun WebsocketServerScreen(
    viewModel: WebsocketServerScreenViewModel = hiltViewModel(),
    onError: ((Exception) -> Unit)? = null
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.serverState) {
       if(state.serverState is ServerState.Error){
           (state.serverState as ServerState.Error).exception?.also { exception ->
               onError?.invoke(exception)
           }
       }
    }

    val postNotificationPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        null
    }

    WebsocketServerScreen(
        state = state,
        onEvent = { event ->

            if(event is ServerScreenEvent.OnStartClicked){
                postNotificationPermissionState?.launchPermissionRequest()
            }

            viewModel.onEvent(event)
        }
    )
}


@Composable
fun WebsocketServerScreen(
    state: ServerScreenState,
    onEvent: (ServerScreenEvent) -> Unit
) {

    // 1. Create a single ScrollState for the entire content
    val scrollState = rememberScrollState()

    Surface {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            // Settings
            var portNoEditMode by remember { mutableStateOf(false) }
            EditTextPref(
                title = { Text("Port") },
                value = state.serverSettings.port.toString(),
                editMode = portNoEditMode,
                onEditClick = { portNoEditMode = !portNoEditMode },
                isError = { value ->
                    val port = value.toIntOrNull()
                    port == null || port !in 1..65535 || port in 1..1024
                },
                errorText = "Port must be a number between 1025 and 65535",
                onUpdateClick = {
                    onEvent(ServerScreenEvent.OnPortChanged(it.toInt()))
                    portNoEditMode = false
                }
            )

            SwitchPref(
                title = { Text("Listen on 0.0.0.0") },
                subtitle = { Text("Listen on all available network interfaces") },
                checked = state.serverSettings.listenOnAllInterfaces,
                onCheckedChange = {
                    onEvent(ServerScreenEvent.OnListenOnAllInterfacesChanged(it))
                }
            )


            // Title and Address Card
            AnimatedVisibility(state.serverState is ServerState.Running) {
                Card(
                    modifier = Modifier
                        .padding(vertical = 10.dp) // Added vertical padding for spacing
                        .fillMaxWidth()
                ) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp),
                        text = state.address,
                        textAlign = TextAlign.Center
                    )
                }
            }
            // Add some space at the bottom of the scrollable content
            Spacer(modifier = Modifier.weight(1f))

            // Button Box: This will now be fixed at the bottom of the screen
            // because the content Column above has Modifier.weight(1f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp), // Padding around the button
                contentAlignment = Alignment.Center
            ) {
                ServerControllerButton(
                    modifier = Modifier.size(120.dp),
                    serverState = state.serverState,
                    onStartClick = {
                        onEvent(ServerScreenEvent.OnStartClicked)
                    },
                    onStopClick = {
                        onEvent(ServerScreenEvent.OnStopClicked)
                    }
                )
            }

            Spacer(modifier = Modifier.weight(0.2f))
        }
    }
}


@Preview
@Composable
fun ServerScreenPreview() {

    WebsocketCAMTheme {
        WebsocketServerScreen(
            state = ServerScreenState(
                address = "ws://192.168.19.60:8080",
                serverState = ServerState.Running(ServerInfo("192.168.18.0", 8080))
            ),
            onEvent = {}
        )
    }

}