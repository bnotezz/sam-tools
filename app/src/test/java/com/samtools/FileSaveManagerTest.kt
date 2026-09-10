package com.samtools

import com.samtools.service.FileSaveManager
import org.junit.Assert.assertEquals
import org.junit.Test

class FileSaveManagerTest {

    @Test
    fun sanitizeFileName_removesInvalidCharacters() {
        val input = "my:file/with*illegal?chars.pdf"
        val sanitized = FileSaveManager.sanitizeFileName(input)
        assertEquals("my_file_with_illegal_chars.pdf", sanitized)
    }

    @Test
    fun sanitizeFileName_handlesEmptyOrWhitespace() {
        val input = "   "
        val sanitized = FileSaveManager.sanitizeFileName(input)
        assertEquals("downloaded_file", sanitized)
    }

    @Test
    fun sanitizeSubfolder_removesLeadingTrailingSlashes() {
        val input = "/SAM Tools/Subfolder/ "
        val sanitized = FileSaveManager.sanitizeSubfolder(input)
        assertEquals("SAM Tools/Subfolder", sanitized)
    }

    @Test
    fun buildRelativePath_handlesEmptySubfolder() {
        val path = FileSaveManager.buildRelativePath("")
        assertEquals("Download", path)
    }

    @Test
    fun buildRelativePath_appendsSubfolder() {
        val path = FileSaveManager.buildRelativePath("SAM Tools")
        assertEquals("Download/SAM Tools", path)
    }
}
