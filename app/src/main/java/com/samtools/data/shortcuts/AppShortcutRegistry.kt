package com.samtools.data.shortcuts

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.samtools.R
import com.samtools.data.preferences.UserPreferences
import com.samtools.ui.ShortcutTrampolineActivity

object AppShortcutRegistry {

    const val ACTION_TAILSCALE_CONNECT = "com.samtools.action.SHORTCUT_TAILSCALE_CONNECT"
    const val ACTION_TAILSCALE_DISCONNECT = "com.samtools.action.SHORTCUT_TAILSCALE_DISCONNECT"

    const val ID_TAILSCALE_CONNECT = "tailscale_connect"
    const val ID_TAILSCALE_DISCONNECT = "tailscale_disconnect"

    /**
     * Complete list of extensible shortcut definitions available in SAM Tools.
     */
    fun getAllShortcuts(preferences: UserPreferences): List<AppShortcutItem> {
        return listOf(
            AppShortcutItem(
                id = ID_TAILSCALE_CONNECT,
                action = ACTION_TAILSCALE_CONNECT,
                shortLabelRes = R.string.shortcut_tailscale_connect_short,
                longLabelRes = R.string.shortcut_tailscale_connect_label,
                descriptionRes = R.string.shortcut_tailscale_connect_desc,
                iconRes = R.drawable.ic_tailscale_connect,
                isEnabled = preferences.enableTailscaleConnect
            ),
            AppShortcutItem(
                id = ID_TAILSCALE_DISCONNECT,
                action = ACTION_TAILSCALE_DISCONNECT,
                shortLabelRes = R.string.shortcut_tailscale_disconnect_short,
                longLabelRes = R.string.shortcut_tailscale_disconnect_label,
                descriptionRes = R.string.shortcut_tailscale_disconnect_desc,
                iconRes = R.drawable.ic_tailscale_disconnect,
                isEnabled = preferences.enableTailscaleDisconnect
            )
        )
    }

    /**
     * Builds a ShortcutInfoCompat for a given shortcut definition.
     */
    fun buildShortcutInfo(context: Context, item: AppShortcutItem): ShortcutInfoCompat {
        val launchIntent = Intent(context, ShortcutTrampolineActivity::class.java).apply {
            action = item.action
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        return ShortcutInfoCompat.Builder(context, item.id)
            .setShortLabel(context.getString(item.shortLabelRes))
            .setLongLabel(context.getString(item.longLabelRes))
            .setIcon(IconCompat.createWithResource(context, item.iconRes))
            .setIntent(launchIntent)
            .build()
    }

    /**
     * Synchronizes dynamic shortcuts displayed on long-press of the app launcher icon.
     */
    fun updateDynamicShortcuts(context: Context, preferences: UserPreferences) {
        val enabledShortcuts = getAllShortcuts(preferences)
            .filter { it.isEnabled }
            .map { buildShortcutInfo(context, it) }

        ShortcutManagerCompat.setDynamicShortcuts(context, enabledShortcuts)
    }

    /**
     * Requests the launcher to pin the selected shortcut directly to the user's home screen.
     */
    fun requestPinShortcut(context: Context, shortcutId: String, preferences: UserPreferences): Boolean {
        if (!ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            return false
        }

        val item = getAllShortcuts(preferences).firstOrNull { it.id == shortcutId } ?: return false
        val shortcutInfo = buildShortcutInfo(context, item)

        return ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null)
    }
}
