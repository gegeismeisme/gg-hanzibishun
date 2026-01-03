package com.yourstudio.hskstroke.bishun.data.characters.source

import android.content.Context
import com.yourstudio.hskstroke.bishun.data.characters.fs.CharacterAssetPathResolver
import com.yourstudio.hskstroke.bishun.data.characters.model.CharacterJsonDto
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException
import java.util.zip.ZipFile

class CharacterPackedZipDataSource(
    private val context: Context,
    private val json: Json,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val assetPath: String = DEFAULT_ASSET_PATH,
    private val outputFileName: String = DEFAULT_FILE_NAME,
    private val pathResolver: CharacterAssetPathResolver = CharacterAssetPathResolver(),
) {
    private val fileMutex = Mutex()
    private var failedCopyForAppUpdateTime: Long? = null

    suspend fun load(character: String): Result<CharacterJsonDto> = withContext(dispatcher) {
        val candidates = pathResolver.assetCandidatesFor(character)
        if (candidates.isEmpty()) {
            return@withContext Result.failure<CharacterJsonDto>(
                IllegalArgumentException("Character cannot be blank."),
            )
        }

        val zipFile = ensureZipFile() ?: return@withContext Result.failure(
            IOException("Packed asset missing ($assetPath)"),
        )

        runCatching {
            ZipFile(zipFile).use { zip ->
                for (candidate in candidates) {
                    val entry = zip.getEntry(candidate) ?: zip.getEntry(candidate.removePrefix("characters/"))
                    if (entry != null) {
                        zip.getInputStream(entry).use { stream ->
                            val buffer = stream.readBytes()
                            return@use json.decodeFromString(CharacterJsonDto.serializer(), buffer.decodeToString())
                        }
                    }
                }
                throw IOException("No packed entry found for $character")
            }
        }
    }

    private suspend fun ensureZipFile(): File? = fileMutex.withLock {
        val file = File(context.filesDir, outputFileName)
        val appLastUpdateTime = getAppLastUpdateTime()
        val needsRefresh = !file.exists() ||
            file.length() == 0L ||
            (appLastUpdateTime > 0L && file.lastModified() < appLastUpdateTime)
        if (!needsRefresh) return@withLock file
        if (failedCopyForAppUpdateTime == appLastUpdateTime) return@withLock null

        file.parentFile?.mkdirs()
        val tempFile = File(file.absolutePath + ".tmp")
        if (tempFile.exists()) tempFile.delete()

        val copied = runCatching {
            context.assets.open(assetPath).use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            true
        }.getOrDefault(false)

        if (!copied) {
            if (tempFile.exists()) tempFile.delete()
            failedCopyForAppUpdateTime = appLastUpdateTime
            return@withLock null
        }

        failedCopyForAppUpdateTime = null
        if (file.exists()) file.delete()
        if (!tempFile.renameTo(file)) {
            tempFile.copyTo(file, overwrite = true)
            tempFile.delete()
        }
        file
    }

    @Suppress("DEPRECATION")
    private fun getAppLastUpdateTime(): Long {
        return runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).lastUpdateTime
        }.getOrDefault(0L)
    }

    private companion object {
        private const val DEFAULT_ASSET_PATH = "characters/characters.pack.zip"
        private const val DEFAULT_FILE_NAME = "characters.pack.zip"
    }
}
