package com.sathix.app.features.calls.webrtc

import com.sathix.app.BuildConfig
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import timber.log.Timber

sealed class SignalingMessage {
    data class Offer(val sdp: String) : SignalingMessage()
    data class Answer(val sdp: String) : SignalingMessage()
    data class Ice(val sdpMid: String?, val sdpMLineIndex: Int, val candidate: String) : SignalingMessage()
    data object HangUp : SignalingMessage()
}

class SignalingClient {

    private var socket: Socket? = null
    private var roomId: String = ""
    private var onMessage: ((SignalingMessage) -> Unit)? = null

    fun connect(callId: String, peerId: String, isCaller: Boolean, listener: (SignalingMessage) -> Unit) {
        roomId = callId
        onMessage = listener
        runCatching {
            val opts = IO.Options().apply {
                forceNew = true
                reconnection = true
                query = "callId=$callId&peerId=$peerId&caller=$isCaller"
            }
            socket = IO.socket(BuildConfig.SIGNALING_SERVER_URL, opts)
            socket?.on(Socket.EVENT_CONNECT) { socket?.emit("join", JSONObject().put("room", roomId)) }
            socket?.on("offer") { args ->
                (args.firstOrNull() as? JSONObject)?.optString("sdp")?.let {
                    onMessage?.invoke(SignalingMessage.Offer(it))
                }
            }
            socket?.on("answer") { args ->
                (args.firstOrNull() as? JSONObject)?.optString("sdp")?.let {
                    onMessage?.invoke(SignalingMessage.Answer(it))
                }
            }
            socket?.on("ice") { args ->
                (args.firstOrNull() as? JSONObject)?.let { j ->
                    onMessage?.invoke(
                        SignalingMessage.Ice(
                            j.optString("sdpMid"),
                            j.optInt("sdpMLineIndex"),
                            j.optString("candidate")
                        )
                    )
                }
            }
            socket?.on("hangup") { onMessage?.invoke(SignalingMessage.HangUp) }
            socket?.connect()
        }.onFailure { Timber.e(it, "Signaling connect failed") }
    }

    fun sendOffer(desc: SessionDescription) = emit("offer", JSONObject().put("sdp", desc.description).put("room", roomId))
    fun sendAnswer(desc: SessionDescription) = emit("answer", JSONObject().put("sdp", desc.description).put("room", roomId))
    fun sendIceCandidate(c: IceCandidate) = emit("ice", JSONObject()
        .put("candidate", c.sdp).put("sdpMid", c.sdpMid).put("sdpMLineIndex", c.sdpMLineIndex).put("room", roomId))
    fun sendHangUp() = emit("hangup", JSONObject().put("room", roomId))

    private fun emit(event: String, payload: JSONObject) { socket?.emit(event, payload) }

    fun disconnect() { socket?.disconnect(); socket = null }
}
