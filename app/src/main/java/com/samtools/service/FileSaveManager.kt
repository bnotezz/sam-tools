package com.samtools.service

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileSaveManager {

    data class SavedFileInfo(
        val originalUri: Uri?,
        val destinationUri: Uri,
        val displayName: String,
        val sizeBytes: Long
    )

    data class SaveResult(
        val totalCount: Int,
        val successfulFiles: List<SavedFileInfo>,
        val failedUris: List<Uri>,
        val errorMessage: String? = null
    ) {
        val isFullSuccess: Boolean get() = failedUris.isEmpty() && successfulFiles.isNotEmpty()
    }

    /**
     * Saves a list of URIs to the device's public Downloads directory.
     */
    suspend fun saveUrisToDownloads(
        context: Context,
        uris: List<Uri>,
        subfolder: String = "",
        onProgress: ((current: Int, total: Int, name: String) -> Unit)? = null
    ): SaveResult = withContext(Dispatchers.IO) {
        val successfulFiles = mutableListOf<SavedFileInfo>()
        val failedUris = mutableListOf<Uri>()
        var lastError: String? = null

        val total = uris.size
        uris.forEachIndexed { index, uri ->
            try {
                val fileName = resolveFileName(context.contentResolver, uri)
                val mimeType = resolveMimeType(context.contentResolver, uri, fileName)

                onProgress?.invoke(index + 1, total, fileName)

                val savedInfo = copyUriToDownloads(
                    context = context,
                    sourceUri = uri,
                    suggestedName = fileName,
                    mimeType = mimeType,
                    subfolder = subfolder
                )

                if (savedInfo != null) {
                    successfulFiles.add(savedInfo)
                } else {
                    failedUris.add(uri)
                }
            } catch (e: Exception) {
                lastError = e.localizedMessage ?: e.message
                failedUris.add(uri)
            }
        }

        SaveResult(
            totalCount = total,
            successfulFiles = successfulFiles,
            failedUris = failedUris,
            errorMessage = lastError
        )
    }

    /**
     * Saves a plain text string (shared via EXTRA_TEXT) as a text file in Downloads.
     */
    suspend fun saveTextToDownloads(
        context: Context,
        text: String,
        subfolder: String = ""
    ): SavedFileInfo? = withContext(Dispatchers.IO) {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "shared_note_$timeStamp.txt"
        val mimeType = "text/plain"

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val relativePath = buildRelativePath(subfolder)
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, mimeType)
                    put(MediaStore.Downloads.RELATIVE_PATH, relativePath)
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }

                val resolver = context.contentResolver
                val targetUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    ?: return@withContext null

                resolver.openOutputStream(targetUri)?.use { out ->
                    out.write(text.toByteArray(Charsets.UTF_8))
                    out.flush()
                }

                values.clear()
                values.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(targetUri, values, null, null)

                SavedFileInfo(
                    originalUri = null,
                    destinationUri = targetUri,
                    displayName = fileName,
                    sizeBytes = text.toByteArray(Charsets.UTF_8).size.toLong()
                )
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val targetDir = if (subfolder.isNotBlank()) File(downloadsDir, sanitizeSubfolder(subfolder)) else downloadsDir
                if (!targetDir.exists()) targetDir.mkdirs()

                val targetFile = File(targetDir, fileName)
                FileOutputStream(targetFile).use { out ->
                    out.write(text.toByteArray(Charsets.UTF_8))
                    out.flush()
                }

                SavedFileInfo(
                    originalUri = null,
                    destinationUri = Uri.fromFile(targetFile),
                    displayName = fileName,
                    sizeBytes = targetFile.length()
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun copyUriToDownloads(
        context: Context,
        sourceUri: Uri,
        suggestedName: String,
        mimeType: String,
        subfolder: String
    ): SavedFileInfo? {
        val resolver = context.contentResolver

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val relativePath = buildRelativePath(subfolder)
            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, suggestedName)
                put(MediaStore.Downloads.MIME_TYPE, mimeType)
                put(MediaStore.Downloads.RELATIVE_PATH, relativePath)
                put(MediaStore.Downloads.IS_PENDING, 1)
            }

            val targetUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                ?: return null

            var bytesCopied = 0L
            resolver.openInputStream(sourceUri)?.use { inputStream ->
                resolver.openOutputStream(targetUri)?.use { outputStream ->
                    val buffer = ByteArray(32 * 1024)
                    var read: Int
                    while (inputStream.read(buffer).also { read = it } != -1) {
                        outputStream.write(buffer, 0, read)
                        bytesCopied += read
                    }
                    outputStream.flush()
                }
            } ?: run {
                resolver.delete(targetUri, null, null)
                return null
            }

            contentValues.clear()
            contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
            resolver.update(targetUri, contentValues, null, null)

            SavedFileInfo(
                originalUri = sourceUri,
                destinationUri = targetUri,
                displayName = suggestedName,
                sizeBytes = bytesCopied
            )
        } else {
            // Android 9 and below
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val targetDir = if (subfolder.isNotBlank()) File(downloadsDir, sanitizeSubfolder(subfolder)) else downloadsDir
            if (!targetDir.exists()) targetDir.mkdirs()

            var targetFile = File(targetDir, sanitizeFileName(suggestedName))
            var counter = 1
            val baseName = targetFile.nameWithoutExtension
            val ext = if (targetFile.extension.isNotBlank()) ".${targetFile.extension}" else ""
            while (targetFile.exists()) {
                targetFile = File(targetDir, "$baseName ($counter)$ext")
                counter++
            }

            var bytesCopied = 0L
            resolver.openInputStream(sourceUri)?.use { inputStream ->
                FileOutputStream(targetFile).use { outputStream ->
                    val buffer = ByteArray(32 * 1024)
                    var read: Int
                    while (inputStream.read(buffer).also { read = it } != -1) {
                        outputStream.write(buffer, 0, read)
                        bytesCopied += read
                    }
                    outputStream.flush()
                }
            } ?: return null

            SavedFileInfo(
                originalUri = sourceUri,
                destinationUri = Uri.fromFile(targetFile),
                displayName = targetFile.name,
                sizeBytes = bytesCopied
            )
        }
    }

    fun resolveFileName(resolver: ContentResolver, uri: Uri): String {
        var name: String? = null

        // Try querying Display Name from ContentResolver
        if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            try {
                resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (index != -1) {
                            name = cursor.getString(index)
                        }
                    }
                }
            } catch (ignored: Exception) {
            }
        }

        // Fallback to path segment
        if (name.isNullOrBlank()) {
            name = uri.lastPathSegment
        }

        // If still null or blank, generate timestamp name
        if (name.isNullOrBlank()) {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            name = "file_$timeStamp"
        }

        name = sanitizeFileName(name)

        // Ensure extension exists if detectable
        if (!name.contains('.')) {
            val mime = resolver.getType(uri)
            if (!mime.isNullOrBlank()) {
                val ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mime)
                if (!ext.isNullOrBlank()) {
                    name = "$name.$ext"
                }
            }
        }

        return name
    }

    fun resolveMimeType(resolver: ContentResolver, uri: Uri, fileName: String): String {
        val resolverType = resolver.getType(uri)
        if (!resolverType.isNullOrBlank() && resolverType != "application/octet-stream") {
            return resolverType
        }

        val dotIndex = fileName.lastIndexOf('.')
        if (dotIndex != -1 && dotIndex < fileName.length - 1) {
            val ext = fileName.substring(dotIndex + 1).lowercase(Locale.getDefault())
            val fromExt = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
            if (!fromExt.isNullOrBlank()) {
                return fromExt
            }
        }

        return resolverType ?: "application/octet-stream"
    }

    fun sanitizeFileName(name: String): String {
        return name.replace("[\\\\/:*?\"<>|]".toRegex(), "_")
            .trim()
            .ifEmpty { "downloaded_file" }
    }

    fun sanitizeSubfolder(subfolder: String): String {
        return subfolder.replace("[\\\\:*?\"<>|]".toRegex(), "_")
            .trim('/', '\\', ' ')
    }

    const val DIRECTORY_DOWNLOADS = "Download"

    fun buildRelativePath(subfolder: String): String {
        val baseDir = try {
            Environment.DIRECTORY_DOWNLOADS ?: DIRECTORY_DOWNLOADS
        } catch (e: Throwable) {
            DIRECTORY_DOWNLOADS
        }
        val sanitized = sanitizeSubfolder(subfolder)
        return if (sanitized.isBlank()) {
            baseDir
        } else {
            "$baseDir/$sanitized"
        }
    }
}
