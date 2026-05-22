package com.kimdb.tsv

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import java.nio.file.Path
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.Types

private class ImportImdbToSqliteCommand : CliktCommand(name = "import-imdb-to-sqlite") {
    private val resourcesDir by option("-r", "--resources-dir", help = "Directory containing IMDb TSV files.")
        .convert { Path.of(it) }
        .default(Path.of("src/main/resources"))

    private val sqlitePath by option("-d", "--db", help = "SQLite database output path.")
        .convert { Path.of(it) }
        .default(Path.of("build/kimdb.db"))

    private val batchSize by option("-b", "--batch-size", help = "JDBC batch size.")
        .convert { it.toInt() }
        .default(2_000)

    override fun run() {
        val titlePath = resourcesDir.resolve(ImdbDatasetManifestGenerator.TITLE_BASICS_TSV)
        val namePath = resourcesDir.resolve(ImdbDatasetManifestGenerator.NAME_BASICS_TSV)
        val jdbcUrl = "jdbc:sqlite:${sqlitePath.toAbsolutePath()}"

        DriverManager.getConnection(jdbcUrl).use { connection ->
            connection.autoCommit = false
            createSchema(connection)
            importTitles(connection, titlePath, batchSize)
            importNames(connection, namePath, batchSize)
            createIndexes(connection)
            connection.commit()
        }

        echo("Imported IMDb TSV data into SQLite: $sqlitePath")
    }
}

private fun createSchema(connection: Connection) {
    connection.createStatement().use { statement ->
        statement.execute(
            """
            CREATE TABLE IF NOT EXISTS ${SqliteSchema.Tables.TITLES} (
              tconst TEXT PRIMARY KEY,
              titleType TEXT NOT NULL,
              primaryTitle TEXT NOT NULL,
              originalTitle TEXT NOT NULL,
              isAdult TEXT NOT NULL,
              startYear INTEGER,
              endYear INTEGER,
              runTimeMinutes INTEGER,
              genres TEXT,
              primaryTitleLength INTEGER NOT NULL
            );
            """.trimIndent()
        )
        statement.execute(
            """
            CREATE TABLE IF NOT EXISTS ${SqliteSchema.Tables.NAMES} (
              nconst TEXT PRIMARY KEY,
              primaryName TEXT NOT NULL,
              birthYear INTEGER,
              deathYear INTEGER,
              primaryProfession TEXT,
              knownForTitles TEXT
            );
            """.trimIndent()
        )
        statement.execute("DELETE FROM ${SqliteSchema.Tables.TITLES};")
        statement.execute("DELETE FROM ${SqliteSchema.Tables.NAMES};")
    }
}

private fun importTitles(
    connection: Connection,
    titlePath: Path,
    batchSize: Int
) {
    val sql =
        """
        INSERT INTO titles (
          tconst, titleType, primaryTitle, originalTitle, isAdult, startYear, endYear, runTimeMinutes, genres, primaryTitleLength
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

    connection.prepareStatement(sql).use { ps ->
        var count = 0
        forEachTsvRow(titlePath, expectedColumns = 9) { cols ->
            val primaryTitle = cols[2]
            ps.setString(1, cols[0])
            ps.setString(2, cols[1])
            ps.setString(3, primaryTitle)
            ps.setString(4, cols[3])
            ps.setString(5, cols[4])
            ps.setNullableInt(6, cols[5])
            ps.setNullableInt(7, cols[6])
            ps.setNullableInt(8, cols[7])
            ps.setNullableString(9, cols[8])
            ps.setInt(10, primaryTitle.length)
            ps.addBatch()
            ++count
            if (count % batchSize == 0) ps.executeBatch()
        }
        ps.executeBatch()
    }
}

private fun importNames(
    connection: Connection,
    namePath: Path,
    batchSize: Int
) {
    val sql =
        """
        INSERT INTO names (
          nconst, primaryName, birthYear, deathYear, primaryProfession, knownForTitles
        ) VALUES (?, ?, ?, ?, ?, ?)
        """.trimIndent()

    connection.prepareStatement(sql).use { ps ->
        var count = 0
        forEachTsvRow(namePath, expectedColumns = 6) { cols ->
            ps.setString(1, cols[0])
            ps.setString(2, cols[1])
            ps.setNullableInt(3, cols[2])
            ps.setNullableInt(4, cols[3])
            ps.setNullableString(5, cols[4])
            ps.setNullableString(6, cols[5])
            ps.addBatch()
            ++count
            if (count % batchSize == 0) ps.executeBatch()
        }
        ps.executeBatch()
    }
}

private fun createIndexes(connection: Connection) {
    connection.createStatement().use { statement ->
        statement.execute(
            "CREATE INDEX IF NOT EXISTS ${SqliteSchema.Indexes.TITLES_TCONST} ON ${SqliteSchema.Tables.TITLES}(tconst);"
        )
        statement.execute(
            "CREATE INDEX IF NOT EXISTS ${SqliteSchema.Indexes.TITLES_PRIMARY_TITLE} ON ${SqliteSchema.Tables.TITLES}(primaryTitle);"
        )
        statement.execute(
            "CREATE INDEX IF NOT EXISTS ${SqliteSchema.Indexes.TITLES_TYPE_LENGTH} ON ${SqliteSchema.Tables.TITLES}(titleType, primaryTitleLength);"
        )
        statement.execute(
            "CREATE INDEX IF NOT EXISTS ${SqliteSchema.Indexes.NAMES_NCONST} ON ${SqliteSchema.Tables.NAMES}(nconst);"
        )
        statement.execute(
            "CREATE INDEX IF NOT EXISTS ${SqliteSchema.Indexes.NAMES_PRIMARY_NAME} ON ${SqliteSchema.Tables.NAMES}(primaryName);"
        )
    }
}

private fun PreparedStatement.setNullableInt(
    index: Int,
    value: String
) = setNullableToken(index, value, Types.INTEGER) { setInt(index, it.toInt()) }

private fun PreparedStatement.setNullableString(
    index: Int,
    value: String
) = setNullableToken(index, value, Types.VARCHAR) { setString(index, it) }

private inline fun PreparedStatement.setNullableToken(
    index: Int,
    value: String,
    sqlType: Int,
    setValue: PreparedStatement.(String) -> Unit
) {
    if (value == NULL_TOKEN) {
        setNull(index, sqlType)
    } else {
        setValue(value)
    }
}

fun main(args: Array<String>) {
    ImportImdbToSqliteCommand().main(args)
}
