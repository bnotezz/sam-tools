package com.samtools.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.samtools.data.preferences.UserPreferences
import com.samtools.data.preferences.UserPreferencesRepository
import com.samtools.ui.navigation.SamToolsNavGraph
import com.samtools.ui.theme.SamToolsTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var preferencesRepository: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        preferencesRepository = UserPreferencesRepository.getInstance(this)

        setContent {
            val preferences by preferencesRepository.userPreferencesFlow
                .collectAsState(initial = UserPreferences())
            val coroutineScope = rememberCoroutineScope()

            SamToolsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SamToolsNavGraph(
                        preferences = preferences,
                        onUpdateSubfolder = { subfolder ->
                            coroutineScope.launch {
                                preferencesRepository.updateSubfolder(subfolder)
                            }
                        },
                        onToggleInstantSave = { enabled ->
                            coroutineScope.launch {
                                preferencesRepository.updateInstantSave(enabled)
                            }
                        },
                        onToggleOverwriteDuplicates = { enabled ->
                            coroutineScope.launch {
                                preferencesRepository.updateOverwriteDuplicates(enabled)
                            }
                        },
                        onToggleTailscaleConnect = { enabled ->
                            coroutineScope.launch {
                                preferencesRepository.updateEnableTailscaleConnect(enabled)
                            }
                        },
                        onToggleTailscaleDisconnect = { enabled ->
                            coroutineScope.launch {
                                preferencesRepository.updateEnableTailscaleDisconnect(enabled)
                            }
                        }
                    )
                }
            }
        }
    }
}
