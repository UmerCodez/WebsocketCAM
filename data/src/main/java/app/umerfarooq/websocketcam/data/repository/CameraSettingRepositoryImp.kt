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
package app.umerfarooq.websocketcam.data.repository



import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.Range
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import app.umerfarooq.websocketcam.data.di.IoDispatcher
import app.umerfarooq.websocketcam.domain.model.camera.CameraFacing
import app.umerfarooq.websocketcam.domain.model.camera.CameraSettings
import app.umerfarooq.websocketcam.domain.model.camera.FpsRange
import app.umerfarooq.websocketcam.domain.repository.CameraSettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.cameraPreferencesDataStore: DataStore<Preferences> by preferencesDataStore("camera_settings")

class CameraSettingsRepositoryImp @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : CameraSettingsRepository {


    private object Key {
        val CAMERA_SETTINGS_JSON = stringPreferencesKey("camera_settings_json")
    }

    override val cameraSettings: Flow<CameraSettings>
        get() = context.cameraPreferencesDataStore.data.map { pref ->
            pref[Key.CAMERA_SETTINGS_JSON]?.let { cameraSettingJson ->
                runCatching {
                    CameraSettings.fromJson(cameraSettingJson)
                }.getOrDefault(CameraSettings())
            }?: CameraSettings()

        }.flowOn(ioDispatcher)

    override suspend fun update(cameraSettings: (CameraSettings) -> CameraSettings) {
        val oldSettings = this@CameraSettingsRepositoryImp.cameraSettings.first()
        val newSettings = cameraSettings.invoke(oldSettings)
        saveCameraSettings(newSettings)
    }

    private suspend fun saveCameraSettings(settings: CameraSettings) {
        context.cameraPreferencesDataStore.edit { pref ->
            pref[Key.CAMERA_SETTINGS_JSON] = settings.toJson()
        }
    }

    override suspend fun getAvailableFPSRanges(cameraFacing: CameraFacing) : List<FpsRange> {
        return getAvailableFpsRanges(
            context = context,
            lensFacing = when(cameraFacing){
                CameraFacing.FRONT -> CameraCharacteristics.LENS_FACING_FRONT
                CameraFacing.BACK -> CameraCharacteristics.LENS_FACING_BACK
            }
        ).map {
            FpsRange(it.lower, it.upper)
        }
    }

    private fun getAvailableFpsRanges(
        context: Context,
        lensFacing: Int
    ): List<Range<Int>> {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

        for (cameraId in cameraManager.cameraIdList) {
            val characteristics = cameraManager.getCameraCharacteristics(cameraId)
            val facing = characteristics.get(CameraCharacteristics.LENS_FACING)

            if (facing == lensFacing) {
                val fpsRanges =
                    characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES)
                return fpsRanges?.toList() ?: emptyList()
            }
        }

        // If no matching camera found
        return emptyList()
    }
}
