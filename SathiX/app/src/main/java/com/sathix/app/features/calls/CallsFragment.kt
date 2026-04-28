package com.sathix.app.features.calls

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.sathix.app.databinding.FragmentCallsBinding
import com.sathix.app.features.calls.adapter.CallHistoryAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CallsFragment : Fragment() {
    private var _binding: FragmentCallsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CallsViewModel by viewModels()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentCallsBinding.inflate(i, c, false); return binding.root
    }

    override fun onViewCreated(view: View, s: Bundle?) {
        val adapter = CallHistoryAdapter()
        binding.rv.layoutManager = LinearLayoutManager(requireContext())
        binding.rv.adapter = adapter
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.history.collectLatest { adapter.submitList(it) }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
