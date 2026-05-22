package com.kimdb.tsv

import java.io.BufferedInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.time.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.io.path.bufferedReader
import kotlin.io.path.exists
import kotlin.io.path.fileSize

@Serializable
data class DatasetFileManifest(
    val fileName: String,
    val sourceUrl: String,
    val sizeBytes: Long,
    val sha256: String,
    val rowCount: Long? = null,
    val header: String? = null,
)

@Serializable
data class ImdbDatasetManifest(
    val createdAtUtc: String,
    val downloadedAtUtc: String?,
    val files: Map<String, DatasetFileManifest>,
)

object ImdbDatasetManifestGenerator {
    private const val SOURCE_BASE_URL = "https://datasets.imdbws.com/"
    private val json =
        Json {
            prettyPrint = true
            prettyPrintIndent = "  "
        }

    fun generate(
        resourcesDir: Path,
        downloadedAtUtc: Instant? = null,
    ): ImdbDatasetManifest {
        val files = linkedMapOf<String, DatasetFileManifest>()

        addIfExists(files, resourcesDir, "title.basics.tsv", isTsv = true)
        addIfExists(files, resourcesDir, "title.basics.tsv.gz", isTsv = false)
        addIfExists(files, resourcesDir, "name.basics.tsv", isTsv = true)
        addIfExists(files, resourcesDir, "name.basics.tsv.gz", isTsv = false)

        return ImdbDatasetManifest(Instant.now().toString(), downloadedAtUtc?.toString(), files)
    }

    private fun addIfExists(
        files: MutableMap<String, DatasetFileManifest>,
        resourcesDir: Path,
        fileName: String,
        isTsv: Boolean,
    ) {
        val path = resourcesDir.resolve(fileName)
        if (!path.exists()) return

        files[fileName] =
            if (isTsv) {
                tsvFileManifest(path, fileName)
            } else {
                binaryFileManifest(path, fileName)
            }
    }

    private fun tsvFileManifest(
        path: Path,
        fileName: String,
    ): DatasetFileManifest {
        var header: String? = null
        var rows = 0L
        path.bufferedReader().use { reader ->
            header = reader.readLine()
            while (reader.readLine() != null) {
                rows++
            }
        }
        return DatasetFileManifest(
            fileName = fileName,
            sourceUrl = SOURCE_BASE_URL + fileName.removeSuffix(".tsv") + ".tsv.gz",
            sizeBytes = path.fileSize(),
            sha256 = sha256(path),
            rowCount = rows,
            header = header,
        )
    }

    private fun binaryFileManifest(
        path: Path,
        fileName: String,
    ): DatasetFileManifest =
        DatasetFileManifest(
            fileName = fileName,
            sourceUrl = SOURCE_BASE_URL + fileName,
            sizeBytes = path.fileSize(),
            sha256 = sha256(path),
        )

    fun toJson(manifest: ImdbDatasetManifest) = json.encodeToString(manifest)

    fun writeJson(
        manifest: ImdbDatasetManifest,
        outputPath: Path,
    ) {
        outputPath.parent?.let { Files.createDirectories(it) }
        Files.writeString(outputPath, toJson(manifest))
    }

    private fun sha256(path: Path): String {
        val digest = MessageDigest.getInstance("SHA-256")
        BufferedInputStream(Files.newInputStream(path)).use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                digest.update(buffer, 0, read)
            }
        }

        return digest.digest().joinToString("") { b -> "%02x".format(b) }
    }
}
