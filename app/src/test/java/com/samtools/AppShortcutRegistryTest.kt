package com.samtools

import com.samtools.data.preferences.UserPreferences
import com.samtools.data.shortcuts.AppShortcutRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppShortcutRegistryTest {

    @Test
    fun getAllShortcuts_respectsEnabledPreferences() {
        val prefs = UserPreferences(
            enableTailscaleConnect = true,
            enableTailscaleDisconnect = false
        )

        val shortcuts = AppShortcutRegistry.getAllShortcuts(prefs)
        assertEquals(2, shortcuts.size)

        val connect = shortcuts.first { it.id == AppShortcutRegistry.ID_TAILSCALE_CONNECT }
        val disconnect = shortcuts.first { it.id == AppShortcutRegistry.ID_TAILSCALE_DISCONNECT }

        assertTrue(connect.isEnabled)
        assertFalse(disconnect.isEnabled)
        assertEquals(AppShortcutRegistry.ACTION_TAILSCALE_CONNECT, connect.action)
        assertEquals(AppShortcutRegistry.ACTION_TAILSCALE_DISCONNECT, disconnect.action)
    }
}
