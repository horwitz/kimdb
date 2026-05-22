package com.kimdb.tsv

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import java.nio.file.Path
import java.time.Instant

private class GenerateDatasetManifestCommand : CliktCommand(name = "generate-dataset-manifest") {
    private val resourcesDir by option("-r", "--resources-dir", help = "Directory containing IMDb TSV/GZ files.")
        .convert { Path.of(it) }
        .default(Path.of("src/main/resources"))

    private val outputPath by option("-o", "--output", help = "Output JSON manifest path.")
        .convert { Path.of(it) }
        .default(Path.of("src/main/resources/dataset-manifest.json"))

    private val downloadedAtUtc by option(
        "-t",
        "--downloaded-at-utc",
        help = "Optional download timestamp (ISO-8601 UTC), e.g., 2026-05-22T14:00:00Z."
    ).convert { Instant.parse(it) }

    override fun run() {
        val manifest = ImdbDatasetManifestGenerator.generate(resourcesDir, downloadedAtUtc)
        ImdbDatasetManifestGenerator.writeJson(manifest, outputPath)
        echo("Wrote dataset manifest: $outputPath")
    }
}

fun main(args: Array<String>) {
    GenerateDatasetManifestCommand().main(args)
}
