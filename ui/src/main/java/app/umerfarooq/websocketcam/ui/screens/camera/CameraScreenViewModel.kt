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
package app.umerfarooq.websocketcam.ui.screens.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.hardware.camera2.CaptureRequest
import android.util.Log
import android.util.Range
import android.util.Size
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.umerfarooq.websocketcam.domain.model.camera.CameraConfigDefaults
import app.umerfarooq.websocketcam.domain.model.camera.CameraFacing
import app.umerfarooq.websocketcam.domain.repository.CameraSettingsRepository
import app.umerfarooq.websocketcam.domain.service.ServerState
import app.umerfarooq.websocketcam.domain.service.WebsocketServerService
import app.umerfarooq.websocketcam.domain.util.BoundedServiceInstanceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject


@OptIn(ExperimentalCoroutinesApi::class)
@ExperimentalCamera2Interop
@HiltViewModel
class CameraScreenViewModel @Inject constructor(
    private val cameraSettingsRepository: CameraSettingsRepository,
    private val boundedServiceInstanceProvider: BoundedServiceInstanceProvider
) : ViewModel(), ImageAnalysis.Analyzer {
    // Used to set up a link between the Camera and your UI.
    private val _surfaceRequest = MutableStateFlow<SurfaceRequest?>(null)
    val surfaceRequest: StateFlow<SurfaceRequest?> = _surfaceRequest

    private var _settingsLoaded = MutableStateFlow(false)
    val settingsLoaded: StateFlow<Boolean> = _settingsLoaded

    private var _websocketServerState = MutableStateFlow<ServerState>(ServerState.Stopped)
    val websocketServerState: StateFlow<ServerState> = _websocketServerState

    private var _cameraContentScale = MutableStateFlow(CameraConfigDefaults.CAMERA_CONTENT_SCALE)
    val cameraContentScale = _cameraContentScale.asStateFlow()


    private var websocketServerService: WebsocketServerService? = null

    private val tag = CameraScreenViewModel::class.simpleName

    private lateinit var targetResolution: Size
    private lateinit var cameraPreviewUseCase : Preview
    private lateinit var imageAnalysisUseCase : ImageAnalysis
    private lateinit var cameraSelector: CameraSelector
    private var quality: Int = 85
    private var rotateImage = false

    init {
        Log.d(tag, "init(${hashCode()})")

        viewModelScope.launch {
            boundedServiceInstanceProvider.websocketServerService
                .filterNotNull()
                .flatMapLatest { websocketServerService ->
                    this@CameraScreenViewModel.websocketServerService = websocketServerService
                    websocketServerService.serverState
                }
                .collect { serverState ->
                    _websocketServerState.value = serverState
                }

            }


        viewModelScope.launch {
            cameraSettingsRepository.cameraSettings.collect { cameraSettings ->
                _settingsLoaded.value = false

                rotateImage = cameraSettings.rotateImage
                _cameraContentScale.value = cameraSettings.cameraContentScale


                val isFpsRangeAvailableForSelectedCamera = !cameraSettingsRepository.getAvailableFPSRanges(cameraSettings.cameraFacing).isEmpty()


                // This fpsRange will get default value of (30,30) when selected camera don't have FPS ranges available.
                // This default value will never be used
                val fpsRange = when(cameraSettings.cameraFacing){
                    CameraFacing.FRONT -> cameraSettings.selectedFpsRangeFrontCam
                    CameraFacing.BACK -> cameraSettings.selectedFpsRangeBackCam
                }

                // TODO: add check that if available FPS range is empty then don't set specific range

                targetResolution = Size(cameraSettings.cameraResolution.width, cameraSettings.cameraResolution.height)
                quality = cameraSettings.quality


                cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(
                        when(cameraSettings.cameraFacing){
                            CameraFacing.FRONT -> CameraSelector.LENS_FACING_FRONT
                            CameraFacing.BACK -> CameraSelector.LENS_FACING_BACK
                        }
                    )
                    .build()


                @Suppress("DEPRECATION")
                cameraPreviewUseCase =  Preview.Builder()
                    .setTargetResolution(targetResolution)
                    .also {
                        if(isFpsRangeAvailableForSelectedCamera) {
                            val extender = Camera2Interop.Extender(it)
                            extender.setCaptureRequestOption(
                                CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE,
                                Range(fpsRange.lower, fpsRange.upper)
                            )
                        }
                    }
                    .build().apply {
                        setSurfaceProvider { newSurfaceRequest ->
                            _surfaceRequest.update { newSurfaceRequest }
                        }
                    }

                @Suppress("DEPRECATION")
                imageAnalysisUseCase = ImageAnalysis.Builder()
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setTargetResolution(targetResolution)
                    .also {
                        if(isFpsRangeAvailableForSelectedCamera) {
                            val extender = Camera2Interop.Extender(it)
                            extender.setCaptureRequestOption(
                                CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE,
                                Range(fpsRange.lower, fpsRange.upper)
                            )
                        }
                    }
                    .build().apply {
                        setAnalyzer(
                            { it.run() },
                            this@CameraScreenViewModel// No-op analyzer
                        )

                    }

                _settingsLoaded.value = true

            }
        }
    }



    suspend fun bindToCamera(appContext: Context, lifecycleOwner: LifecycleOwner) {
        val processCameraProvider = ProcessCameraProvider.awaitInstance(appContext)
        processCameraProvider.bindToLifecycle(
            lifecycleOwner, cameraSelector, cameraPreviewUseCase, imageAnalysisUseCase
        )

        // Cancellation signals we're done with the camera
        try { awaitCancellation() } finally { processCameraProvider.unbindAll() }
    }

    override fun analyze(imageProxy: ImageProxy) {

        viewModelScope.launch(Dispatchers.Default) {
            try {
                val bitmap = if (rotateImage)
                    imageProxy.toBitmap().rotateBitmap(
                        imageProxy.imageInfo.rotationDegrees.toFloat()
                    )
                else imageProxy.toBitmap()


                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                val jpegBytes = outputStream.toByteArray()

                withContext(Dispatchers.IO) {
                    websocketServerService?.broadcast(jpegBytes)
                }

            } catch (e: Exception) {
                Log.d(tag, "Exception: ${e.message}")
                e.printStackTrace()
            } finally {
                imageProxy.close()
            }
        }


    }

    override fun onCleared() {
        super.onCleared()
        Log.d(tag, "onCleared(${hashCode()})")
        imageAnalysisUseCase.clearAnalyzer()
    }

    private fun Bitmap.rotateBitmap(degrees: Float): Bitmap {
        if (degrees == 0f) return this

        val matrix = Matrix().apply {
            postRotate(degrees)
        }

        return Bitmap.createBitmap(
            this,
            0,
            0,
            this.width,
            this.height,
            matrix,
            true
        )
    }

}

