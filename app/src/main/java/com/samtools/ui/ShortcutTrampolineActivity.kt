package com.samtools.ui

import android.app.Activity
import android.os.Bundle
import com.samtools.data.shortcuts.AppShortcutRegistry
import com.samtools.service.TailscaleController

class ShortcutTrampolineActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when (intent?.action) {
            AppShortcutRegistry.ACTION_TAILSCALE_CONNECT -> {
                TailscaleController.connect(this)
            }
            AppShortcutRegistry.ACTION_TAILSCALE_DISCONNECT -> {
                TailscaleController.disconnect(this)
            }
        }

        finish()
    }
}
