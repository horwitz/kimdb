package com.kimdb

import com.kimdb.tsv.ImdbDatasetManifestGenerator
import com.kimdb.tsv.ImdbTsvTokenValidation
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ImdbTsvTokenValidationTest {
    @Test
    fun tokenSetsMatchModelMappings() {
        withResourceLines("/${ImdbDatasetManifestGenerator.TITLE_BASICS_TSV}") { titleLines ->
            withResourceLines("/${ImdbDatasetManifestGenerator.NAME_BASICS_TSV}") { nameLines ->
                val report = ImdbTsvTokenValidation.validate(titleLines, nameLines)

                println("distinct titleType count=${report.titleTypes.size}")
                println("distinct isAdult count=${report.isAdultValues.size}")
                println("distinct genres count=${report.genres.size}")
                println("distinct primaryProfession count=${report.primaryProfessions.size}")
                println("unknown titleTypes=${report.unknownTitleTypes}")
                println("unknown isAdultValues=${report.unknownIsAdultValues}")
                println("unknown genres=${report.unknownGenres}")

                assertTrue(report.unknownTitleTypes.isEmpty(), "Unknown titleType values: ${report.unknownTitleTypes}")
                assertTrue(
                    report.unknownIsAdultValues.isEmpty(),
                    "Unknown isAdult values: ${report.unknownIsAdultValues}"
                )
                assertTrue(report.unknownGenres.isEmpty(), "Unknown genre values: ${report.unknownGenres}")
            }
        }
    }

    private fun <T> withResourceLines(
        resourcePath: String,
        block: (Sequence<String>) -> T,
    ): T {
        val stream =
            this::class.java.getResourceAsStream(resourcePath)
                ?: error("Missing classpath resource: $resourcePath")

        return stream.bufferedReader().useLines { lines -> block(lines) }
    }
}
