package com.antigravity.pulsar.data.providers

import android.content.Context
import android.content.pm.PackageManager
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLSurface
import android.opengl.GLES20
import android.os.Build
import com.antigravity.pulsar.model.GpuState

class GpuProvider(private val context: Context) {

    private var cachedGpuState: GpuState? = null

    fun getGpuState(): GpuState {
        cachedGpuState?.let { return it }

        var renderer = "Standard Android GPU"
        var vendor = "Unknown"
        var glesVersion = "OpenGL ES 3.2"
        var extCount = 0

        try {
            val dpy: EGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
            val vers = IntArray(2)
            EGL14.eglInitialize(dpy, vers, 0, vers, 1)

            val configAttr = intArrayOf(
                EGL14.EGL_RENDERABLE_TYPE, 4,
                EGL14.EGL_NONE
            )
            val configs = arrayOfNulls<EGLConfig>(1)
            val numConfig = IntArray(1)
            EGL14.eglChooseConfig(dpy, configAttr, 0, configs, 0, 1, numConfig, 0)

            if (numConfig[0] > 0 && configs[0] != null) {
                val ctxAttr = intArrayOf(
                    12440, 2,
                    EGL14.EGL_NONE
                )
                val ctx: EGLContext = EGL14.eglCreateContext(dpy, configs[0], EGL14.EGL_NO_CONTEXT, ctxAttr, 0)

                val surfAttr = intArrayOf(
                    EGL14.EGL_WIDTH, 1,
                    EGL14.EGL_HEIGHT, 1,
                    EGL14.EGL_NONE
                )
                val surf: EGLSurface = EGL14.eglCreatePbufferSurface(dpy, configs[0], surfAttr, 0)
                EGL14.eglMakeCurrent(dpy, surf, surf, ctx)

                val r = GLES20.glGetString(GLES20.GL_RENDERER)
                val v = GLES20.glGetString(GLES20.GL_VENDOR)
                val ver = GLES20.glGetString(GLES20.GL_VERSION)
                val ext = GLES20.glGetString(GLES20.GL_EXTENSIONS)

                if (!r.isNullOrBlank()) renderer = r
                if (!v.isNullOrBlank()) vendor = v
                if (!ver.isNullOrBlank()) glesVersion = ver
                if (!ext.isNullOrBlank()) extCount = ext.split(" ").filter { it.isNotBlank() }.size

                EGL14.eglMakeCurrent(dpy, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)
                EGL14.eglDestroySurface(dpy, surf)
                EGL14.eglDestroyContext(dpy, ctx)
                EGL14.eglTerminate(dpy)
            }
        } catch (_: Throwable) {
            val soc = Build.SOC_MODEL.lowercase()
            if (soc.contains("gs201") || soc.contains("tensor g2")) {
                renderer = "Mali-G710 MP7"
                vendor = "ARM"
            } else if (soc.contains("g5") || soc.contains("g6") || soc.contains("tensor")) {
                renderer = "Mali-G715 / Immortalis"
                vendor = "ARM"
            }
        }

        val vulkanVersion = getVulkanVersion()

        val result = GpuState(
            renderer = renderer,
            vendor = vendor,
            glesVersion = glesVersion,
            vulkanVersion = vulkanVersion,
            extensionCount = extCount
        )
        cachedGpuState = result
        return result
    }

    private fun getVulkanVersion(): String {
        return try {
            val pm = context.packageManager
            if (pm.hasSystemFeature(PackageManager.FEATURE_VULKAN_HARDWARE_VERSION)) {
                val features = pm.systemAvailableFeatures
                for (feature in features) {
                    if (PackageManager.FEATURE_VULKAN_HARDWARE_VERSION == feature.name) {
                        val major = (feature.version shr 22) and 0x7F
                        val minor = (feature.version shr 12) and 0x3FF
                        return "Vulkan $major.$minor"
                    }
                }
            }
            "Vulkan 1.3"
        } catch (_: Throwable) {
            "Vulkan 1.3"
        }
    }
}