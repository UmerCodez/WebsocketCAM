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
package app.umerfarooq.websocketcam.data.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import app.umerfarooq.websocketcam.data.util.getIp
import app.umerfarooq.websocketcam.domain.model.websocketserver.ServerInfo
import app.umerfarooq.websocketcam.domain.repository.WebsocketServerSettingsRepository
import app.umerfarooq.websocketcam.domain.service.ServerState
import app.umerfarooq.websocketcam.domain.service.WebsocketServerService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.net.InetSocketAddress
import java.net.UnknownHostException
import java.nio.ByteBuffer
import javax.inject.Inject

@AndroidEntryPoint
class WebsocketServerServiceImp : Service(), WebsocketServerService {

    @Inject
    lateinit var websocketServerSettings: WebsocketServerSettingsRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var server: MyWebsocketServer? = null
    private val _serverState = MutableStateFlow<ServerState>(ServerState.Stopped)
    override val serverState: StateFlow<ServerState> get() = _serverState.asStateFlow()
    private var serverJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate()")
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand()")

        trickAndroid8andLater()

        val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager

        // This parent job encapsulates all server-related coroutines.
        // Cancelling it cancels all children (collectors) since they are launched without an explicit scope.

        serverJob?.cancel()
       serverJob =  scope.launch {
            val serverSettings = websocketServerSettings.websocketServerSettings.first()

            // TODO check this later
            val ipAddress = if (serverSettings.listenOnAllInterfaces)
                "0.0.0.0"
            else
                wifiManager.getIp()

            if(ipAddress == null){
                _serverState.emit(ServerState.Error(UnknownHostException("Failed to get IP address")))
                _serverState.emit(ServerState.Stopped)
                stopForeground()
                return@launch
            }

            ipAddress.also { ipAddress ->
                server = MyWebsocketServer( scope = scope, address = InetSocketAddress(ipAddress, serverSettings.port))

                 // Gets cancel when serverJob.cancel() is called
                launch {
                    server?.serverState?.collect { state ->
                        _serverState.emit(state)
                    }
                }

                // Gets cancel when serverJob.cancel() is called
                launch {
                    server?.serverState?.collect { state ->
                        Log.d(TAG, "server state: $state")
                        if(state is ServerState.Running){
                            val notificationIntent = Intent().apply {
                                component = ComponentName(
                                    "app.umerfarooq.websocketcam", // Replace with your actual app package name
                                    "app.umerfarooq.websocketcam.MainActivity" // Fully qualified name of your MainActivity
                                )
                                // flags = Service.START_FLAG_REDELIVERY or Service.START_FLAG_REDELIVERY
                            }

                            val pendingIntent =
                                PendingIntent.getActivity(
                                    applicationContext,
                                    0,
                                    notificationIntent,
                                    PendingIntent.FLAG_IMMUTABLE
                                )

                            val notificationBuilder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                                .apply {
                                    setSmallIcon(android.R.drawable.stat_notify_sync)
                                    setContentTitle("Websocket Server")
                                    setContentText("ws://${state.serverInfo.address}:${state.serverInfo.portNo}")
                                    setPriority(NotificationCompat.PRIORITY_HIGH)
                                    setContentIntent(pendingIntent)
                                    setAutoCancel(false)
                                    setOngoing(true)
                                }


                            val notification = notificationBuilder.build()

                            startForeground(ON_GOING_NOTIFICATION_ID, notification)
                        } else {
                            stopForeground()
                            stopSelf()
                        }
                    }
                }

                launch {
                    server?.start()
                }


            }

        }

        return START_NOT_STICKY
    }


    override fun startServer() {
        ContextCompat.startForegroundService(applicationContext, Intent(applicationContext,
            WebsocketServerServiceImp::class.java))
    }

    override fun stopServer() {
        server?.stop()
        stopSelf()
    }

    override fun broadcast(byteArray: ByteArray) {
        server?.broadcast(byteArray)
    }

    /**
     * For Android 8 and above there is a framework restriction which required service.startForeground()
     * method to be called within five seconds after call to Context.startForegroundService()
     * so make sure we call this method even if we are returning from service.onStartCommand() without calling
     * service.startForeground()
     */
    private fun trickAndroid8andLater() {
        val tempNotificationId = 521

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val tempNotification = NotificationCompat.Builder(
                applicationContext, CHANNEL_ID
            )
                .setContentTitle("WebsocketCAM")
                .setContentText("Initializing...").build()

            startForeground(tempNotificationId, tempNotification)
            stopForeground()
        }
    }


    private fun stopForeground() {
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG, "createNotificationChannel()")
            val name: CharSequence = "Websocket CAM"
            val description = "Notifications from Websocket CAM"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = description
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Binder given to clients
    private val binder: IBinder = LocalBinder()

    override fun onBind(intent: Intent) = binder

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {


        // Return this instance of LocalService so clients can call public methods
        val service: WebsocketServerServiceImp
            get() = this@WebsocketServerServiceImp // Return this instance of LocalService so clients can call public methods

    }

    companion object {
        private val TAG: String = WebsocketServerServiceImp::class.java.getSimpleName()
        const val CHANNEL_ID = "WebsocketCAM-Notification-Channel"
        // cannot be zero
        const val ON_GOING_NOTIFICATION_ID = 245
    }
}


private class MyWebsocketServer(
    private val scope: CoroutineScope,
    private val address: InetSocketAddress
) : WebSocketServer(address) {

    private val tag = "WebsocketServer"

    private val _serverState = MutableStateFlow<ServerState>(ServerState.Stopped)
    val serverState = _serverState.asStateFlow()

    override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {
        Log.d(tag, "new connection to " + conn.remoteSocketAddress)
    }

    override fun onClose(conn: WebSocket, code: Int, reason: String?, remote: Boolean) {
        Log.d(tag, "closed " + conn.remoteSocketAddress + " with exit code " + code + " additional info: " + reason)
    }

    override fun onMessage(conn: WebSocket, message: String?) {
        Log.d(tag, "received message from " + conn.remoteSocketAddress + ": " + message)
    }

    override fun onMessage(conn: WebSocket, message: ByteBuffer?) {
        Log.d(tag, "received ByteBuffer from " + conn.remoteSocketAddress)
    }

    override fun onError(conn: WebSocket?, ex: Exception?) {
        Log.d(tag, "an error occurred on connection " + conn?.remoteSocketAddress + ":" + ex)

        // if conn is null than we have error related to server not to specific connection
        if (conn == null) {
            scope.launch {
                _serverState.emit(ServerState.Error(ex))
                delay(1000)
                // Since the server failed to start, its state is now Stopped.
                // Emitting Stopped ensures that any ViewModel observing this state
                // will be updated accordingly.
                _serverState.emit(ServerState.Stopped)
            }
        }

        ex?.printStackTrace()
    }

    override fun onStart() {
        Log.d(tag, "server started successfully")
        scope.launch {
            _serverState.emit(ServerState.Running(ServerInfo(address.hostString, address.port)))
        }

    }

    override fun stop() {

        scope.launch {
            try {
                super.stop()
            } catch (e: Exception) {
                _serverState.emit(ServerState.Error(e))
            } finally {
                _serverState.emit(ServerState.Stopped)
            }

        }
    }


}