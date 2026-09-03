package com.antigravity.pulsar.data

import android.content.Context
import com.antigravity.pulsar.data.providers.BatteryProvider
import com.antigravity.pulsar.data.providers.CpuProvider
import com.antigravity.pulsar.data.providers.DeviceSpecsProvider
import com.antigravity.pulsar.data.providers.DisplayProvider
import com.antigravity.pulsar.data.providers.MemoryProvider
import com.antigravity.pulsar.data.providers.NetworkProvider
import com.antigravity.pulsar.data.providers.StorageProvider
import com.antigravity.pulsar.data.providers.ThermalProvider
import com.antigravity.pulsar.model.BatteryState
import com.antigravity.pulsar.model.CpuState
import com.antigravity.pulsar.model.DeviceSpecs
import com.antigravity.pulsar.model.DisplayState
import com.antigravity.pulsar.model.MemoryState
import com.antigravity.pulsar.model.NetworkState
import com.antigravity.pulsar.model.StorageState
import com.antigravity.pulsar.model.ThermalState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TelemetryRepository(private val context: Context) {

    private val deviceSpecsProvider = DeviceSpecsProvider(context)
    private val cpuProvider = CpuProvider(deviceSpecsProvider)
    private val memoryProvider = MemoryProvider(context)
    private val batteryProvider = BatteryProvider(context)
    private val thermalProvider = ThermalProvider(context, batteryProvider)
    private val networkProvider = NetworkProvider(context)
    private val storageProvider = StorageProvider()
    private val displayProvider = DisplayProvider(context)
    private val gpuProvider = com.antigravity.pulsar.data.providers.GpuProvider(context)
    private val sensorProvider = com.antigravity.pulsar.data.providers.SensorProvider(context)

    private val _cpuState = MutableStateFlow(CpuState())
    val cpuState: StateFlow<CpuState> = _cpuState.asStateFlow()

    private val _memoryState = MutableStateFlow(MemoryState())
    val memoryState: StateFlow<MemoryState> = _memoryState.asStateFlow()

    private val _batteryState = MutableStateFlow(BatteryState())
    val batteryState: StateFlow<BatteryState> = _batteryState.asStateFlow()

    private val _thermalState = MutableStateFlow(ThermalState())
    val thermalState: StateFlow<ThermalState> = _thermalState.asStateFlow()

    private val _networkState = MutableStateFlow(NetworkState())
    val networkState: StateFlow<NetworkState> = _networkState.asStateFlow()

    private val _storageState = MutableStateFlow(StorageState())
    val storageState: StateFlow<StorageState> = _storageState.asStateFlow()

    private val _displayState = MutableStateFlow(DisplayState())
    val displayState: StateFlow<DisplayState> = _displayState.asStateFlow()

    private val _gpuState = MutableStateFlow(com.antigravity.pulsar.model.GpuState())
    val gpuState: StateFlow<com.antigravity.pulsar.model.GpuState> = _gpuState.asStateFlow()

    private val _sensorsState = MutableStateFlow(com.antigravity.pulsar.model.SensorsState())
    val sensorsState: StateFlow<com.antigravity.pulsar.model.SensorsState> = _sensorsState.asStateFlow()

    private val _deviceSpecs = MutableStateFlow(DeviceSpecs())
    val deviceSpecs: StateFlow<DeviceSpecs> = _deviceSpecs.asStateFlow()

    private val repositoryScope = CoroutineScope(Dispatchers.Default)

    init {
        val initialSpecs = deviceSpecsProvider.getDeviceSpecs()
        val initialGpu = gpuProvider.getGpuState()
        _gpuState.value = initialGpu
        _deviceSpecs.value = initialSpecs.copy(
            gpuRenderer = initialGpu.renderer,
            gpuVendor = initialGpu.vendor,
            vulkanVersion = initialGpu.vulkanVersion,
            totalSensorsCount = sensorProvider.totalSensorsCount
        )
        startTelemetryLoop(1500L)
    }

    fun startTelemetryLoop(intervalMs: Long) {
        repositoryScope.launch {
            while (isActive) {
                try {
                    _cpuState.value = cpuProvider.getCpuState()
                    _memoryState.value = memoryProvider.getMemoryState()
                    _batteryState.value = batteryProvider.getBatteryState()
                    _thermalState.value = thermalProvider.getThermalState()
                    _networkState.value = networkProvider.getNetworkState()
                    _storageState.value = storageProvider.getStorageState()
                    _displayState.value = displayProvider.getDisplayState()
                    _sensorsState.value = sensorProvider.getSensorsState()
                } catch (_: Throwable) {}

                delay(intervalMs)
            }
        }
    }
}