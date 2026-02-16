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
package app.umerfarooq.websocketcam.ui.screens.server.components

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import app.umerfarooq.websocketcam.domain.model.websocketserver.ServerInfo
import app.umerfarooq.websocketcam.domain.service.ServerState
import app.umerfarooq.websocketcam.ui.theme.WebsocketCAMTheme

@Composable
fun ServerControllerButton(
    modifier: Modifier = Modifier,
    serverState: ServerState,
    onStartClick: (() -> Unit)? = null,
    onStopClick: (() -> Unit)? = null
) {
    IconButton(
        modifier = modifier.aspectRatio(1f)
            .clip(CircleShape),
        onClick = {
            if(serverState !is ServerState.Running){
                onStartClick?.invoke()
            } else {
                onStopClick?.invoke()
            }
        },
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = when(serverState){
                is ServerState.Running -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.primary
            },
            contentColor = when(serverState){
                is ServerState.Running -> MaterialTheme.colorScheme.onErrorContainer
                else -> MaterialTheme.colorScheme.onPrimary
            }
        )
    ) {

        when(serverState) {
            is ServerState.Running -> {
                Icon(
                    modifier = Modifier.fillMaxSize(0.7f),
                    imageVector = Icons.Default.Stop,
                    contentDescription = null
                )
            }
            else -> {
                Icon(
                    modifier = Modifier.fillMaxSize(0.7f),
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null
                )
            }
        }

    }

}

@Preview
@Composable
private fun ServerControllerButtonStoppedPreview() {
    WebsocketCAMTheme {
        ServerControllerButton(
            serverState = ServerState.Stopped,
        )
    }
}

@Preview
@Composable
private fun ServerControllerButtonPlayPreview() {
    WebsocketCAMTheme {
        ServerControllerButton(
            serverState = ServerState.Running(ServerInfo("192.168.1.1", 8080)),
        )
    }
}