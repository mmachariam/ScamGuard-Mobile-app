package com.example.scamguard.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
internal fun rememberLocalApiPermission(onDenied: () -> Unit): (() -> Unit) -> Unit {
    // Android 17 requires a runtime grant to connect to the local backend.
    if (Build.VERSION.SDK_INT < 37) return { action -> action() }

    val context = LocalContext.current
    val currentOnDenied by rememberUpdatedState(onDenied)
    val permission = Manifest.permission.ACCESS_LOCAL_NETWORK
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val action = pendingAction
        pendingAction = null
        if (granted) action?.invoke() else currentOnDenied()
    }

    return { action ->
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            action()
        } else if (pendingAction == null) {
            pendingAction = action
            launcher.launch(permission)
        }
    }
}
