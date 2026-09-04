package com.antigravity.pulsar.service.hud

import android.view.Choreographer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FpsMeter {

    private val _fps = MutableStateFlow(60)
    val fps: StateFlow<Int> = _fps.asStateFlow()

    private var frameCount = 0
    private var lastTimeNanos = 0L
    private var isRunning = false

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!isRunning) return

            if (lastTimeNanos == 0L) {
                lastTimeNanos = frameTimeNanos
            } else {
                frameCount++
                val deltaNanos = frameTimeNanos - lastTimeNanos
                if (deltaNanos >= 1_000_000_000L) {
                    val calculatedFps = (frameCount * 1_000_000_000.0 / deltaNanos).toInt()
                    _fps.value = calculatedFps.coerceIn(1, 240)
                    frameCount = 0
                    lastTimeNanos = frameTimeNanos
                }
            }

            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    fun start() {
        if (isRunning) return
        isRunning = true
        frameCount = 0
        lastTimeNanos = 0L
        Choreographer.getInstance().postFrameCallback(frameCallback)
    }

    fun stop() {
        isRunning = false
        Choreographer.getInstance().removeFrameCallback(frameCallback)
    }
}
