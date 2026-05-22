package com.kimdb.bench

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.kimdb.KImdb
import com.kimdb.api.KImdbRepository
import com.kimdb.model.TConst
import com.kimdb.model.TitleType
import com.kimdb.tsv.ImdbDatasetManifestGenerator
import java.nio.file.Path
import kotlin.system.measureNanoTime

private class BenchmarkBackendsCommand : CliktCommand(name = "benchmark-backends") {
    private val resourcesDir by option("-r", "--resources-dir", help = "Directory containing IMDb TSV files.")
        .convert { Path.of(it) }
        .default(Path.of("src/main/resources"))

    private val sqlitePath by option("-d", "--db", help = "SQLite DB path produced by importImdbToSqlite.")
        .convert { Path.of(it) }
        .default(Path.of("build/kimdb.db"))

    private val warmup by option("-w", "--warmup", help = "Warmup repetitions per operation.")
        .convert { it.toInt() }
        .default(10)

    private val repetitions by option("-n", "--repetitions", help = "Measured repetitions per operation.")
        .convert { it.toInt() }
        .default(50)

    private val sampleTitle by option("--sample-title", help = "Sample primaryTitle query input.")
        .default("The Avengers")

    private val sampleName by option("--sample-name", help = "Sample primaryName query input.")
        .default("Michelle Williams")

    private val sampleTconst by option("--sample-tconst", help = "Sample tconst query input.")
        .default("tt0110912")

    private val sampleType by option("--sample-type", help = "Sample TitleType (imdb string value).")
        .default(TitleType.MOVIE.imdbValue)

    private val sampleLength by option("--sample-length", help = "Sample primaryTitle length for type+length query.")
        .convert { it.toInt() }
        .default(10)

    override fun run() {
        require(warmup >= 0) { "warmup must be >= 0" }
        require(repetitions > 0) { "repetitions must be > 0" }

        val titleBasics = resourcesDir.resolve(ImdbDatasetManifestGenerator.TITLE_BASICS_TSV)
        val nameBasics = resourcesDir.resolve(ImdbDatasetManifestGenerator.NAME_BASICS_TSV)
        val inMemory = KImdb.inMemoryRepositoryFromTsv(titleBasics, nameBasics)
        val sqlite = KImdb.sqliteRepository(sqlitePath)

        val titleType = TitleType.of(sampleType)
        val tconst = TConst.of(sampleTconst)

        val operations =
            listOf(
                "getTitlesByPrimaryTitle" to { repo: KImdbRepository -> repo.getTitlesByPrimaryTitle(sampleTitle).size },
                "getNamesByPrimaryName" to { repo: KImdbRepository -> repo.getNamesByPrimaryName(sampleName).size },
                "getTitle" to { repo: KImdbRepository -> repo.getTitle(tconst) != null },
                "getTitlesByTypeAndLength" to {
                        repo: KImdbRepository ->
                    repo.getTitlesByTypeAndLength(titleType, sampleLength).size
                }
            )

        echo("Backend benchmark (ns/op): warmup=$warmup repetitions=$repetitions")
        operations.forEach { (name, op) ->
            runWarmup(inMemory, warmup, op)
            runWarmup(sqlite, warmup, op)

            val inMemoryNs = runMeasured(inMemory, repetitions, op)
            val sqliteNs = runMeasured(sqlite, repetitions, op)
            val ratio = sqliteNs.toDouble() / inMemoryNs.toDouble()
            echo("$name: inMemory=${inMemoryNs}ns sqlite=${sqliteNs}ns ratio=${"%.2f".format(ratio)}x")
        }
    }
}

private fun runWarmup(
    repo: KImdbRepository,
    warmup: Int,
    operation: (KImdbRepository) -> Any
) {
    repeat(warmup) { operation(repo) }
}

private fun runMeasured(
    repo: KImdbRepository,
    repetitions: Int,
    operation: (KImdbRepository) -> Any
): Long {
    var totalNs = 0L
    repeat(repetitions) {
        totalNs += measureNanoTime { operation(repo) }
    }

    return totalNs / repetitions
}

fun main(args: Array<String>) {
    BenchmarkBackendsCommand().main(args)
}
