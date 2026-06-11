package com.example.data.network

import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.*
import java.util.concurrent.TimeUnit

sealed class ConnectionState {
    object Disconnected : ConnectionState()
    object Connecting : ConnectionState()
    object Connected : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}

class WebSocketService {
    private val client = OkHttpClient.Builder()
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .connectTimeout(10, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    private val _incomingMessages = MutableSharedFlow<String>(extraBufferCapacity = 64)
    val incomingMessages: SharedFlow<String> = _incomingMessages

    fun connect(url: String) {
        val request = try {
            Request.Builder().url(url).build()
        } catch (e: Exception) {
            _connectionState.value = ConnectionState.Error("Invalid URL format: ${e.localizedMessage}")
            return
        }

        _connectionState.value = ConnectionState.Connecting

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                _connectionState.value = ConnectionState.Connected
                Log.d("WebSocketService", "WebSocket connection opened: $url")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                _incomingMessages.tryEmit(text)
                Log.d("WebSocketService", "WebSocket message received: $text")
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
                _connectionState.value = ConnectionState.Disconnected
                Log.d("WebSocketService", "WebSocket closing: $code / $reason")
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                _connectionState.value = ConnectionState.Disconnected
                Log.d("WebSocketService", "WebSocket closed")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                _connectionState.value = ConnectionState.Error(t.localizedMessage ?: "Connection failure")
                Log.e("WebSocketService", "WebSocket failure", t)
            }
        })
    }

    fun sendMessage(message: String): Boolean {
        val ws = webSocket
        return if (ws != null && _connectionState.value is ConnectionState.Connected) {
            ws.send(message)
            Log.d("WebSocketService", "WebSocket message sent: $message")
            true
        } else {
            Log.w("WebSocketService", "WebSocket not connected. Cannot send: $message")
            false
        }
    }

    fun disconnect() {
        webSocket?.close(1000, "User manual disconnect initiated.")
        webSocket = null
        _connectionState.value = ConnectionState.Disconnected
    }
}
