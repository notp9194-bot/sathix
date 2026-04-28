package com.sathix.app.features.calls.webrtc

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.sathix.app.BuildConfig
import org.webrtc.AudioTrack
import org.webrtc.Camera2Enumerator
import org.webrtc.CameraVideoCapturer
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.SessionDescription
import org.webrtc.SurfaceTextureHelper
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoCapturer
import org.webrtc.VideoSource
import org.webrtc.VideoTrack
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebRTCManager @Inject constructor() {

    private var factory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null
    private var localVideoTrack: VideoTrack? = null
    private var localAudioTrack: AudioTrack? = null
    private var videoCapturer: VideoCapturer? = null
    private var videoSource: VideoSource? = null
    private val eglBase: EglBase by lazy { EglBase.create() }

    private var localView: SurfaceViewRenderer? = null
    private var remoteView: SurfaceViewRenderer? = null
    private var audioManager: android.media.AudioManager? = null
    private var muted = false
    private var speakerOn = false

    private val signaling = SignalingClient()

    fun init(ctx: Context, local: SurfaceViewRenderer, remote: SurfaceViewRenderer, video: Boolean) {
        localView = local; remoteView = remote
        audioManager = ctx.getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
        audioManager?.mode = android.media.AudioManager.MODE_IN_COMMUNICATION

        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(ctx)
                .setEnableInternalTracer(false).createInitializationOptions()
        )

        local.init(eglBase.eglBaseContext, null)
        remote.init(eglBase.eglBaseContext, null)
        local.setEnableHardwareScaler(true)
        remote.setEnableHardwareScaler(true)

        val opts = PeerConnectionFactory.Options()
        factory = PeerConnectionFactory.builder()
            .setOptions(opts)
            .setVideoEncoderFactory(DefaultVideoEncoderFactory(eglBase.eglBaseContext, true, true))
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglBase.eglBaseContext))
            .createPeerConnectionFactory()

        createPeerConnection(ctx)

        if (video && hasCameraPermission(ctx)) addLocalVideo(ctx)
        addLocalAudio()
    }

    private fun createPeerConnection(ctx: Context) {
        val iceServers = mutableListOf<PeerConnection.IceServer>()
        iceServers += PeerConnection.IceServer.builder(BuildConfig.STUN_SERVER).createIceServer()
        if (BuildConfig.TURN_SERVER.isNotBlank()) {
            iceServers += PeerConnection.IceServer.builder(BuildConfig.TURN_SERVER)
                .setUsername(BuildConfig.TURN_USERNAME).setPassword(BuildConfig.TURN_PASSWORD).createIceServer()
        }
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        }
        peerConnection = factory?.createPeerConnection(rtcConfig, object : SimplePeerObserver() {
            override fun onIceCandidate(c: IceCandidate?) {
                c?.let { signaling.sendIceCandidate(it) }
            }
            override fun onAddStream(stream: MediaStream?) {
                stream?.videoTracks?.firstOrNull()?.addSink(remoteView)
            }
        })
    }

    private fun addLocalVideo(ctx: Context) {
        val enumerator = Camera2Enumerator(ctx)
        val name = enumerator.deviceNames.firstOrNull { enumerator.isFrontFacing(it) }
            ?: enumerator.deviceNames.firstOrNull() ?: return
        videoCapturer = enumerator.createCapturer(name, null)
        val helper = SurfaceTextureHelper.create("CaptureThread", eglBase.eglBaseContext)
        videoSource = factory?.createVideoSource(false)
        videoCapturer?.initialize(helper, ctx, videoSource?.capturerObserver)
        videoCapturer?.startCapture(640, 480, 30)
        localVideoTrack = factory?.createVideoTrack("LOCAL_VIDEO", videoSource)
        localVideoTrack?.addSink(localView)
        peerConnection?.addTrack(localVideoTrack)
    }

    private fun addLocalAudio() {
        val source = factory?.createAudioSource(MediaConstraints())
        localAudioTrack = factory?.createAudioTrack("LOCAL_AUDIO", source)
        peerConnection?.addTrack(localAudioTrack)
    }

    fun startCall(callId: String, peerId: String) {
        signaling.connect(callId, peerId, isCaller = true) { onSignalingMessage(it) }
        peerConnection?.createOffer(object : SimpleSdpObserver() {
            override fun onCreateSuccess(desc: SessionDescription?) {
                peerConnection?.setLocalDescription(SimpleSdpObserver(), desc)
                desc?.let { signaling.sendOffer(it) }
            }
        }, MediaConstraints())
    }

    fun answerCall(callId: String, peerId: String) {
        signaling.connect(callId, peerId, isCaller = false) { onSignalingMessage(it) }
    }

    private fun onSignalingMessage(msg: SignalingMessage) {
        when (msg) {
            is SignalingMessage.Offer -> {
                peerConnection?.setRemoteDescription(SimpleSdpObserver(),
                    SessionDescription(SessionDescription.Type.OFFER, msg.sdp))
                peerConnection?.createAnswer(object : SimpleSdpObserver() {
                    override fun onCreateSuccess(desc: SessionDescription?) {
                        peerConnection?.setLocalDescription(SimpleSdpObserver(), desc)
                        desc?.let { signaling.sendAnswer(it) }
                    }
                }, MediaConstraints())
            }
            is SignalingMessage.Answer -> {
                peerConnection?.setRemoteDescription(SimpleSdpObserver(),
                    SessionDescription(SessionDescription.Type.ANSWER, msg.sdp))
            }
            is SignalingMessage.Ice -> {
                peerConnection?.addIceCandidate(IceCandidate(msg.sdpMid, msg.sdpMLineIndex, msg.candidate))
            }
            SignalingMessage.HangUp -> endCall()
        }
    }

    fun toggleMute() {
        muted = !muted
        localAudioTrack?.setEnabled(!muted)
    }

    fun toggleSpeaker() {
        speakerOn = !speakerOn
        audioManager?.isSpeakerphoneOn = speakerOn
    }

    fun switchCamera() { (videoCapturer as? CameraVideoCapturer)?.switchCamera(null) }

    fun endCall() {
        runCatching { signaling.sendHangUp() }
        runCatching { videoCapturer?.stopCapture() }
        peerConnection?.close()
        signaling.disconnect()
    }

    fun dispose() {
        endCall()
        videoCapturer?.dispose()
        videoSource?.dispose()
        peerConnection?.dispose(); peerConnection = null
        factory?.dispose(); factory = null
        localView?.release(); remoteView?.release()
    }

    private fun hasCameraPermission(ctx: Context) =
        ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
}
