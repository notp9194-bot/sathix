package com.sathix.app.features.status

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.sathix.app.databinding.FragmentStatusBinding
import com.sathix.app.features.status.adapter.StatusAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StatusFragment : Fragment() {
    private var _binding: FragmentStatusBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StatusViewModel by viewModels()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentStatusBinding.inflate(i, c, false); return binding.root
    }

    override fun onViewCreated(view: View, s: Bundle?) {
        val adapter = StatusAdapter()
        binding.rv.layoutManager = LinearLayoutManager(requireContext())
        binding.rv.adapter = adapter
        binding.fab.setOnClickListener { viewModel.postTextStatus("Hello SathiX!") }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.statuses.collectLatest { adapter.submitList(it.entries.toList()) }
        }
    }
    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
