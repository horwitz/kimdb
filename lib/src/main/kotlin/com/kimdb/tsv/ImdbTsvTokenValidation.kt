package com.kimdb.tsv

import com.kimdb.model.Genre
import com.kimdb.model.IsAdult
import com.kimdb.model.TitleType

data class ImdbTsvTokenValidationReport(
    val titleTypes: Set<String>,
    val isAdultValues: Set<String>,
    val genres: Set<String>,
    val primaryProfessions: Set<String>,
    val unknownTitleTypes: Set<String>,
    val unknownIsAdultValues: Set<String>,
    val unknownGenres: Set<String>
)

object ImdbTsvTokenValidation {
    fun validate(
        titleBasicsLines: Sequence<String>,
        nameBasicsLines: Sequence<String>
    ): ImdbTsvTokenValidationReport {
        val titleTypes = linkedSetOf<String>()
        val isAdultValues = linkedSetOf<String>()
        val genres = linkedSetOf<String>()
        val primaryProfessions = linkedSetOf<String>()

        forEachTsvRow(titleBasicsLines, expectedColumns = 9) { cols ->
            titleTypes += cols[1]
            isAdultValues += cols[4]
            cols[8].toCsvTokensOrEmpty().forEach(genres::add)
        }

        forEachTsvRow(nameBasicsLines, expectedColumns = 6) { cols ->
            cols[4].toCsvTokensOrEmpty().forEach(primaryProfessions::add)
        }

        val knownTitleTypes = TitleType.entries.mapTo(linkedSetOf()) { it.imdbValue }
        val knownIsAdultValues = IsAdult.entries.mapTo(linkedSetOf()) { it.imdbValue }
        val knownGenres = Genre.entries.mapTo(linkedSetOf()) { it.imdbValue }

        return ImdbTsvTokenValidationReport(
            titleTypes = titleTypes,
            isAdultValues = isAdultValues,
            genres = genres,
            primaryProfessions = primaryProfessions,
            unknownTitleTypes = titleTypes - knownTitleTypes,
            unknownIsAdultValues = isAdultValues - knownIsAdultValues,
            unknownGenres = genres - knownGenres
        )
    }
}
