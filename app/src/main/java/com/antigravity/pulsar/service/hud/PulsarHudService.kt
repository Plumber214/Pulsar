package com.antigravity.pulsar.service.hud

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.antigravity.pulsar.MainActivity
import com.antigravity.pulsar.R
import com.antigravity.pulsar.data.TelemetryRepository
import com.antigravity.pulsar.data.preferences.UserPreferencesRepository
import com.antigravity.pulsar.model.UserPreferences
import com.antigravity.pulsar.theme.PulsarTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted

class PulsarHudService : Service(), LifecycleOwner, SavedStateRegistryOwner, ViewModelStoreOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle get() = lifecycleRegistry

    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    private val customViewModelStore = ViewModelStore()
    override val viewModelStore: ViewModelStore
        get() = customViewModelStore

    override fun onBind(intent: Intent?): IBinder? = null

    private lateinit var windowManager: WindowManager
    private var composeView: ComposeView? = null
    private var windowParams: WindowManager.LayoutParams? = null
    private val fpsMeter = FpsMeter()

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    companion object {
        const val CHANNEL_ID = "pulsar_hud_overlay_channel"
        const val NOTIFICATION_ID = 2002
        const val ACTION_STOP = "com.antigravity.pulsar.ACTION_STOP_HUD"

        private val _isHudActive = MutableStateFlow(false)
        val isHudActive: StateFlow<Boolean> = _isHudActive.asStateFlow()

        fun start(context: Context) {
            val intent = Intent(context, PulsarHudService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, PulsarHudService::class.java)
            context.stopService(intent)
        }

        fun toggle(context: Context) {
            if (_isHudActive.value) {
                stop(context)
            } else {
                if (HudPermissionHelper.canDrawOverlays(context)) {
                    start(context)
                } else {
                    HudPermissionHelper.requestOverlayPermission(context)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        _isHudActive.value = true

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        fpsMeter.start()

        initOverlayView()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun initOverlayView() {
        if (!HudPermissionHelper.canDrawOverlays(this)) {
            stopSelf()
            return
        }

        val telemetryRepo = TelemetryRepository.getInstance(applicationContext)
        val prefsRepo = UserPreferencesRepository(applicationContext)

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            @Suppress("DEPRECATION")
            (WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED),
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 40
            y = 220
        }
        windowParams = params

        composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@PulsarHudService)
            setViewTreeSavedStateRegistryOwner(this@PulsarHudService)
            setViewTreeViewModelStoreOwner(this@PulsarHudService)

            setContent {
                val fps by fpsMeter.fps.collectAsState()
                val cpuState by telemetryRepo.cpuState.collectAsState()
                val memoryState by telemetryRepo.memoryState.collectAsState()
                val batteryState by telemetryRepo.batteryState.collectAsState()
                val thermalState by telemetryRepo.thermalState.collectAsState()
                val prefs by prefsRepo.userPreferencesFlow.collectAsState(initial = UserPreferences())

                PulsarTheme(isAmoled = true) {
                    FloatingHudContent(
                        fps = fps,
                        cpuState = cpuState,
                        memoryState = memoryState,
                        batteryState = batteryState,
                        thermalState = thermalState,
                        tempUnit = prefs.temperatureUnit,
                        onDrag = { dx, dy ->
                            val metrics = resources.displayMetrics
                            params.x = (params.x + dx.toInt()).coerceIn(0, metrics.widthPixels)
                            params.y = (params.y + dy.toInt()).coerceIn(0, metrics.heightPixels)
                            try {
                                windowManager.updateViewLayout(this@apply, params)
                            } catch (_: Exception) {
                                // View detached or ignored
                            }
                        },
                        onDragEnd = {
                            val displayMetrics = resources.displayMetrics
                            val screenWidth = displayMetrics.widthPixels
                            val screenHeight = displayMetrics.heightPixels
                            val viewWidth = this@apply.width.takeIf { it > 0 } ?: 200
                            val viewHeight = this@apply.height.takeIf { it > 0 } ?: 100
                            if (params.x + (viewWidth / 2) < screenWidth / 2) {
                                params.x = 24
                            } else {
                                params.x = (screenWidth - viewWidth - 24).coerceAtLeast(24)
                            }
                            params.y = params.y.coerceIn(60, (screenHeight - viewHeight - 60).coerceAtLeast(60))
                            try {
                                windowManager.updateViewLayout(this@apply, params)
                            } catch (_: Exception) {
                                // Ignore
                            }
                        },
                        onClose = {
                            stopSelf()
                        }
                    )
                }
            }
        }

        try {
            windowManager.addView(composeView, params)
        } catch (e: Exception) {
            e.printStackTrace()
            stopSelf()
        }
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        clampOverlayPosition()
    }

    private fun clampOverlayPosition() {
        val view = composeView ?: return
        val params = windowParams ?: return
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels
        val viewWidth = view.width.takeIf { it > 0 } ?: 200
        val viewHeight = view.height.takeIf { it > 0 } ?: 100

        params.x = params.x.coerceIn(16, (screenWidth - viewWidth - 16).coerceAtLeast(16))
        params.y = params.y.coerceIn(60, (screenHeight - viewHeight - 60).coerceAtLeast(60))
        try {
            windowManager.updateViewLayout(view, params)
        } catch (_: Exception) {}
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Pulsar Floating HUD",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Live Floating Telemetry Overlay"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val launchIntent = Intent(this, MainActivity::class.java)
        val pendingLaunch = PendingIntent.getActivity(
            this, 0, launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = Intent(this, PulsarHudService::class.java).apply { action = ACTION_STOP }
        val pendingStop = PendingIntent.getService(
            this, 2, stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Pulsar HUD Active")
            .setContentText("Tap to return to Pulsar • Close to dismiss overlay")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingLaunch)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Close HUD", pendingStop)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        super.onDestroy()
        _isHudActive.value = false
        fpsMeter.stop()

        composeView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                // Ignore
            }
            composeView = null
        }
        customViewModelStore.clear()
    }
}
