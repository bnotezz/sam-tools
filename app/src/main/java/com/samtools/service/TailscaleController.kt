package com.samtools.service

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import com.samtools.R

object TailscaleController {

    const val PACKAGE_NAME = "com.tailscale.ipn"
    const val RECEIVER_CLASS = "com.tailscale.ipn.IPNReceiver"

    const val ACTION_CONNECT = "com.tailscale.ipn.CONNECT_VPN"
    const val ACTION_DISCONNECT = "com.tailscale.ipn.DISCONNECT_VPN"

    /**
     * Checks if Tailscale is installed on the device.
     */
    fun isInstalled(context: Context): Boolean {
        return try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    PACKAGE_NAME,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(PACKAGE_NAME, 0)
            }
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    /**
     * Sends broadcast to Tailscale to connect VPN.
     */
    fun connect(context: Context): Boolean {
        if (!isInstalled(context)) {
            Toast.makeText(context, R.string.tailscale_not_installed, Toast.LENGTH_LONG).show()
            openInPlayStore(context)
            return false
        }

        val intent = Intent(ACTION_CONNECT).apply {
            setPackage(PACKAGE_NAME)
            setClassName(PACKAGE_NAME, RECEIVER_CLASS)
        }
        context.sendBroadcast(intent)
        Toast.makeText(context, R.string.tailscale_connecting, Toast.LENGTH_SHORT).show()
        return true
    }

    /**
     * Sends broadcast to Tailscale to disconnect VPN.
     */
    fun disconnect(context: Context): Boolean {
        if (!isInstalled(context)) {
            Toast.makeText(context, R.string.tailscale_not_installed, Toast.LENGTH_LONG).show()
            openInPlayStore(context)
            return false
        }

        val intent = Intent(ACTION_DISCONNECT).apply {
            setPackage(PACKAGE_NAME)
            setClassName(PACKAGE_NAME, RECEIVER_CLASS)
        }
        context.sendBroadcast(intent)
        Toast.makeText(context, R.string.tailscale_disconnecting, Toast.LENGTH_SHORT).show()
        return true
    }

    /**
     * Opens Tailscale on Google Play Store.
     */
    fun openInPlayStore(context: Context) {
        val playStoreIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$PACKAGE_NAME")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(playStoreIntent)
        } catch (e: Exception) {
            val webIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$PACKAGE_NAME")
            ).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(webIntent)
        }
    }
}
