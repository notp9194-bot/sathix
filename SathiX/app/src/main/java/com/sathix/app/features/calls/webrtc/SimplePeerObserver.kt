package com.sathix.app.features.calls.webrtc

import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.RtpReceiver

open class SimplePeerObserver : PeerConnection.Observer {
    override fun onSignalingChange(s: PeerConnection.SignalingState?) {}
    override fun onIceConnectionChange(s: PeerConnection.IceConnectionState?) {}
    override fun onIceConnectionReceivingChange(b: Boolean) {}
    override fun onIceGatheringChange(s: PeerConnection.IceGatheringState?) {}
    override fun onIceCandidate(c: IceCandidate?) {}
    override fun onIceCandidatesRemoved(c: Array<out IceCandidate>?) {}
    override fun onAddStream(s: MediaStream?) {}
    override fun onRemoveStream(s: MediaStream?) {}
    override fun onDataChannel(d: DataChannel?) {}
    override fun onRenegotiationNeeded() {}
    override fun onAddTrack(receiver: RtpReceiver?, streams: Array<out MediaStream>?) {}
}
