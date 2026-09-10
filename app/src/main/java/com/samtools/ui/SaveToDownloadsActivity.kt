package com.samtools.ui

import android.app.DownloadManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.samtools.R
import com.samtools.data.preferences.UserPreferencesRepository
import com.samtools.service.FileSaveManager
import com.samtools.ui.theme.SamToolsTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SaveToDownloadsActivity : ComponentActivity() {

    private lateinit var preferencesRepository: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferencesRepository = UserPreferencesRepository.getInstance(this)

        val uris = extractUrisFromIntent(intent)
        val sharedText = extractTextFromIntent(intent)

        if (uris.isEmpty() && sharedText.isNullOrBlank()) {
            Toast.makeText(this, R.string.save_dialog_no_files, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        lifecycleScope.launch {
            val preferences = preferencesRepository.userPreferencesFlow.first()

            if (preferences.instantSave) {
                // Instant background save mode
                performInstantSave(uris, sharedText, preferences.subfolder)
            } else {
                // Interactive Material 3 BottomSheet mode
                setContent {
                    SamToolsTheme {
                        SaveToDownloadsDialog(
                            uris = uris,
                            sharedText = sharedText,
                            subfolder = preferences.subfolder,
                            onDismiss = { finish() }
                        )
                    }
                }
            }
        }
    }

    private suspend fun performInstantSave(uris: List<Uri>, sharedText: String?, subfolder: String) {
        if (uris.isNotEmpty()) {
            val result = FileSaveManager.saveUrisToDownloads(this, uris, subfolder)
            if (result.isFullSuccess) {
                if (result.successfulFiles.size == 1) {
                    val name = result.successfulFiles.first().displayName
                    Toast.makeText(this, getString(R.string.toast_saved_successfully, name), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, getString(R.string.toast_saved_multiple, result.successfulFiles.size), Toast.LENGTH_SHORT).show()
                }
            } else {
                val error = result.errorMessage ?: "Unknown error"
                Toast.makeText(this, getString(R.string.toast_saving_failed, error), Toast.LENGTH_LONG).show()
            }
        } else if (!sharedText.isNullOrBlank()) {
            val saved = FileSaveManager.saveTextToDownloads(this, sharedText, subfolder)
            if (saved != null) {
                Toast.makeText(this, getString(R.string.toast_saved_successfully, saved.displayName), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, getString(R.string.toast_saving_failed, getString(R.string.error_saving_text)), Toast.LENGTH_LONG).show()
            }
        }
        finish()
    }

    private fun extractUrisFromIntent(intent: Intent): List<Uri> {
        val uriList = mutableListOf<Uri>()

        // 1. Direct intent data
        intent.data?.let { uriList.add(it) }

        // 2. EXTRA_STREAM content
        when (intent.action) {
            Intent.ACTION_SEND -> {
                try {
                    val streamUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(Intent.EXTRA_STREAM) as? Uri
                    }
                    if (streamUri != null) {
                        uriList.add(streamUri)
                    } else {
                        intent.getStringExtra(Intent.EXTRA_STREAM)?.let { str ->
                            try { uriList.add(Uri.parse(str)) } catch (_: Exception) {}
                        }
                    }
                } catch (_: Exception) {
                    try {
                        @Suppress("DEPRECATION")
                        val list = intent.getParcelableArrayListExtra<Parcelable>(Intent.EXTRA_STREAM)
                        list?.filterIsInstance<Uri>()?.let { uriList.addAll(it) }
                    } catch (_: Exception) {}
                }
            }
            Intent.ACTION_SEND_MULTIPLE -> {
                try {
                    val parcelables = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM, Parcelable::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM)
                    }
                    parcelables?.filterIsInstance<Uri>()?.let { uriList.addAll(it) }
                } catch (_: Exception) {}
            }
        }

        // 3. ClipData URIs
        intent.clipData?.let { clipData ->
            for (i in 0 until clipData.itemCount) {
                clipData.getItemAt(i).uri?.let { uriList.add(it) }
            }
        }

        return uriList.distinct()
    }

    private fun extractTextFromIntent(intent: Intent): String? {
        if (intent.action != Intent.ACTION_SEND) return null

        val extraText = intent.getStringExtra(Intent.EXTRA_TEXT)
            ?: intent.getCharSequenceExtra(Intent.EXTRA_TEXT)?.toString()
        if (!extraText.isNullOrBlank()) return extraText

        intent.clipData?.let { clipData ->
            for (i in 0 until clipData.itemCount) {
                val itemText = clipData.getItemAt(i).text?.toString()
                if (!itemText.isNullOrBlank()) return itemText
            }
        }
        return null
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun SaveToDownloadsDialog(
        uris: List<Uri>,
        sharedText: String?,
        subfolder: String,
        onDismiss: () -> Unit
    ) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val coroutineScope = rememberCoroutineScope()

        var isSaving by remember { mutableStateOf(true) }
        var progressFraction by remember { mutableFloatStateOf(0f) }
        var currentFileName by remember { mutableStateOf("") }
        var saveResult by remember { mutableStateOf<FileSaveManager.SaveResult?>(null) }
        var textSavedInfo by remember { mutableStateOf<FileSaveManager.SavedFileInfo?>(null) }

        LaunchedEffect(Unit) {
            if (uris.isNotEmpty()) {
                val res = FileSaveManager.saveUrisToDownloads(
                    context = this@SaveToDownloadsActivity,
                    uris = uris,
                    subfolder = subfolder,
                    onProgress = { current, total, name ->
                        currentFileName = name
                        progressFraction = current.toFloat() / total.toFloat()
                    }
                )
                saveResult = res
            } else if (!sharedText.isNullOrBlank()) {
                textSavedInfo = FileSaveManager.saveTextToDownloads(
                    context = this@SaveToDownloadsActivity,
                    text = sharedText,
                    subfolder = subfolder
                )
            }
            isSaving = false
        }

        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isSaving) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.save_dialog_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (currentFileName.isNotBlank()) currentFileName else stringResource(R.string.save_dialog_preparing),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape)
                    )
                } else {
                    val isSuccess = (saveResult?.isFullSuccess == true) || (textSavedInfo != null)
                    val savedItems = saveResult?.successfulFiles ?: listOfNotNull(textSavedInfo)

                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSuccess) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = if (isSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (isSuccess) {
                            if (savedItems.size == 1) {
                                stringResource(R.string.save_dialog_success_single)
                            } else {
                                stringResource(R.string.save_dialog_success_multiple, savedItems.size)
                            }
                        } else {
                            stringResource(R.string.save_dialog_error, saveResult?.errorMessage ?: stringResource(R.string.error_unknown))
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Saved files list
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (savedItems.size > 1) 120.dp else 60.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(savedItems) { file ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = file.displayName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Action buttons (Material 3 vertical stacked layout with clear hierarchy)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (isSuccess && savedItems.size == 1) {
                            val singleFile = savedItems.first()
                            Button(
                                onClick = {
                                    val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(singleFile.destinationUri, contentResolver.getType(singleFile.destinationUri))
                                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    try {
                                        startActivity(viewIntent)
                                    } catch (e: Exception) {
                                        Toast.makeText(this@SaveToDownloadsActivity, R.string.toast_cannot_open_file, Toast.LENGTH_SHORT).show()
                                    }
                                    onDismiss()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.btn_open_file))
                            }
                        }

                        if (isSuccess) {
                            FilledTonalButton(
                                onClick = {
                                    val downloadsIntent = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    try {
                                        startActivity(downloadsIntent)
                                    } catch (ignored: Exception) {}
                                    onDismiss()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.btn_open_downloads))
                            }
                        }

                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(stringResource(if (isSuccess) R.string.btn_done else R.string.btn_close))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
