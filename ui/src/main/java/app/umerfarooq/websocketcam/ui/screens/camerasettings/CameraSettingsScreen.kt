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
package app.umerfarooq.websocketcam.ui.screens.camerasettings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.umerfarooq.websocketcam.domain.model.camera.CameraContentScale
import app.umerfarooq.websocketcam.domain.model.camera.CameraFacing
import app.umerfarooq.websocketcam.domain.model.camera.CameraResolution
import app.umerfarooq.websocketcam.domain.model.camera.CameraSettings
import app.umerfarooq.websocketcam.domain.model.camera.FpsRange
import app.umerfarooq.websocketcam.domain.service.ServerState
import app.umerfarooq.websocketcam.ui.screens.components.ListPref
import app.umerfarooq.websocketcam.ui.screens.components.SliderPref
import app.umerfarooq.websocketcam.ui.screens.components.SwitchPref
import app.umerfarooq.websocketcam.ui.theme.WebsocketCAMTheme


@Composable
fun CameraSettingsScreen(
    viewModel: CameraSettingsScreenViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    CameraSettingsScreen(
        state = state,
        onEvent = viewModel::onEvent
    )

}

@Composable
fun CameraSettingsScreen(
    state: CameraSettingsScreenState,
    onEvent: (CameraSettingsScreenEvent) -> Unit
) {
    Surface {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            var showResolutions by remember { mutableStateOf(false) }
            val resolutions = remember {
                listOf(
                    CameraResolution(160, 120),   // Low bandwidth
                    CameraResolution(224, 224),
                    CameraResolution(320, 240),   // Low-mid
                    CameraResolution(416, 416),   // YOLO
                    CameraResolution(640, 480),   // General vision
                    CameraResolution(640, 640),   // YOLOv5/8
                    CameraResolution(512, 512),  // General vision
                    CameraResolution(800, 600),   // Legacy 4:3
                )
            }

            ListPref(
                title = { Text("Camera Resolution") },
                mappings = resolutions.associate { resolutions ->
                    resolutions.toString() to resolutions
                },
                selectedKey = state.cameraSettings.cameraResolution.toString(),
                expanded = showResolutions,
                onDismissRequest = { showResolutions = false },
                onExpandClick = {showResolutions = true},
                onItemSelected = {
                    onEvent(CameraSettingsScreenEvent.OnCameraResolutionSelected(it))
                    showResolutions = false
                },
            )

            var showContentScales by remember { mutableStateOf(false) }

            ListPref(
                title = { Text("Camera View Content scale") },
                mappings = CameraContentScale.entries.associate { contentScale ->
                    contentScale.name to contentScale
                },
                expanded = showContentScales,
                onDismissRequest = { showContentScales = false },
                onExpandClick = {showContentScales = true},
                selectedKey = state.cameraSettings.cameraContentScale.name,
                onItemSelected = {
                    onEvent(CameraSettingsScreenEvent.OnCameraContentScaleSelected(it))
                    showContentScales = false
                },
            )

            var showCameraFacings by remember { mutableStateOf(false) }

            ListPref(
                title = { Text("Camera Facing") },
                mappings = mapOf(
                    "BACK" to CameraFacing.BACK,
                    "FRONT" to CameraFacing.FRONT,
                ),
                expanded = showCameraFacings,
                onDismissRequest = { showCameraFacings = false },
                onExpandClick = {showCameraFacings = true},
                selectedKey = state.cameraSettings.cameraFacing.name,
                onItemSelected = {
                    onEvent(CameraSettingsScreenEvent.OnCameraFacingSelected(it))
                    showCameraFacings = false
                },
            )


            if(!state.availableFpsRangesForSelectedCamera.isEmpty()) {

                AnimatedVisibility(state.cameraSettings.cameraFacing == CameraFacing.FRONT) {
                    var showFPSRangeFrontCam by remember { mutableStateOf(false) }
                    ListPref(
                        title = { Text("FPS Range (FRONT)") },
                        mappings = state.availableFpsRangesForSelectedCamera.associate { fpsRange ->
                            fpsRange.toString() to fpsRange
                        },
                        expanded = showFPSRangeFrontCam,
                        onDismissRequest = { showFPSRangeFrontCam = false },
                        onExpandClick = {showFPSRangeFrontCam = true},
                        selectedKey = state.cameraSettings.selectedFpsRangeFrontCam.toString(),
                        onItemSelected = {
                            onEvent(CameraSettingsScreenEvent.OnFrontCamFpsRangeSelected(it))
                            showFPSRangeFrontCam = false
                        },
                    )
                }

                AnimatedVisibility(state.cameraSettings.cameraFacing == CameraFacing.BACK) {
                    var showFPSRangeBackCam by remember { mutableStateOf(false) }
                    ListPref(
                        title = { Text("FPS Range (BACK)") },
                        mappings = state.availableFpsRangesForSelectedCamera.associate { fpsRange ->
                            fpsRange.toString() to fpsRange
                        },
                        expanded = showFPSRangeBackCam,
                        onDismissRequest = { showFPSRangeBackCam = false },
                        onExpandClick = {showFPSRangeBackCam = true},
                        selectedKey = state.cameraSettings.selectedFpsRangeBackCam.toString(),
                        onItemSelected = {
                            onEvent(CameraSettingsScreenEvent.OnBackCamFpsRangeSelected(it))
                            showFPSRangeBackCam = false
                        },
                    )
                }
            }


            var quality by remember(state.cameraSettings.quality) { mutableFloatStateOf(state.cameraSettings.quality.toFloat()) }

            SliderPref(
                title = { Text("Quality") },
                subtitle = { Text(quality.toInt().toString()) },
                value = quality,
                valueRange = 1f..100f,
                onValueChange = {
                    quality = it
                },
                onValueChangeFinished = {
                    onEvent(CameraSettingsScreenEvent.OnQualityChanged(it.toInt()))
                }

            )

            SwitchPref(
                title = { Text("Auto-Rotate Image") },
                subtitle = {
                    Column {
                        Text("Rotate image to match device orientation before sending")
                        AnimatedVisibility(state.cameraSettings.rotateImage) {
                            Text(
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                                        shape = MaterialTheme.shapes.small
                                    )
                                    .padding(7.dp),
                                text = "This is expensive operation",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                },
                checked = state.cameraSettings.rotateImage,
                onCheckedChange = {
                    onEvent(CameraSettingsScreenEvent.OnRotateImageChanged(it))
                }
            )

        }

    }

}


@Preview
@Composable
fun CameraSettingsScreenPreview(modifier: Modifier = Modifier) {

    WebsocketCAMTheme {
        CameraSettingsScreen(
            state = CameraSettingsScreenState(
                cameraSettings = CameraSettings(
                    cameraResolution = CameraResolution(640, 480),
                    quality = 80,
                    cameraFacing = CameraFacing.BACK,
                    selectedFpsRangeFrontCam = FpsRange(30, 30),
                    rotateImage = true,
                    selectedFpsRangeBackCam = FpsRange(30, 30),
                    cameraContentScale = CameraContentScale.CROP
                ),
                serverState = ServerState.Stopped
            ),
            onEvent = {}
        )
    }

}