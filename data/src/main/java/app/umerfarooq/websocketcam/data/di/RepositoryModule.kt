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
package app.umerfarooq.websocketcam.data.di

import app.umerfarooq.websocketcam.data.repository.CameraSettingsRepositoryImp
import app.umerfarooq.websocketcam.data.repository.WebsocketServerSettingsRepositoryImp
import app.umerfarooq.websocketcam.domain.model.websocketserver.WebsocketServerSettings
import app.umerfarooq.websocketcam.domain.repository.CameraSettingsRepository
import app.umerfarooq.websocketcam.domain.repository.WebsocketServerSettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCameraSettingsRepository(
        cameraSettingsRepositoryImp: CameraSettingsRepositoryImp
    ): CameraSettingsRepository

    @Binds
    @Singleton
    abstract fun bindWebsocketServerSettingsRepository(
        websocketServerSettingsRepositoryImp: WebsocketServerSettingsRepositoryImp
    ): WebsocketServerSettingsRepository
}
