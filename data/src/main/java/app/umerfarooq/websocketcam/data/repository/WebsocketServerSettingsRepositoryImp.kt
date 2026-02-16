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
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import app.umerfarooq.websocketcam.data.di.IoDispatcher
import app.umerfarooq.websocketcam.domain.model.websocketserver.WebsocketServerSettings
import app.umerfarooq.websocketcam.domain.repository.WebsocketServerSettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject


private val Context.websocketPreferencesDataStore: DataStore<Preferences> by preferencesDataStore("websocket_server_settings")

class WebsocketServerSettingsRepositoryImp @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : WebsocketServerSettingsRepository {

    private object Key {
        val SERVER_SETTINGS_JSON = stringPreferencesKey("server_settings_json")
    }

    override val websocketServerSettings: Flow<WebsocketServerSettings>
        get() = context.websocketPreferencesDataStore.data
            .map { pref ->
                pref[Key.SERVER_SETTINGS_JSON]?.let { serverSettingsJson ->
                    runCatching {
                        WebsocketServerSettings.fromJson(serverSettingsJson)
                    }.getOrDefault(WebsocketServerSettings())
                }?: WebsocketServerSettings()
            }
            .flowOn(ioDispatcher)

    override suspend fun update(
        transform: (WebsocketServerSettings) -> WebsocketServerSettings
    ) = withContext(ioDispatcher) {
        val oldSettings = websocketServerSettings.first()
        val newSettings = transform(oldSettings)
        saveWebsocketServerSettings(newSettings)
    }

    private suspend fun saveWebsocketServerSettings(settings: WebsocketServerSettings) {
        context.websocketPreferencesDataStore.edit { pref ->
            pref[Key.SERVER_SETTINGS_JSON] = settings.toJson()
        }
    }
}