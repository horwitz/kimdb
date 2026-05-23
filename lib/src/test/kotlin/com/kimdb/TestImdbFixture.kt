package com.kimdb

import com.kimdb.tsv.ImdbDatasetManifestGenerator
import java.nio.file.Files
import java.nio.file.Path

object TestImdbFixture {
    val resourcesDir by lazy { resolveResourcesDir() }

    val titleBasicsPath: Path by lazy { resourcesDir.resolve(ImdbDatasetManifestGenerator.TITLE_BASICS_TSV) }

    val nameBasicsPath: Path by lazy { resourcesDir.resolve(ImdbDatasetManifestGenerator.NAME_BASICS_TSV) }

    val repository by lazy {
        KImdb.inMemoryRepositoryFromTsv(titleBasicsPath, nameBasicsPath)
    }

    fun resourceDataLineCount(resourceName: String): Long {
        val path = resourcesDir.resolve(resourceName)

        return Files.newBufferedReader(path).useLines { lines ->
            lines.drop(1).count().toLong()
        }
    }

    fun <T> withFileLines(
        path: Path,
        block: (Sequence<String>) -> T
    ) = Files.newBufferedReader(path).useLines(block)

    private fun resolveResourcesDir(): Path {
        val configured = System.getProperty("kimdb.resourcesDir")?.let(Path::of)
        val candidates = buildList {
            if (configured != null) add(configured)
            add(Path.of("src/main/resources"))
            add(Path.of("../src/main/resources"))
        }

        return candidates.firstOrNull { candidate ->
            Files.isRegularFile(candidate.resolve(ImdbDatasetManifestGenerator.TITLE_BASICS_TSV)) &&
                Files.isRegularFile(candidate.resolve(ImdbDatasetManifestGenerator.NAME_BASICS_TSV))
        } ?: error(
            "Could not locate IMDb resources directory. Tried: ${
                candidates.joinToString(", ") { it.toAbsolutePath().normalize().toString() }
            }. Set -Dkimdb.resourcesDir=<path> if needed."
        )
    }
}
