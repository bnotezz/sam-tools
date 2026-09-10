package com.samtools.data.preferences

data class UserPreferences(
    val subfolder: String = "",
    val instantSave: Boolean = false,
    val overwriteDuplicates: Boolean = false,
    val enableTailscaleConnect: Boolean = true,
    val enableTailscaleDisconnect: Boolean = true
)
