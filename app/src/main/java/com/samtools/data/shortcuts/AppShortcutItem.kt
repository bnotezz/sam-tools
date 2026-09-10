package com.samtools.data.shortcuts

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class AppShortcutItem(
    val id: String,
    val action: String,
    @StringRes val shortLabelRes: Int,
    @StringRes val longLabelRes: Int,
    @StringRes val descriptionRes: Int,
    @DrawableRes val iconRes: Int,
    val isEnabled: Boolean = true
)
