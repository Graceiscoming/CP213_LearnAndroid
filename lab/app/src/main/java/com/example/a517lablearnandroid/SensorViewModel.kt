package com.example.a517lablearnandroid

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SensorViewModel(application: Application) : AndroidViewModel(application) {
    private val sensorTracker = SensorTracker(application)

    private val _sensorData = MutableStateFlow(floatArrayOf(0f, 0f, 0f))
    val sensorData: StateFlow<FloatArray> = _sensorData.asStateFlow()

    init {
        viewModelScope.launch {
            sensorTracker.getAccelerometerData().collect { values ->
                _sensorData.value = values
            }
        }
    }
}
