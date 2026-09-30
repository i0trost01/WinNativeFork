package com.winlator.cmod.feature.shortcuts

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files

class FrontendExporterTest {
    @Test
    fun generatedExportExtensionsAreRecognised() {
        listOf(
                "Hades.desktop",
                "Hades.png",
                "Hades.steam",
                "Hades.steamappid",
                "Hades.epic",
                "Hades.gog",
                "Hades.STEAM",
            )
            .forEach { assertTrue(it, FrontendExporter.isGeneratedExportFile(it)) }
    }

    @Test
    fun unrelatedFilesAreNotConsideredGenerated() {
        listOf("readme.txt", "game.iso", "cover.jpg", "noext", "notes.desktop.bak")
            .forEach { assertFalse(it, FrontendExporter.isGeneratedExportFile(it)) }
    }

    @Test
    fun losslessScalingIsExcludedFromExport() {
        assertTrue(FrontendExporter.isExcludedFromExport("STEAM", "993090"))
        assertTrue(FrontendExporter.isExcludedFromExport("steam", "993090"))
    }

    @Test
    fun realGamesAreNotExcludedFromExport() {
        assertFalse(FrontendExporter.isExcludedFromExport("STEAM", "730"))
        assertFalse(FrontendExporter.isExcludedFromExport("EPIC", "993090"))
        assertFalse(FrontendExporter.isExcludedFromExport(null, "993090"))
    }

    @Test
    fun clearExportedFilesRemovesOnlyGeneratedFiles() {
        val dir = Files.createTempDirectory("frontend-export").toFile()
        File(dir, "Hades.desktop").writeText("[Desktop Entry]")
        File(dir, "Hades.png").writeBytes(byteArrayOf(1, 2, 3))
        File(dir, "Hades.steam").writeText("1145360")
        File(dir, "Hades.steamappid").writeText("1145360")
        File(dir, "keep.txt").writeText("user data")
        File(dir, "art.jpg").writeBytes(byteArrayOf(4, 5))
        File(dir, "subdir").mkdirs()

        val deleted = FrontendExporter.clearExportedFiles(dir)

        assertEquals(4, deleted)
        assertFalse(File(dir, "Hades.desktop").exists())
        assertFalse(File(dir, "Hades.png").exists())
        assertFalse(File(dir, "Hades.steam").exists())
        assertFalse(File(dir, "Hades.steamappid").exists())
        assertTrue(File(dir, "keep.txt").exists())
        assertTrue(File(dir, "art.jpg").exists())
        assertTrue(File(dir, "subdir").isDirectory)
        dir.deleteRecursively()
    }
}
