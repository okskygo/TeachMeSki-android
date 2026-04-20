package com.teachmeski.app

import android.app.Application
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.teachmeski.app.notifications.NotificationChannelRegistry
import dagger.hilt.android.HiltAndroidApp
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class TeachMeSkiApp : Application() {

    @Inject
    lateinit var supabaseClient: SupabaseClient

    // App-scoped lifetime; only used for realtime connect/disconnect side-effects.
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        NotificationChannelRegistry.createAll(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(RealtimeLifecycleObserver())
    }

    private inner class RealtimeLifecycleObserver : DefaultLifecycleObserver {
        // App enters foreground (covers cold start + return from background).
        override fun onStart(owner: LifecycleOwner) {
            appScope.launch {
                runCatching { supabaseClient.realtime.connect() }
                    .onFailure { Log.w(TAG, "realtime.connect() failed", it) }
            }
        }

        // App enters background. Disconnect to stop reconnect loop and save Supabase quota.
        // Active channel subscriptions will be re-established by their owning flows
        // (ChatDataSource.roomNewMessagesFlow) when the app returns to foreground.
        override fun onStop(owner: LifecycleOwner) {
            runCatching { supabaseClient.realtime.disconnect() }
                .onFailure { Log.w(TAG, "realtime.disconnect() failed", it) }
        }
    }

    companion object {
        private const val TAG = "TeachMeSkiApp"
    }
}
