package com.sathix.app.features.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.sathix.app.databinding.ActivityAuthBinding
import com.sathix.app.features.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (viewModel.isSignedIn()) {
            goHome(); return
        }

        binding.btnSendOtp.setOnClickListener {
            val phone = binding.etPhone.text.toString().trim()
            if (phone.length >= 8) viewModel.sendOtp(this, "+$phone".replace("++", "+"))
            else binding.etPhone.error = "Enter valid phone with country code"
        }

        binding.btnVerify.setOnClickListener {
            val code = binding.etOtp.text.toString().trim()
            if (code.length >= 4) viewModel.verifyOtp(code)
            else binding.etOtp.error = "Enter OTP"
        }

        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                binding.progress.visibility = if (state.loading) android.view.View.VISIBLE else android.view.View.GONE
                binding.tvStatus.text = state.message ?: ""
                binding.layoutOtp.visibility = if (state.otpSent) android.view.View.VISIBLE else android.view.View.GONE
                if (state.signedIn) goHome()
            }
        }
    }

    private fun goHome() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
