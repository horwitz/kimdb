package com.kimdb.bench

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.kimdb.KImdb
import com.kimdb.api.KImdbRepository
import com.kimdb.model.NConst
import com.kimdb.model.TConst
import com.kimdb.model.TitleType
import com.kimdb.tsv.resolveRequiredImdbInputs
import java.nio.file.Path

private class VerifyBackendsParityCommand : CliktCommand(name = "verify-backends-parity") {
    private val resourcesDir by option("-r", "--resources-dir", help = "Directory containing IMDb TSV files.")
        .convert { Path.of(it) }
        .default(Path.of("src/main/resources"))

    private val sqlitePath by option("-d", "--db", help = "SQLite DB path produced by importImdbToSqlite.")
        .convert { Path.of(it) }
        .default(Path.of("build/kimdb.db"))

    override fun run() {
        val inputs = resolveRequiredImdbInputs(
            resourcesDir = resourcesDir,
            sqlitePath = sqlitePath,
            sqliteMissingMessageSuffix = "(run :lib:importImdbToSqlite first)"
        )
        val inMemory = KImdb.inMemoryRepositoryFromTsv(inputs.titleBasics, inputs.nameBasics)
        val sqlite = KImdb.sqliteRepository(inputs.sqliteDb)

        verifyListOp("titlesByPrimaryTitle(The Ladykillers)", inMemory, sqlite) {
            it.getTitlesByPrimaryTitle("The Ladykillers").toSet()
        }
        verifyListOp("namesByPrimaryName(Michelle Williams)", inMemory, sqlite) {
            it.getNamesByPrimaryName("Michelle Williams").toSet()
        }
        verifyValueOp("titleById(tt0080339)", inMemory, sqlite) {
            it.getTitle(TConst.of("tt0080339"))
        }
        verifyValueOp("nameById(nm0931329)", inMemory, sqlite) {
            it.getName(NConst.of("nm0931329"))
        }
        verifyListOp("titlesByTypeAndLength(movie,10)", inMemory, sqlite) {
            it.getTitlesByTypeAndLength(TitleType.MOVIE, 10).toSet()
        }

        echo("Backend parity verified: in-memory and SQLite results matched for all configured checks.")
    }
}

private fun <T> verifyListOp(
    label: String,
    inMemory: KImdbRepository,
    sqlite: KImdbRepository,
    op: (KImdbRepository) -> Set<T>
) {
    val inMemoryOutput = op(inMemory)
    val sqliteOutput = op(sqlite)
    require(inMemoryOutput == sqliteOutput) { "Backend parity mismatch for $label: inMemory=${inMemoryOutput.size} sqlite=${sqliteOutput.size}" }
}

private fun <T> verifyValueOp(
    label: String,
    inMemory: KImdbRepository,
    sqlite: KImdbRepository,
    op: (KImdbRepository) -> T
) {
    val inMemoryOutput = op(inMemory)
    val sqliteOutput = op(sqlite)
    require(inMemoryOutput == sqliteOutput) { "Backend parity mismatch for $label: inMemory=$inMemoryOutput sqlite=$sqliteOutput" }
}

fun main(args: Array<String>) {
    VerifyBackendsParityCommand().main(args)
}
