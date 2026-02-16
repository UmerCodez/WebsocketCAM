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
package app.umerfarooq.websocketcam.data.util

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import app.umerfarooq.websocketcam.data.service.WebsocketServerServiceImp
import app.umerfarooq.websocketcam.domain.service.WebsocketServerService
import app.umerfarooq.websocketcam.domain.util.BoundedServiceInstanceProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject


class BoundedServiceInstanceProviderImp @Inject  constructor(
    @param:ApplicationContext val context: Context
): BoundedServiceInstanceProvider {


    private val websocketServerServiceBindHelper = ServiceBindHelper(
        context,
        WebsocketServerServiceImp::class.java
    )

    private val _mqttService: MutableStateFlow<WebsocketServerService?> = MutableStateFlow(null)
    override val websocketServerService = _mqttService.asStateFlow()


    init {
        websocketServerServiceBindHelper.onServiceConnected { binder ->
            val websocketServerService = (binder as WebsocketServerServiceImp.LocalBinder).service
            _mqttService.value = websocketServerService

        }

        websocketServerServiceBindHelper.onServiceConnectionLost {
            _mqttService.value = null
        }

        websocketServerServiceBindHelper.bindToService()

    }

}



private class ServiceBindHelper(
    private val context: Context,
    private val service: Class<out Service>
) : ServiceConnection {

    private var bounded = false
    private var onServiceConnectedCallBack: ((IBinder) -> Unit)? = null
    private var onServiceConnectionLostCallBack: (() -> Unit)? = null



    fun bindToService() {
        Log.d(TAG, "bindToService()")
        val intent = Intent(context, service)
        context.bindService(intent, this, Context.BIND_AUTO_CREATE)
        bounded = true
    }

    fun unBindFromService() {
        Log.d(TAG, "unBindFromService()")
        if (bounded) {
            context.unbindService(this)
            bounded = false
        }
    }

    fun onServiceConnected(callBack: ((IBinder) -> Unit)?) {
        onServiceConnectedCallBack = callBack
    }
    fun onServiceConnectionLost(callBack: (() -> Unit)?) {
        onServiceConnectionLostCallBack = callBack
    }

    override fun onServiceConnected(name: ComponentName, binder: IBinder) {
        Log.d(TAG, "onServiceConnected()")
        bounded = true

        onServiceConnectedCallBack?.invoke(binder)

    }

    /**  The onServiceDisconnected() method in Android is called when the connection to the service is unexpectedly disconnected,
     *   usually due to a crash or the service being killed by the system.
     *   This allows you to handle the situation and possibly attempt to reestablish the connection.
     *   onServiceDisconnected() method is not called when you explicitly call context.unbindService().
     *   It's only called when the connection to the service is unexpectedly lost, such as when the service process crashes or is killed by the system.
     *   */
    override fun onServiceDisconnected(name: ComponentName) {
        Log.d(TAG, "onServiceDisconnected()")

        onServiceConnectionLostCallBack?.invoke()

        bounded = false
        bindToService()
    }


    companion object {
        private val TAG: String = ServiceBindHelper::class.java.simpleName
    }

}