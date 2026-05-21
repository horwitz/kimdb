package com.kimdb.tsv

import com.kimdb.InMemoryKimdbRepository
import com.kimdb.model.Genre
import com.kimdb.model.IsAdult
import com.kimdb.model.NConst
import com.kimdb.model.Name
import com.kimdb.model.TConst
import com.kimdb.model.Title
import com.kimdb.model.TitleType
import java.nio.file.Path
import kotlin.io.path.useLines

object ImdbTsvParser {
    private const val NULL_TOKEN = "\\N"

    fun loadRepository(
        titleBasicsPath: Path,
        nameBasicsPath: Path,
    ): InMemoryKimdbRepository =
        InMemoryKimdbRepository(
            titles = parseTitleBasics(titleBasicsPath),
            names = parseNameBasics(nameBasicsPath),
        )

    fun parseTitleBasics(path: Path) = parseTsv(path, expectedColumns = 9, rowParser = ::parseTitleRow)

    fun parseNameBasics(path: Path) = parseTsv(path, expectedColumns = 6, rowParser = ::parseNameRow)

    private fun <T> parseTsv(
        path: Path,
        expectedColumns: Int,
        rowParser: (List<String>) -> T,
    ) =
        path.useLines { lines ->
            lines
                .drop(1)
                .filter(String::isNotBlank)
                .map { line ->
                    val cols = line.split('\t')
                    require(cols.size == expectedColumns) {
                        "Expected $expectedColumns columns, got ${cols.size}: $line"
                    }
                    rowParser(cols)
                }.toList()
        }

    private fun parseTitleRow(cols: List<String>) =
        Title(
            tconst = TConst.of(cols[0]),
            titleType = TitleType.of(cols[1]),
            primaryTitle = cols[2],
            originalTitle = cols[3],
            isAdult = IsAdult.of(cols[4]),
            startYear = cols[5].toIntOrNullToken(),
            endYear = cols[6].toIntOrNullToken(),
            runTimeMinutes = cols[7].toLongOrNullToken(),
            genres = cols[8].toGenresOrEmpty(),
        )

    private fun parseNameRow(cols: List<String>) =
        Name(
            nconst = NConst.of(cols[0]),
            primaryName = cols[1],
            birthYear = cols[2].toIntOrNullToken(),
            deathYear = cols[3].toIntOrNullToken(),
            primaryProfession = cols[4].toStringSetOrEmpty(),
            knownForTitles = cols[5].toTconstSetOrEmpty(),
        )

    private fun String.toIntOrNullToken() = if (this == NULL_TOKEN) null else toInt()

    private fun String.toLongOrNullToken() = if (this == NULL_TOKEN) null else toLong()

    private fun String.toGenresOrEmpty() = toCsvSetOrEmpty(Genre::of)

    private fun String.toStringSetOrEmpty() = toCsvSetOrEmpty { it }

    private fun String.toTconstSetOrEmpty() = toCsvSetOrEmpty(TConst::of)

    private fun <T> String.toCsvSetOrEmpty(f: (String) -> T) =
        if (this == NULL_TOKEN) {
            emptySet()
        } else {
            split(',').asSequence().filter(String::isNotBlank).map(f).toSet()
        }
}
