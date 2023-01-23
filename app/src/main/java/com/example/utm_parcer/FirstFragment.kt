package com.example.utm_parcer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.android.installreferrer.api.InstallReferrerClient
import com.example.utm_parcer.databinding.FragmentFirstBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    companion object {
        private val UTM_KEYS = hashMapOf<String, String?>(
            Pair("utm_source", null),
            Pair("utm_medium", null),
            Pair("utm_campaign", null),
            Pair("utm_content", null),
            Pair("utm_term", null)
        )
    }

    private var _binding: FragmentFirstBinding? = null
    private lateinit var referrerClient: InstallReferrerClient
    private lateinit var installReferrerStateListener: InstallReferrerStateListenerImpl

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        referrerClient = InstallReferrerClient.newBuilder(requireContext()).build()
        installReferrerStateListener =
            InstallReferrerStateListenerImpl(referrerClient) { referrerUrl -> printUrl(referrerUrl) }
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonFirst.setOnClickListener {
            referrerClient.startConnection(installReferrerStateListener)
        }

        installReferrerStateListener.errorFlow.onEach { message ->
            referrerClient.endConnection()
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun printUrl(referrerUrl: String) {
        referrerClient.endConnection()
        referrerUrl.split('&').forEach { pair ->
            val pairAsArray = pair.trim().split('=')
            val key = pairAsArray[0].trim()
            if (UTM_KEYS.containsKey(key)) UTM_KEYS[key] = pairAsArray[1].trim()
        }
        binding.textviewFirst.text = UTM_KEYS.toString()
    }
}