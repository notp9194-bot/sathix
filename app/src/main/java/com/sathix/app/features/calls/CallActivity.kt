package com.sathix.app.features.calls

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.sathix.app.databinding.ActivityCallBinding
import com.sathix.app.domain.model.CallType
import com.sathix.app.features.calls.webrtc.WebRTCManager
import com.sathix.app.services.CallForegroundService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CallActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCallBinding
    private val viewModel: CallActivityViewModel by viewModels()

    @Inject lateinit var webRTC: WebRTCManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val callId = intent.getStringExtra("callId").orEmpty()
        val peerId = intent.getStringExtra("peerId").orEmpty()
        val typeStr = intent.getStringExtra("callType") ?: CallType.AUDIO.name
        val isCaller = intent.getBooleanExtra("isCaller", true)
        val type = CallType.valueOf(typeStr)

        binding.tvPeer.text = peerId
        binding.tvType.text = type.name

        startForegroundService(Intent(this, CallForegroundService::class.java).apply {
            putExtra("callId", callId); putExtra("peerId", peerId); putExtra("callType", type.name)
        })

        webRTC.init(applicationContext, binding.localView, binding.remoteView, type == CallType.VIDEO)
        if (isCaller) webRTC.startCall(callId, peerId) else webRTC.answerCall(callId, peerId)

        binding.btnEnd.setOnClickListener {
            webRTC.endCall()
            stopService(Intent(this, CallForegroundService::class.java))
            viewModel.endCall(callId)
            finish()
        }
        binding.btnMute.setOnClickListener { webRTC.toggleMute() }
        binding.btnSpeaker.setOnClickListener { webRTC.toggleSpeaker() }
        binding.btnSwitchCam.setOnClickListener { webRTC.switchCamera() }
    }

    override fun onDestroy() {
        super.onDestroy()
        webRTC.dispose()
    }
}
