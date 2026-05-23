package com.kimdb.tsv

import java.nio.file.Files
import java.nio.file.Path

data class ImdbInputPaths(
    val titleBasics: Path,
    val nameBasics: Path,
    val sqliteDb: Path
)

fun resolveRequiredImdbInputs(
    resourcesDir: Path,
    sqlitePath: Path,
    sqliteMissingMessageSuffix: String? = null
): ImdbInputPaths {
    val titleBasics = resourcesDir.resolve(ImdbDatasetManifestGenerator.TITLE_BASICS_TSV)
    val nameBasics = resourcesDir.resolve(ImdbDatasetManifestGenerator.NAME_BASICS_TSV)
    require(Files.isRegularFile(titleBasics)) { "Missing TSV file: $titleBasics" }
    require(Files.isRegularFile(nameBasics)) { "Missing TSV file: $nameBasics" }

    val sqliteMessage =
        if (sqliteMissingMessageSuffix == null) {
            "Missing SQLite DB file: $sqlitePath"
        } else {
            "Missing SQLite DB file: $sqlitePath $sqliteMissingMessageSuffix"
        }
    require(Files.isRegularFile(sqlitePath)) { sqliteMessage }

    return ImdbInputPaths(
        titleBasics = titleBasics,
        nameBasics = nameBasics,
        sqliteDb = sqlitePath
    )
}
