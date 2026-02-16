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

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ListItem
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.umerfarooq.websocketcam.ui.theme.WebsocketCAMTheme

@Composable
fun SliderPref(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    subtitle: @Composable () -> Unit,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float> = 0f..100f,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: ((Float) -> Unit)? = null
) {
    ListItem(
        modifier = modifier,
        headlineContent = { title() },
        supportingContent = { subtitle() },
        trailingContent = {
            Slider(
                modifier = Modifier.fillMaxWidth(0.5f),
                value = value,
                valueRange = valueRange,
                onValueChange = onValueChange,
                onValueChangeFinished = {
                    onValueChangeFinished?.invoke(value)
                }
            )
        }
    )

}

@Preview
@Composable
private fun SliderPrefPreview() {
    WebsocketCAMTheme {
        SliderPref(
            title = { Text("Quality") },
            subtitle = { Text("80") },
            value = 80f,
            onValueChange = {}
        )
    }
}

