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

import app.umerfarooq.websocketcam.domain.model.camera.CameraContentScale
import app.umerfarooq.websocketcam.domain.model.camera.CameraFacing
import app.umerfarooq.websocketcam.domain.model.camera.CameraResolution
import app.umerfarooq.websocketcam.domain.model.camera.FpsRange


sealed interface CameraSettingsScreenEvent {
    data class OnCameraResolutionSelected(val cameraResolution: CameraResolution) : CameraSettingsScreenEvent
    data class OnCameraFacingSelected(val cameraFacing: CameraFacing) : CameraSettingsScreenEvent
    data class OnQualityChanged(val quality: Int) : CameraSettingsScreenEvent
    data class OnRotateImageChanged(val rotateImage: Boolean) : CameraSettingsScreenEvent
    data class OnFrontCamFpsRangeSelected(val fpsRange: FpsRange) : CameraSettingsScreenEvent
    data class OnBackCamFpsRangeSelected(val fpsRange: FpsRange) : CameraSettingsScreenEvent
    data class OnCameraContentScaleSelected(val cameraContentScale: CameraContentScale) : CameraSettingsScreenEvent
}

