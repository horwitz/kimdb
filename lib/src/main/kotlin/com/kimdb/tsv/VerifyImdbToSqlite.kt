package com.kimdb.tsv

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import java.nio.file.Files
import java.nio.file.Path
import java.sql.Connection
import java.sql.DriverManager

private class VerifyImdbToSqliteCommand : CliktCommand(name = "verify-imdb-to-sqlite") {
    private val resourcesDir by option("-r", "--resources-dir", help = "Directory containing IMDb TSV files.")
        .convert { Path.of(it) }
        .default(Path.of("src/main/resources"))

    private val sqlitePath by option("-d", "--db", help = "SQLite database path.")
        .convert { Path.of(it) }
        .default(Path.of("build/kimdb.db"))

    override fun run() {
        val titlePath = resourcesDir.resolve(ImdbDatasetManifestGenerator.TITLE_BASICS_TSV)
        val namePath = resourcesDir.resolve(ImdbDatasetManifestGenerator.NAME_BASICS_TSV)
        require(Files.isRegularFile(titlePath)) { "Missing TSV file: $titlePath" }
        require(Files.isRegularFile(namePath)) { "Missing TSV file: $namePath" }
        require(Files.isRegularFile(sqlitePath)) { "Missing SQLite DB file: $sqlitePath" }

        val expectedTitleCount = countRows(titlePath, expectedColumns = 9)
        val expectedNameCount = countRows(namePath, expectedColumns = 6)

        val jdbcUrl = "jdbc:sqlite:${sqlitePath.toAbsolutePath()}"
        DriverManager.getConnection(jdbcUrl).use { connection ->
            val actualTitleCount = queryCount(connection, SqliteSchema.Tables.TITLES)
            val actualNameCount = queryCount(connection, SqliteSchema.Tables.NAMES)

            require(actualTitleCount == expectedTitleCount) {
                "titles row count mismatch: expected=$expectedTitleCount actual=$actualTitleCount"
            }
            require(actualNameCount == expectedNameCount) {
                "names row count mismatch: expected=$expectedNameCount actual=$actualNameCount"
            }

            SqliteSchema.requiredIndexes.forEach { indexName ->
                require(hasIndex(connection, indexName)) { "Missing required index: $indexName" }
            }

            echo(
                "SQLite import verified: titles=$actualTitleCount names=$actualNameCount indexes=${SqliteSchema.requiredIndexes.size} db=$sqlitePath"
            )
        }
    }
}

private fun countRows(
    path: Path,
    expectedColumns: Int
): Long {
    var count = 0L
    forEachTsvRow(path, expectedColumns) { ++count }
    return count
}

private fun queryCount(
    connection: Connection,
    tableName: String
): Long = connection.createStatement().use { statement ->
    statement.executeQuery("SELECT COUNT(*) AS c FROM $tableName").use { resultSet ->
        check(resultSet.next()) { "COUNT query returned no rows for $tableName" }
        resultSet.getLong("c")
    }
}

private fun hasIndex(
    connection: Connection,
    indexName: String
): Boolean = connection.prepareStatement("SELECT 1 FROM sqlite_master WHERE type='index' AND name=? LIMIT 1").use { ps ->
    ps.setString(1, indexName)
    ps.executeQuery().use { rs -> rs.next() }
}

fun main(args: Array<String>) {
    VerifyImdbToSqliteCommand().main(args)
}
