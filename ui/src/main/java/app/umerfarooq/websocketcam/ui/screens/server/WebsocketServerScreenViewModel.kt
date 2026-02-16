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
package app.umerfarooq.websocketcam.ui.screens.server

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.umerfarooq.websocketcam.domain.repository.WebsocketServerSettingsRepository
import app.umerfarooq.websocketcam.domain.service.ServerState
import app.umerfarooq.websocketcam.domain.service.WebsocketServerService
import app.umerfarooq.websocketcam.domain.util.BoundedServiceInstanceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WebsocketServerScreenViewModel @Inject constructor(
    private val websocketServerSettingsRepository: WebsocketServerSettingsRepository,
    private val boundedServiceInstanceProvider: BoundedServiceInstanceProvider
) : ViewModel() {

    private val _state = MutableStateFlow(ServerScreenState())
    val state = _state.asStateFlow()

    private val tag = WebsocketServerScreenViewModel::class.java.simpleName

    var websocketServerService: WebsocketServerService? = null

    init {


        Log.d(tag, "init()")

        viewModelScope.launch {

            boundedServiceInstanceProvider.websocketServerService
                .filterNotNull()
                .flatMapLatest { websocketServerService ->
                    this@WebsocketServerScreenViewModel.websocketServerService = websocketServerService
                    websocketServerService.serverState
                }.collect { serverState ->
                    _state.update {
                        it.copy(
                            serverState = serverState,
                            address = when (serverState) {
                                is ServerState.Running -> "ws://${serverState.serverInfo.address}:${serverState.serverInfo.portNo}"
                                else -> ""
                            }
                        )
                    }
                }
        }


        viewModelScope.launch {
            websocketServerSettingsRepository.websocketServerSettings.collect { settings ->
                _state.update {
                    it.copy(serverSettings = settings)
                }
            }
        }

    }


    fun onEvent(event: ServerScreenEvent) {
        when (event) {
            is ServerScreenEvent.OnStartClicked -> {
                websocketServerService?.startServer()
            }
            is ServerScreenEvent.OnStopClicked -> {
                websocketServerService?.stopServer()
            }

            is ServerScreenEvent.OnListenOnAllInterfacesChanged -> {
                viewModelScope.launch {
                    websocketServerSettingsRepository.update {
                        it.copy(listenOnAllInterfaces = event.listenOnAllInterfaces)
                    }
                }

            }
            is ServerScreenEvent.OnPortChanged -> {
                viewModelScope.launch {
                    websocketServerSettingsRepository.update {
                        it.copy(port = event.port)
                    }
                }
            }
        }
    }


    override fun onCleared() {
        super.onCleared()
        Log.d(tag, "onCleared()")
    }

}