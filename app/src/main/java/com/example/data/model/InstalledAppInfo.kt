package com.example.data.model

import android.graphics.drawable.Drawable

data class InstalledAppInfo(
    val label: String,
    val packageName: String,
    val activityName: String = "",
    val isSystemApp: Boolean = false,
    val icon: Drawable? = null
)
