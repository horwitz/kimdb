package com.kimdb.tsv

import java.io.BufferedInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.time.Instant
import kotlin.io.path.bufferedReader
import kotlin.io.path.exists
import kotlin.io.path.fileSize

data class DatasetFileManifest(
    val fileName: String,
    val sourceUrl: String,
    val sizeBytes: Long,
    val sha256: String,
    val rowCount: Long? = null,
    val header: String? = null,
)

data class ImdbDatasetManifest(
    val createdAtUtc: String,
    val downloadedAtUtc: String?,
    val files: Map<String, DatasetFileManifest>,
)

object ImdbDatasetManifestGenerator {
    private const val SOURCE_BASE_URL = "https://datasets.imdbws.com/"

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

    fun toJson(manifest: ImdbDatasetManifest): String {
        val fileEntries =
            manifest.files.entries.joinToString(",\n") { (key, value) ->
                """    "${esc(key)}": ${toJson(value)}"""
            }
        return buildString {
            appendLine("{")
            appendLine("""  "createdAtUtc": "${esc(manifest.createdAtUtc)}",""")
            if (manifest.downloadedAtUtc == null) {
                appendLine("""  "downloadedAtUtc": null,""")
            } else {
                appendLine("""  "downloadedAtUtc": "${esc(manifest.downloadedAtUtc)}",""")
            }
            appendLine("""  "files": {""")
            append(fileEntries)
            appendLine()
            appendLine("  }")
            appendLine("}")
        }
    }

    private fun toJson(file: DatasetFileManifest) =
        buildString {
            appendLine("{")
            appendLine("""      "fileName": "${esc(file.fileName)}",""")
            appendLine("""      "sourceUrl": "${esc(file.sourceUrl)}",""")
            appendLine("""      "sizeBytes": ${file.sizeBytes},""")
            appendLine("""      "sha256": "${esc(file.sha256)}",""")
            if (file.rowCount == null) {
                appendLine("""      "rowCount": null,""")
            } else {
                appendLine("""      "rowCount": ${file.rowCount},""")
            }
            if (file.header == null) {
                appendLine("""      "header": null""")
            } else {
                appendLine("""      "header": "${esc(file.header)}"""")
            }
            append("    }")
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

    private fun esc(s: String) =
        s
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
}
