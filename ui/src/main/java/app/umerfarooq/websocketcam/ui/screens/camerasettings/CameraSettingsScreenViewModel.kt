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

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.umerfarooq.websocketcam.domain.repository.CameraSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CameraSettingsScreenViewModel @Inject constructor(
    private val cameraSettingsRepository: CameraSettingsRepository
) : ViewModel(){

    private val _state = MutableStateFlow(CameraSettingsScreenState())
    val state = _state
    private val tag = CameraSettingsScreenViewModel::class.simpleName

    init {
        Log.d(tag, "init(${hashCode()})")

        viewModelScope.launch {
            cameraSettingsRepository.cameraSettings.collect{ cameraSettings ->
                _state.update {
                    it.copy(
                        cameraSettings = cameraSettings,
                        availableFpsRangesForSelectedCamera = cameraSettingsRepository.getAvailableFPSRanges(cameraSettings.cameraFacing)
                    )
                }
            }
        }
    }

    fun onEvent(event: CameraSettingsScreenEvent){
        when(event){
            is CameraSettingsScreenEvent.OnCameraFacingSelected -> {
                viewModelScope.launch {
                    cameraSettingsRepository.update {
                        it.copy(cameraFacing = event.cameraFacing)
                    }
                }
            }
            is CameraSettingsScreenEvent.OnCameraResolutionSelected -> {
                viewModelScope.launch {
                    cameraSettingsRepository.update {
                        it.copy(cameraResolution = event.cameraResolution)
                    }
                }
            }
            is CameraSettingsScreenEvent.OnQualityChanged -> {
                viewModelScope.launch {
                    cameraSettingsRepository.update {
                        it.copy(quality = event.quality)
                    }
                }

            }
            is CameraSettingsScreenEvent.OnRotateImageChanged -> {
                viewModelScope.launch {
                    cameraSettingsRepository.update {
                        it.copy(rotateImage = event.rotateImage)
                    }
                }
            }
            is CameraSettingsScreenEvent.OnFrontCamFpsRangeSelected -> {
                viewModelScope.launch {
                    cameraSettingsRepository.update {
                        it.copy(selectedFpsRangeFrontCam = event.fpsRange)
                    }
                }
            }
            is CameraSettingsScreenEvent.OnBackCamFpsRangeSelected -> {
                viewModelScope.launch {
                    cameraSettingsRepository.update {
                        it.copy(selectedFpsRangeBackCam = event.fpsRange)
                    }
                }
            }
            is CameraSettingsScreenEvent.OnCameraContentScaleSelected -> {
                viewModelScope.launch {
                    cameraSettingsRepository.update {
                        it.copy(cameraContentScale = event.cameraContentScale)
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(tag, "onCleared(${hashCode()})")
    }


}