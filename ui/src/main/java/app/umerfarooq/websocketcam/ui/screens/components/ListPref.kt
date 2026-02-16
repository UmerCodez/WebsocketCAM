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
package app.umerfarooq.websocketcam.ui.screens.components


import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.umerfarooq.websocketcam.domain.model.camera.CameraResolution
import app.umerfarooq.websocketcam.ui.theme.WebsocketCAMTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <V> ListPref(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    mappings : Map<String, V>,
    selectedKey: String,
    expanded: Boolean,
    onDismissRequest: (() -> Unit),
    onExpandClick: (() -> Unit)? = null,
    onItemSelected: ((V) -> Unit)? = null,
) {

    ListItem(
        modifier = modifier,
        headlineContent = title,
        supportingContent = {
            Text(selectedKey)
        },
        trailingContent = {
            
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = onDismissRequest
            ) {
                mappings.keys.toList().forEach { key ->
                    val backgroundColor =
                        if (key == selectedKey) MaterialTheme.colorScheme.tertiaryContainer
                        else MaterialTheme.colorScheme.surface
                    val textColor =
                        if (key == selectedKey) MaterialTheme.colorScheme.onTertiaryContainer
                        else MaterialTheme.colorScheme.onSurface
                    DropdownMenuItem(
                        modifier = Modifier
                            .background(backgroundColor),
                        text = {
                            Text(
                                text = key,
                                color = textColor
                            )
                        },
                        onClick = {
                            mappings[key]?.also { onItemSelected?.invoke(it) }
                        }
                    )

                }
            }
            
            IconButton(
                onClick = { onExpandClick?.invoke() },
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = "Edit"
                )
            }
        }
    )



}

private val resolutions = listOf(
    CameraResolution(160, 120),   // Low bandwidth
    CameraResolution(224,224),
    CameraResolution(320, 240),   // Low-mid
    CameraResolution(416, 416),   // YOLO
    CameraResolution(640, 480),   // General vision
    CameraResolution(640, 640),   // YOLOv5/8
    CameraResolution(512, 512),  // General vision
    CameraResolution(800, 600),   // Legacy 4:3
    CameraResolution(1280, 720),  // HD
)

@Preview
@Composable
private fun ListPrefPreview() {
    WebsocketCAMTheme{
        ListPref(
            title = { Text("Camera Resolution") },
            mappings = resolutions.associate { resolution ->
                resolution.toString() to resolution
            },
            expanded = true,
            onDismissRequest = {},
            selectedKey = resolutions[0].toString(),
        )
    }

}
