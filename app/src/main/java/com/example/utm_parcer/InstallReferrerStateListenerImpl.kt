package com.example.utm_parcer

import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class InstallReferrerStateListenerImpl(
    private val referrerClient: InstallReferrerClient,
    private val printUrl: (String) -> Unit
) : InstallReferrerStateListener {

    private val scope = CoroutineScope(Dispatchers.Main)

    private val _errorFlow = Channel<String>()
    val errorFlow = _errorFlow.receiveAsFlow()

    override fun onInstallReferrerSetupFinished(responseCode: Int) {
        when (responseCode) {
            InstallReferrerClient.InstallReferrerResponse.OK -> {
                // Connection established.
                val referrerUrl = referrerClient.installReferrer.installReferrer
                printUrl(referrerUrl)
            }
            InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED -> {
                // API not available on the current Play Store app.
                sendError("PI not available on the current Play Store app")
            }
            InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE -> {
                // Connection couldn't be established.
                sendError("Connection couldn't be established")
            }
        }
    }

    override fun onInstallReferrerServiceDisconnected() {
        // Try to restart the connection on the next request to
        // Google Play by calling the startConnection() method.
        sendError("Service disconnected..")
    }

    private fun sendError(message: String) {
        scope.launch {
            _errorFlow.send(message)
        }
    }
}