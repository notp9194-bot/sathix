package com.sathix.app.features.channels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.sathix.app.databinding.FragmentSimpleListBinding

class ChannelsFragment : Fragment() {
    private var _b: FragmentSimpleListBinding? = null
    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentSimpleListBinding.inflate(i, c, false)
        _b!!.tvEmpty.text = "Channels you follow will appear here"
        return _b!!.root
    }
    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
