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

import app.umerfarooq.websocketcam.domain.model.camera.CameraSettings
import app.umerfarooq.websocketcam.domain.model.camera.FpsRange
import app.umerfarooq.websocketcam.domain.service.ServerState


data class CameraSettingsScreenState(
    val cameraSettings: CameraSettings = CameraSettings(),
    val serverState: ServerState = ServerState.Stopped,
    val availableFpsRangesForSelectedCamera: List<FpsRange> = emptyList()
)
