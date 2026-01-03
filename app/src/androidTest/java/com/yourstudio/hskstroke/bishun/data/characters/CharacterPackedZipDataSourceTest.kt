package com.yourstudio.hskstroke.bishun.data.characters

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.yourstudio.hskstroke.bishun.data.characters.source.CharacterPackedZipDataSource
import com.yourstudio.hskstroke.bishun.hanzi.parser.CharacterParser
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@RunWith(AndroidJUnit4::class)
class CharacterPackedZipDataSourceTest {
    @Test
    fun loadsFromZipPackFile_whenPresent() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
        val assetBytes = context.assets.open("characters/u6c38.json").use { it.readBytes() }

        val zipFile = File(context.filesDir, "characters.pack.zip")
        ZipOutputStream(zipFile.outputStream()).use { zip ->
            zip.putNextEntry(ZipEntry("characters/u6c38.json"))
            zip.write(assetBytes)
            zip.closeEntry()
        }

        val json = Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }
        val dataSource = CharacterPackedZipDataSource(context = context, json = json)

        val dto = dataSource.load("\u6c38").getOrThrow()
        val definition = CharacterParser().parse("\u6c38", dto)
        assertEquals("\u6c38", definition.symbol)
        assertTrue(definition.strokes.isNotEmpty())
    }
}
