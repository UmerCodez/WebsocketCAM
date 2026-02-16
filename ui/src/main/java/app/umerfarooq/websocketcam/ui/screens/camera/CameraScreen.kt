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

import android.Manifest
import android.view.WindowManager
import androidx.activity.compose.LocalActivity
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.compose.CameraXViewfinder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.umerfarooq.websocketcam.domain.model.camera.CameraContentScale
import app.umerfarooq.websocketcam.domain.service.ServerState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState

@androidx.annotation.OptIn(ExperimentalCamera2Interop::class)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    modifier: Modifier = Modifier,
    viewModel: CameraScreenViewModel = hiltViewModel(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onServerNotRunning: () -> Unit = {}
) {

    val surfaceRequest by viewModel.surfaceRequest.collectAsStateWithLifecycle()
    val settingsLoaded by viewModel.settingsLoaded.collectAsStateWithLifecycle()
    val serverState by viewModel.websocketServerState.collectAsStateWithLifecycle()
    val cameraContentScale by viewModel.cameraContentScale.collectAsStateWithLifecycle()


    val context = LocalContext.current
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)


    LaunchedEffect(lifecycleOwner , settingsLoaded) {
        if(settingsLoaded) {
            viewModel.bindToCamera(context.applicationContext, lifecycleOwner)
        }
    }

    LaunchedEffect(Unit) {
        cameraPermissionState.launchPermissionRequest()
    }

    LaunchedEffect(serverState) {
        if(serverState !is ServerState.Running){
            onServerNotRunning()
        }
    }
    val window = LocalActivity.current?.window
    DisposableEffect(Unit) {
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }


    surfaceRequest?.let { request ->
        CameraXViewfinder(
            surfaceRequest = request,
            modifier = modifier,
            contentScale = when(cameraContentScale){
                CameraContentScale.CROP -> ContentScale.Crop
                CameraContentScale.FIT -> ContentScale.Fit
            }
        )
    }
}