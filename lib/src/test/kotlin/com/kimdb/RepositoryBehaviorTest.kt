package com.kimdb

import com.kimdb.model.NConst
import com.kimdb.model.TConst
import com.kimdb.model.TitleType
import com.kimdb.tsv.ImdbDatasetManifestGenerator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Path

class RepositoryBehaviorTest {
    @Test
    fun getTitleByIdReturnsExpectedKnownMovie() {
        val title = requireNotNull(repository.getTitle(TConst.of("tt0080339")))
        assertEquals("Airplane!", title.primaryTitle)
    }

    @Test
    fun getNamesByPrimaryNameContainsExpectedKnownId() {
        val names = repository.getNamesByPrimaryName("Michelle Williams")
        assertTrue(names.isNotEmpty())
        assertTrue(names.any { it.nconst == NConst.of("nm0931329") })
    }

    @Test
    fun getTitlesByTypeAndLengthReturnsOnlyMatchingRows() {
        val results = repository.getTitlesByTypeAndLength(TitleType.MOVIE, 10)
        assertTrue(results.isNotEmpty())
        assertTrue(results.all { it.titleType == TitleType.MOVIE && it.primaryTitle.length == 10 })
    }

    companion object {
        private val repository by lazy {
            val resourcesDir = resolveResourcesDirFromClasspath("/${ImdbDatasetManifestGenerator.TITLE_BASICS_TSV}")
            KImdb.inMemoryRepositoryFromTsv(
                resourcesDir.resolve(ImdbDatasetManifestGenerator.TITLE_BASICS_TSV),
                resourcesDir.resolve(ImdbDatasetManifestGenerator.NAME_BASICS_TSV)
            )
        }

        private fun resolveResourcesDirFromClasspath(resourcePath: String): Path {
            val url = object {}.javaClass.getResource(resourcePath)
                ?: error("Missing classpath resource: $resourcePath")
            return Path.of(url.toURI()).parent
        }
    }
}
