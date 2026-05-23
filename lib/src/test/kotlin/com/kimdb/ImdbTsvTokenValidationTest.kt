package com.kimdb

import com.kimdb.tsv.ImdbTsvTokenValidation
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Path

class ImdbTsvTokenValidationTest {
    @Test
    fun tokenSetsMatchModelMappings() {
        withFileLines(TestImdbFixture.titleBasicsPath) { titleLines ->
            withFileLines(TestImdbFixture.nameBasicsPath) { nameLines ->
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

    private fun <T> withFileLines(
        path: Path,
        block: (Sequence<String>) -> T
    ) = TestImdbFixture.withFileLines(path, block)
}
