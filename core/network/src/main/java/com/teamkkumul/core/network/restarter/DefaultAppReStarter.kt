package com.teamkkumul.core.network.restarter

import android.content.Context
import android.content.Intent
import android.widget.Toast
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class DefaultAppReStarter @Inject constructor(
    @ApplicationContext private val context: Context,
) : AppReStarter {
    private var currentToast: Toast? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun restartApp() {
        scope.launch {
            val restartIntent = context.packageManager
                .getLaunchIntentForPackage(context.packageName)
                ?.component
                ?.let(Intent::makeRestartActivityTask)

            context.startActivity(restartIntent)
        }
    }

    override fun makeToast(message: String) {
        scope.launch {
            currentToast?.cancel()
            currentToast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
            currentToast?.show()
        }
    }
}
