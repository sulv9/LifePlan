package com.sulv.lifeplan

import App
import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import platform.addCalendarEvent
import platform.remindTimeCalendarRequest

class MainActivity : ComponentActivity() {

    private val calendarWriteRequest: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            if (
                result[Manifest.permission.WRITE_CALENDAR] == true &&
                result[Manifest.permission.READ_CALENDAR] == true
            ) addCalendarEvent(this)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        remindTimeCalendarRequest = calendarWriteRequest
        setContent {
            App()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        remindTimeCalendarRequest = null
    }
}