package com.kimdb.tsv

import com.kimdb.KImdb
import com.kimdb.api.KImdbRepository
import com.kimdb.model.Genre
import com.kimdb.model.IsAdult
import com.kimdb.model.NConst
import com.kimdb.model.Name
import com.kimdb.model.TConst
import com.kimdb.model.Title
import com.kimdb.model.TitleType
import java.nio.file.Path
import java.time.Year

object ImdbTsvParser {
    fun loadRepository(
        titleBasicsPath: Path,
        nameBasicsPath: Path
    ): KImdbRepository = KImdb.inMemoryRepository(
        titles = parseTitleBasics(titleBasicsPath),
        names = parseNameBasics(nameBasicsPath)
    )

    fun parseTitleBasics(path: Path) = mapTsvRows(path, expectedColumns = 9, rowMapper = ::parseTitleRow)

    fun parseNameBasics(path: Path) = mapTsvRows(path, expectedColumns = 6, rowMapper = ::parseNameRow)

    private fun parseTitleRow(cols: List<String>) = Title(
        tconst = TConst.of(cols[0]),
        titleType = TitleType.of(cols[1]),
        primaryTitle = cols[2],
        originalTitle = cols[3],
        isAdult = IsAdult.of(cols[4]),
        startYear = cols[5].toYearOrNullToken(),
        endYear = cols[6].toYearOrNullToken(),
        runTimeMinutes = cols[7].toLongOrNullToken(),
        genres = cols[8].toGenresOrEmpty()
    )

    private fun parseNameRow(cols: List<String>) = Name(
        nconst = NConst.of(cols[0]),
        primaryName = cols[1],
        birthYear = cols[2].toYearOrNullToken(),
        deathYear = cols[3].toYearOrNullToken(),
        primaryProfession = cols[4].toStringSetOrEmpty(),
        knownForTitles = cols[5].toTconstSetOrEmpty()
    )

    private fun String.toYearOrNullToken() = if (this == NULL_TOKEN) null else Year.of(toInt())

    private fun String.toLongOrNullToken() = if (this == NULL_TOKEN) null else toLong()

    private fun String.toGenresOrEmpty() = toCsvSetOrEmpty(Genre::of)

    private fun String.toStringSetOrEmpty() = toCsvSetOrEmpty { it }

    private fun String.toTconstSetOrEmpty() = toCsvSetOrEmpty(TConst::of)

    private fun <T> String.toCsvSetOrEmpty(f: (String) -> T) = toCsvTokensOrEmpty().asSequence().map(f).toSet()
}
