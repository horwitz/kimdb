package com.kimdb

import com.kimdb.tsv.ImdbDatasetManifestGenerator
import java.nio.file.Path

object TestImdbFixture {
    val resourcesDir: Path by lazy {
        resolveResourcesDirFromClasspath("/${ImdbDatasetManifestGenerator.TITLE_BASICS_TSV}")
    }

    val repository by lazy {
        KImdb.inMemoryRepositoryFromTsv(
            resourcesDir.resolve(ImdbDatasetManifestGenerator.TITLE_BASICS_TSV),
            resourcesDir.resolve(ImdbDatasetManifestGenerator.NAME_BASICS_TSV)
        )
    }

    fun resourceDataLineCount(resourceName: String): Long {
        val stream =
            object {}.javaClass.getResourceAsStream("/$resourceName")
                ?: error("Cannot get resource $resourceName")

        return stream.bufferedReader().useLines { lines ->
            lines.drop(1).count().toLong()
        }
    }

    private fun resolveResourcesDirFromClasspath(resourcePath: String): Path {
        val url = object {}.javaClass.getResource(resourcePath)
            ?: error("Missing classpath resource: $resourcePath")
        return Path.of(url.toURI()).parent
    }
}
