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
package app.umerfarooq.websocketcam.domain.model.camera

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val jsonConverter = Json {
    encodeDefaults = true
    ignoreUnknownKeys = true
}

@Serializable
data class CameraSettings(
    val cameraResolution: CameraResolution = CameraConfigDefaults.CAMERA_RESOLUTION,
    val selectedFpsRangeFrontCam: FpsRange = CameraConfigDefaults.FPS_RANGE_FRONT_CAM,
    val selectedFpsRangeBackCam: FpsRange = CameraConfigDefaults.FPS_RANGE_BACK_CAM,
    val quality: Int = CameraConfigDefaults.QUALITY,
    val cameraFacing: CameraFacing = CameraConfigDefaults.CAMERA_FACING,
    val rotateImage: Boolean = CameraConfigDefaults.ROTATE_IMAGE,
    val cameraContentScale: CameraContentScale = CameraConfigDefaults.CAMERA_CONTENT_SCALE
){
    fun toJson() = jsonConverter.encodeToString(this)
    companion object {
        fun fromJson(json: String) = jsonConverter.decodeFromString<CameraSettings>(json)
    }

}
