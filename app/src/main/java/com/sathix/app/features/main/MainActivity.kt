package com.sathix.app.features.main

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.sathix.app.R
import com.sathix.app.databinding.ActivityMainBinding
import com.sathix.app.features.calls.CallsFragment
import com.sathix.app.features.channels.ChannelsFragment
import com.sathix.app.features.chat.ChatListFragment
import com.sathix.app.features.community.CommunityFragment
import com.sathix.app.features.status.StatusFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val notifPermLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        title = getString(R.string.app_name)

        val pages = listOf(
            "Chats" to { ChatListFragment() },
            "Status" to { StatusFragment() },
            "Calls" to { CallsFragment() },
            "Communities" to { CommunityFragment() },
            "Channels" to { ChannelsFragment() }
        )

        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = pages.size
            override fun createFragment(position: Int) = pages[position].second()
        }
        TabLayoutMediator(binding.tabs, binding.viewPager) { tab, pos ->
            tab.text = pages[pos].first
        }.attach()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
