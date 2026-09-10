package com.samtools

import android.app.Application
import com.samtools.data.preferences.UserPreferencesRepository
import com.samtools.data.shortcuts.AppShortcutRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SamToolsApp : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        val preferencesRepository = UserPreferencesRepository.getInstance(this)
        applicationScope.launch {
            preferencesRepository.userPreferencesFlow.collectLatest { preferences ->
                AppShortcutRegistry.updateDynamicShortcuts(this@SamToolsApp, preferences)
            }
        }
    }
}
